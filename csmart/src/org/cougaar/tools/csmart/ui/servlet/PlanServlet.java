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
 
package org.cougaar.tools.csmart.ui.servlet;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.util.*;

import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.http.HttpUtils;

import org.cougaar.util.UnaryPredicate;

import org.cougaar.core.servlet.ServletUtil;
import org.cougaar.core.servlet.SimpleServletSupport;
import org.cougaar.core.util.UID;
import org.cougaar.planning.ldm.asset.*;
import org.cougaar.planning.ldm.plan.*;
import org.cougaar.tools.csmart.runtime.ldm.event.HappinessChangeEvent;
import org.cougaar.tools.csmart.ui.monitor.PropertyNames;
import org.cougaar.tools.csmart.ui.psp.TranslateUtils;



/**
 * This PSP traverses Tasks and related objects (plan elements, assets,
 * workflows) and HappinessChangeEvents and returns information on them.
 * The information is encoded in name/value pairs stored in PropertyTree
 * objects which are serialized to the client.
 */
public class PlanServlet 
  extends HttpServlet
{
  /*
   * Cougaar hook
   */
  private SimpleServletSupport support;
  
  public PlanServlet(SimpleServletSupport support) {
    super();
    this.support = support;
  }
  
  public void doGet(
		    HttpServletRequest request,
		    HttpServletResponse response) throws IOException, ServletException
  {
     // create a new "PlanProvider" context per request
    PlanProvider pi = new PlanProvider(support);
    pi.execute(request, response);  
  }
  
  public void doPost(
		     HttpServletRequest request,
		     HttpServletResponse response) throws IOException, ServletException
  {
    // create a new "PlanProvider" context per request
    PlanProvider pi = new PlanProvider(support);
    pi.execute(request, response);  
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
  private static class PlanProvider {

    
    // flags set by the client to control what objects are returned
    static boolean ignorePlanElements = false;
    static boolean ignoreWorkflows = false;
    static boolean ignoreAssets = false;
    static ServletInputStream in;
    ServletOutputStream out;
    
    // limit on number of PropertyTrees to return; see javadocs above.
    static int limit = Integer.MAX_VALUE;
 
    /* since "MetricProvider" is a static inner class, here
     * we hold onto the support API.
     *
     * this makes it clear that MetricProvider only uses
     * the "support" from the outer class.
     */    
    static SimpleServletSupport support;
    
    public PlanProvider(SimpleServletSupport support) {
      this.support = support;
    }
    

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

  public void execute(HttpServletRequest request, 
		      HttpServletResponse response) throws IOException, ServletException 
  {
    this.out = response.getOutputStream();
  
    try{
      parseParams(request);
    
      List ret = getObjects();
      if (ret!=null)
	{
	  ObjectOutputStream p = new ObjectOutputStream(out);
	  p.writeObject(ret);
	  System.out.println("Sent Objects");
	}
    } catch (Exception e) {
      System.out.println("PlanServlet Exception: " + e);
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
    private static List getObjects() {
      
      // create predicate
      UnaryPredicate pred = getPred();
      
      // query
      Collection col = 
	support.queryBlackboard(pred);
      
      int colSize = col.size();
      
      // check limit
      int maxRetSize;
      if (limit < colSize) {
	maxRetSize = limit + 1;
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
	support.getAgentIdentifier().getAddress();
      
      // scan query results
      Iterator iter = col.iterator();
      for (int i = 0; i < colSize; i++) {
	Object planObject = iter.next();
	
	// scan object
	if (planObject instanceof Asset) {
	  if (ignoreAssets)         
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
		   (ignorePlanElements) && 
		   (planObject instanceof PlanElement)) {
	  continue; // optionally ignore plan elements
	} else if (
		   (!(ignoreWorkflows)) && 
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
     * Make the GET and POST passing of parameters transparent to 
     * the user.
     * <p>
     * Determine either GET or POST methods, call with respective 
     * ServletUtil methods.
     *
     * @see ParamVisitor inner-class defined at the end of this class
     */
  public static void parseParams( 
      HttpServletRequest req) throws IOException
  {  
    String meth = req.getMethod();
    if (meth.equals("GET")) {
      // check for no query params
      if (req.getQueryString() != null) {
        Map m = HttpUtils.parseQueryString(req.getQueryString());
        parseParams(m);
      }
    } else if (meth.equals("POST")) {
      int len = req.getContentLength();
      in = req.getInputStream();
      Map m = HttpUtils.parsePostData(len, in);
      parseParams(m);
    }
  }

  /**
   * Given a <code>Map</code> of (name, value) pairs, call back 
   * to the given <code>ParamVisitor</code>'s "setParam(name,value)"
   * method.
   *
   * @see ParamVisitor inner-class defined at the end of this class
   */
  public static void parseParams(
      Map m) {
    Iterator iter = m.entrySet().iterator();
    while (iter.hasNext()) {
      Map.Entry me = (Map.Entry)iter.next();
      String key = me.getKey().toString();
      String[] value_array = (String[])me.getValue();
      String value = value_array[0];
      setParams(key, value);      
    }
  }
    
    /**
     * Sets "objects to ignore" variables from
     * string in URL predicated with ?ignorePlanObjects
     */
    
    public static void setParams(String name, String value) {
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
}
