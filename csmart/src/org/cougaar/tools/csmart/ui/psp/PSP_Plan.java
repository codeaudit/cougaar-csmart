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

import org.cougaar.core.cluster.*;
import org.cougaar.core.util.*;
import org.cougaar.domain.planning.ldm.asset.Asset;
import org.cougaar.domain.planning.ldm.plan.Expansion;
import org.cougaar.domain.planning.ldm.plan.HasRelationships;
import org.cougaar.domain.planning.ldm.plan.PlanElement;
import org.cougaar.domain.planning.ldm.plan.Task;
import org.cougaar.domain.planning.ldm.plan.Workflow;
import org.cougaar.lib.planserver.*;
import org.cougaar.tools.csmart.ldm.event.HappinessChangeEvent;
import org.cougaar.tools.csmart.ui.monitor.PropertyNames;
import org.cougaar.util.*;

/**
 * This PSP traverses Tasks and related objects (plan elements, assets,
 * workflows) and HappinessChangeEvents and returns information on them.
 * The information is encoded in name/value pairs stored in PropertyTree
 * objects which are serialized to the client.
 */

public class PSP_Plan extends PSP_BaseAdapter implements PlanServiceProvider, UISubscriber {
  Vector assetUIDs;
  // flags set by the client to control what objects are returned
  boolean ignorePlanElements = false;
  boolean ignoreDirectObjects = false;
  boolean ignoreWorkflows = false;

  public PSP_Plan() {
    super();
  }

  public PSP_Plan( String pkg, String id ) throws RuntimePSPException {
    setResourceLocation(pkg, id);
  }

  private static UnaryPredicate getPred() {
    return new UnaryPredicate() {
      public boolean execute(Object o) {
	if (o instanceof Task || 
	    o instanceof HappinessChangeEvent ||
	    o instanceof Asset ||
	    o instanceof PlanElement)
	  return true;
	return false;
      };
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
      parseFilter(query_parameters, psc);
      ObjectOutputStream oos = new ObjectOutputStream(out);
      oos.writeObject(getAllObjects(psc));
    } catch (Exception e) {
      System.out.println("PSP_Plan Exception: " + e);
      e.printStackTrace();
    }
  }

  /**
   * If input from the client contains the name/value pair:
   * planObjectsToIgnore?value
   * then extract the value, which is a comma separated list
   * of types of objects to ignore (defined in PropertyNames).
   * Use these object types, to set appropriate flags used
   * in the getter methods.
   */

  private void parseFilter(HttpInput queryParameters,
			   PlanServiceContext psc) {
    PlanPSPState pspState = new PlanPSPState(this, queryParameters, psc);
    pspState.configure(queryParameters);
    ignorePlanElements = false;
    ignoreDirectObjects = false;
    ignoreWorkflows = false;
    if (pspState.planObjectsToIgnore == null)
      return;
    StringTokenizer st = 
      new StringTokenizer(pspState.planObjectsToIgnore, ",");
    while (st.hasMoreTokens()) {
      String s = st.nextToken();
      if (s.equals(PropertyNames.PLAN_ELEMENT_OBJECT))
	ignorePlanElements = true;
      else if (s.equals(PropertyNames.DIRECT_OBJECT))
	ignoreDirectObjects = true;
      else if (s.equals(PropertyNames.WORKFLOW_OBJECT))
	ignoreWorkflows = true;
    }
  }

  /**
   * Returns a vector of PropertyTree for either a Task and
   * all its related objects (planelements, workflows, assets) or
   * for a HappinessChangeEvent.
   */

  private ArrayList getAllObjects(PlanServiceContext psc) {
    String agent = psc.getServerPlugInSupport().getClusterIDAsString();
    Collection container = 
      psc.getServerPlugInSupport().queryForSubscriber(getPred());
    ArrayList results = new ArrayList(container.size());
    Iterator iter = container.iterator();
    while (iter.hasNext()) {
      Object planObject = iter.next();
      if (planObject instanceof Asset) {
	Asset asset = (Asset)planObject;
	if ((asset instanceof HasRelationships) &&
	    !((HasRelationships)asset).isLocal() &&
	    // Add below back in once Communities are added?
	    //	    (asset.hasCommunityPG(new Date().getTime())) &&
	    (asset.hasClusterPG()))
	  continue; // ignore non-local agent assets
      } else if (ignorePlanElements && planObject instanceof PlanElement) {
	continue; // optionally ignore plan elements
      } else if (!ignoreWorkflows && planObject instanceof Expansion) { 
	// optionally include workflows
	Workflow workflow = ((Expansion)planObject).getWorkflow();
	Object tmp = TranslateUtils.toPropertyTree(workflow, agent);
	// TranslateUtils will return null for some objects
	if (tmp != null)
	  results.add(tmp);
      }
      Object tmp = TranslateUtils.toPropertyTree(planObject, agent);
      // TranslateUtils will return null for some objects
      if (tmp != null)
	results.add(tmp);
      //      results.add(TranslateUtils.toPropertyTree(planObject, agent));
    }
    return results;
  }

  /**
   * Required by PSP interface.
   */

  public boolean test(HttpInput query_parameters, PlanServiceContext sc)  {
    super.initializeTest(); // IF subclass off of PSP_BaseAdapter.java
    return false;  // This PSP is only accessed by direct reference.
  }

  /**
   * A PSP can output either HTML or XML (for now).  The server
   * should be able to ask and find out what type it is.
   **/

  public boolean returnsXML() {
    return false;
  }

  public boolean returnsHTML() {
    return false;
  }

  /**  Any PlanServiceProvider must be able to provide DTD of its
   *  output IFF it is an XML PSP... ie.  returnsXML() == true;
   *  or return null
   **/

  public String getDTD()  {
    return null;
  }

  /**
   * Required by UISubscriber interface.
   */

  public void subscriptionChanged(Subscription subscription) {
  }

  public static class PlanPSPState extends PSPState {
    public String planObjectsToIgnore;

    public PlanPSPState(
        UISubscriber xsubscriber,
        HttpInput query_parameters,
        PlanServiceContext xpsc) {
      super(xsubscriber, query_parameters, xpsc);
    }

    /**
     * Sets planObjectsToIgnore from
     * string in URL predicated with ?ignorePlanObjects
     */

    public void setParam(String name, String value) {
      if (name.equals(PropertyNames.PLAN_OBJECTS_TO_IGNORE))
	planObjectsToIgnore = value;
    }
  }

}
