/*
 * <copyright>
 *  Copyright 1997-2001 BBNT Solutions, LLC
 *  under sponsorship of the Defense Advanced Research Projects Agency (DARPA).
 * 
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the Cougaar Open Source License as published by
 *  DARPA on the Cougaar Open Source Website (www.cougaar.org).
 * 
 *  THE COUGAAR SOFTWARE AND ANY DERIVATIVE SUPPLIED BY LICENSOR IS
 *  PROVIDED 'AS IS' WITHOUT WARRANTIES OF ANY KIND, WHETHER EXPRESS OR
 *  IMPLIED, INCLUDING (BUT NOT LIMITED TO) ALL IMPLIED WARRANTIES OF
 *  MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE, AND WITHOUT
 *  ANY WARRANTIES AS TO NON-INFRINGEMENT.  IN NO EVENT SHALL COPYRIGHT
 *  HOLDER BE LIABLE FOR ANY DIRECT, SPECIAL, INDIRECT OR CONSEQUENTIAL
 *  DAMAGES WHATSOEVER RESULTING FROM LOSS OF USE OF DATA OR PROFITS,
 *  TORTIOUS CONDUCT, ARISING OUT OF OR IN CONNECTION WITH THE USE OR
 *  PERFORMANCE OF THE COUGAAR SOFTWARE.
 * </copyright>
 */
 
package org.cougaar.tools.csmart.ui.psp;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.util.*;

import org.cougaar.core.agent.*;
import org.cougaar.core.domain.*;
import org.cougaar.core.blackboard.*;
import org.cougaar.core.mts.Message;
import org.cougaar.core.mts.MessageAddress;
import org.cougaar.core.util.*;
import org.cougaar.planning.ldm.asset.Asset;
import org.cougaar.planning.ldm.plan.Expansion;
import org.cougaar.planning.ldm.plan.HasRelationships;
import org.cougaar.planning.ldm.plan.PlanElement;
import org.cougaar.planning.ldm.plan.Task;
import org.cougaar.planning.ldm.plan.Workflow;
import org.cougaar.lib.planserver.*;
import org.cougaar.tools.csmart.runtime.ldm.event.HappinessChangeEvent;
import org.cougaar.tools.csmart.ui.monitor.PropertyNames;
import org.cougaar.util.*;

/**
 * This PSP traverses Tasks and related objects (plan elements, assets,
 * workflows) and HappinessChangeEvents and returns information on them.
 * The information is encoded in name/value pairs stored in PropertyTree
 * objects which are serialized to the client.
 */
public class PSP_Plan 
extends PSP_BaseAdapter 
implements PlanServiceProvider, UISubscriber {

  /**
   * Our predicate for finding Blackboard objects.
   */
  private static UnaryPredicate getPred() {
    return new UnaryPredicate() {
      public boolean execute(Object o) {
        return
          (o instanceof Task || 
           o instanceof PlanElement ||
           o instanceof Asset ||
           o instanceof HappinessChangeEvent);
      }
    };
  }

  /**
   * This is the main PSP method called by the infrastructure in response
   * to receiving a request from a client.
   * Get all the plan objects, ignoring those the client specifies,
   * and return their properties to the client in a serialized PropertyTree.
   */

  public void execute(PrintStream out,
                      HttpInput query_parameters,
                      PlanServiceContext psc,
                      PlanServiceUtilities psu) throws Exception {
    try {
      PlanPSPState pspState = 
        new PlanPSPState(this, query_parameters, psc);
      pspState.configure(query_parameters);

      List ret = getObjects(psc, pspState);

      ObjectOutputStream oos = new ObjectOutputStream(out);
      oos.writeObject(ret);
    } catch (Exception e) {
      System.out.println("PSP_Plan Exception: " + e);
      e.printStackTrace();
    }
  }

  /**
   * Returns a vector of PropertyTree for either a Task and
   * all its related objects (planelements, workflows, assets) or
   * for a HappinessChangeEvent.
   * 
   * @see PlanPSPState see javadocs for "pspState.limit" and 
   *   "pspState.ignore*" details
   */
  private static List getObjects(
      PlanServiceContext psc,
      PlanPSPState pspState) {

    // create predicate
    UnaryPredicate pred = getPred();

    // query
    Collection col = 
      psc.getServerPlugInSupport().queryForSubscriber(pred);
    int colSize = col.size();

    // check limit
    int maxRetSize;
    if (pspState.limit < colSize) {
      maxRetSize = pspState.limit + 1;
    } else {
      maxRetSize = colSize;
    }

    // build result list
    //
    // assume maximum List size, since most objects are kept
    List ret = new ArrayList(maxRetSize);
    int retSize = 0;

    // keep agent name for later use
    String agent = 
      psc.getServerPlugInSupport().getClusterIDAsString();

    // scan query results
    Iterator iter = col.iterator();
    for (int i = 0; i < colSize; i++) {
      Object planObject = iter.next();

      // scan object
      if (planObject instanceof Asset) {
        if (pspState.ignoreAssets)
          continue;
        Asset asset = (Asset)planObject;
        if ((asset.hasClusterPG()) &&
            (asset instanceof HasRelationships) &&
            (!((HasRelationships)asset).isLocal())) {
          // ignore non-local agent assets
          //
          // add below back in once Communities are added?
          //    (asset.hasCommunityPG())
          continue; 
        }
      } else if (
          (pspState.ignorePlanElements) && 
          (planObject instanceof PlanElement)) {
        continue; // optionally ignore plan elements
      } else if (
          (!(pspState.ignoreWorkflows)) && 
          (planObject instanceof Expansion)) { 
        // optionally include workflows
        Workflow workflow = ((Expansion)planObject).getWorkflow();
        Object wObj = TranslateUtils.toPropertyTree(workflow, agent);
        // TranslateUtils will return null for some objects
        if (wObj != null) {
          ret.add(wObj);

          if (++retSize >= maxRetSize) {
            // reached our limit
            break;
          }
        }
      }

      // translate to a property-tree
      Object tmp = TranslateUtils.toPropertyTree(planObject, agent);
      if (tmp != null) {
        ret.add(tmp);

        if (++retSize >= maxRetSize) {
          // reached our limit
          break;
        }
      }
    }

    return ret;
  }

  /**
   * Captures the URL parameters.
   *
   * If input from the client contains the name/value pair:
   *   planObjectsToIgnore?value
   * then extract the value, which is a comma separated list
   * of types of objects to ignore (defined in PropertyNames).
   * Use these object types, to set appropriate flags used
   * in the getter methods.
   *
   * If the URL includes "?limit=NUMBER", then a limit is set
   * for the number of objects to return.  If the limit is
   * negative then no limit is set, which is the default.  If
   * the limit is exceeded then (limit+1) objects are returned.
   * For example, if "?limit=50" and there are 100 matching 
   * objects, then 51 PropertyTrees are returned.
   */
  public static class PlanPSPState extends PSPState {
    // flags set by the client to control what objects are returned
    public boolean ignorePlanElements = false;
    public boolean ignoreWorkflows = false;
    public boolean ignoreAssets = false;

    // limit on number of PropertyTrees to return; see javadocs above.
    public int limit = Integer.MAX_VALUE;

    public PlanPSPState(
        UISubscriber xsubscriber,
        HttpInput query_parameters,
        PlanServiceContext xpsc) {
      super(xsubscriber, query_parameters, xpsc);
    }

    /**
     * Sets "objects to ignore" variables from
     * string in URL predicated with ?ignorePlanObjects
     */

    public void setParam(String name, String value) {
      if (name.equals(PropertyNames.PLAN_OBJECTS_TO_IGNORE)) {
        try {
          StringTokenizer st = 
            new StringTokenizer(value, ",");
          while (st.hasMoreTokens()) {
            String s = st.nextToken();
            if (s.equals(PropertyNames.PLAN_ELEMENT_OBJECT)) {
              ignorePlanElements = true;
            } else if (s.equals(PropertyNames.WORKFLOW_OBJECT)) {
              ignoreWorkflows = true;
            } else if (s.equals(PropertyNames.ASSET_OBJECT)) {
              ignoreAssets = true;
            }
          }
        } catch (Exception e) {
          System.err.println("Illegal parameter: "+name+"="+value);
        }
      } else if (name.equals("limit")) {
        try {
          limit = Integer.parseInt(value);
          if (limit < 0) {
            limit = Integer.MAX_VALUE;
          }
        } catch (Exception e) {
          System.err.println("Illegal parameter: "+name+"="+value);
        }
      }
    }
  }

  //
  // ancient/boring methods:
  //

  public PSP_Plan() {
    super();
  }
  public PSP_Plan( String pkg, String id ) throws RuntimePSPException {
    setResourceLocation(pkg, id);
  }
  public boolean test(HttpInput query_parameters, PlanServiceContext sc)  {
    super.initializeTest();
    return false;
  }
  public boolean returnsXML() {
    return false;
  }
  public boolean returnsHTML() {
    return false;
  }
  public String getDTD()  {
    return null;
  }
  public void subscriptionChanged(Subscription subscription) {
  }

}
