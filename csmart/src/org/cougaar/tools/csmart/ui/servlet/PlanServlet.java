/*
 * <copyright>
 *  
 *  Copyright 1997-2004 BBNT Solutions, LLC
 *  under sponsorship of the Defense Advanced Research Projects
 *  Agency (DARPA).
 * 
 *  You can redistribute this software and/or modify it under the
 *  terms of the Cougaar Open Source License as published on the
 *  Cougaar Open Source Website (www.cougaar.org).
 * 
 *  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 *  "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 *  LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 *  A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 *  OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 *  SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 *  LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 *  DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 *  THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 *  OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *  
 * </copyright>
 */
 
package org.cougaar.tools.csmart.ui.servlet;






import org.cougaar.core.servlet.ServletUtil;
import org.cougaar.core.servlet.SimpleServletSupport;
import org.cougaar.core.util.UID;
import org.cougaar.planning.ldm.asset.Asset;
import org.cougaar.planning.ldm.plan.Expansion;
import org.cougaar.planning.ldm.plan.HasRelationships;
import org.cougaar.planning.ldm.plan.PlanElement;
import org.cougaar.planning.ldm.plan.Task;
import org.cougaar.planning.ldm.plan.Workflow;
import org.cougaar.tools.csmart.ui.monitor.PropertyNames;
import org.cougaar.util.UnaryPredicate;
import org.cougaar.util.log.Logger;

import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

/**
 * <p>
 * This Servlet traverses Tasks and related objects (plan elements, assets,
 * workflows) and returns information on them.
 * The information is encoded in name/value pairs stored in PropertyTree. <br>
 *
 * <p>
 * Can be loaded manually by including this line in an agent's .ini configuration file: <pre/>
 *   plugin = org.cougaar.core.servlet.SimpleServletComponent(org.cougaar.tools.csmart.ui.servlet.PlanServlet, 
 *   /CSMART_PlanServlet)
 * </pre>
 * 
 * <p>
 * Is loaded from a URL on a CSMART machine, on agent 'Agent':
 *   http://localhost:port/$Agent/CSMART_PlanServlet objects which are serialized to the client.
 */
public class PlanServlet 
  extends HttpServlet
{
  /*
   * Cougaar hook
   */
  private SimpleServletSupport support;

  public void setSimpleServletSupport(SimpleServletSupport support) {
    this.support = support;
    if ( !  ( "/CSMART_PlanServlet".equals(support.getPath()) ) ) {
      support.getLog().error("Incorrect servlet path: " + support.getPath());
    }
  }
  
  public void doGet(
		    HttpServletRequest request,
		    HttpServletResponse response) throws IOException, ServletException
  {
    // create a new "PlanProvider" context per request
    PlanProvider pi = new PlanProvider(support);
    pi.execute(request, response);  
  }
 
  public void doPut(
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
  private static class PlanProvider implements ServletUtil.ParamVisitor {

    /* flags set by the client to control what objects are returned */
    boolean ignorePlanElements = false;
    boolean ignoreWorkflows = false;
    boolean ignoreAssets = false;
    
    /* stream variables */
    ServletInputStream in;
    ServletOutputStream out;
    
    /* limit on number of PropertyTrees to return; see javadocs above */
    int limit = Integer.MAX_VALUE;

    /* since "PlanProvider" is a static inner class, here
     * we hold onto the support API.
     *
     * this makes it clear that PlanProvider only uses
     * the "support" from the outer class.
     */    
    SimpleServletSupport support;
    private Logger log;
    
    public PlanProvider(SimpleServletSupport support) {
      this.support = support;
      this.log = support.getLog();
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
	       o instanceof Asset);
	  }
	};
    }
    
    /**
     * This is the main Servlet method called by the infrastructure in response
     * to receiving a request from a client.
     * Get all the plan objects, ignoring those the client specifies,
     * and return their properties to the client in a serialized PropertyTree.
     */
    public void execute(HttpServletRequest request, 
			HttpServletResponse response) throws IOException
    {
      this.out = response.getOutputStream();
      
      try {
        ServletUtil.parseParams(this, request);
	
	List ret = getObjects();
        if (ret!=null) {
          ObjectOutputStream p = new ObjectOutputStream(out);
          p.writeObject(ret);

          if(log.isDebugEnabled()) {
            log.debug("Sent Objects");
          }
        }
      } catch (Exception e) {
        if(log.isErrorEnabled()) {
          log.error("PlanServlet Exception: ", e);
        }
      }
    }
    
    /**
     * Returns a vector of PropertyTree for either a Task and
     * all its related objects (planelements, workflows, assets)
     * @return List
     */
    private List getObjects() {
      
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
     * Sets "objects to ignore" variables from
     * string in URL predicated with ?ignorePlanObjects
     * @param name name of parameter
     * @param value value of parameter
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
          if(log.isErrorEnabled()) {
            log.error("Illegal parameter: "+name+"="+value, e);
          }
        }
      } else if (name.equals("limit")) {
        try {
          limit = Integer.parseInt(value);
          if (limit < 0) {
            limit = Integer.MAX_VALUE;
          }
        } catch (Exception e) {
          if(log.isErrorEnabled()) {
            log.error("Illegal parameter: "+name+"="+value, e);
          }
        }
      }
    }
  }
}
