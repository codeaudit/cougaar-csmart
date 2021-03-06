/* 
 * <copyright>
 *  
 *  Copyright 2002-2004 BBNT Solutions, LLC
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




import org.cougaar.core.servlet.SimpleServletSupport;
import org.cougaar.core.util.UID;
import org.cougaar.planning.ldm.plan.AllocationResult;
import org.cougaar.planning.ldm.plan.PlanElement;
import org.cougaar.planning.ldm.plan.Task;
import org.cougaar.util.UnaryPredicate;
import org.cougaar.util.log.Logger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

/**
 * Servlet to gather Metrics for CSMART. 
 *
 * <pre/>
 *    number of tasks
 *    % and number of tasks that are not yet allocated
 *    % and number of tasks that are low confidence < 0.50
 * </pre>
 *
 * <p>
 * Can be loaded manually by including this line in an agent's .ini configuration file: <br>
 *   plugin = org.cougaar.core.servlet.SimpleServletComponent(org.cougaar.tools.csmart.ui.servlet.MetricsServlet, 
 *   /CSMART_MetricsServlet)
 *
 * <p>
 * Is loaded from a URL on a CSMART machine, on agent 'Agent': <br>
 *   http://localhost:port/$Agent/CSMART_MetricsServlet
 *
 */
public class MetricsServlet 
  extends HttpServlet
{
  private SimpleServletSupport support;

  public void setSimpleServletSupport(SimpleServletSupport support) {
    this.support = support;
    if ( !  ( "/CSMART_MetricsServlet".equals(support.getPath()) ) ) {
      support.getLog().error("Incorrect servlet path: " + support.getPath());
    }
  }
  
  /* Reponds to the GET http method call 
   * @param request HttpServletRequest
   * @param response HttpServletResponse
   */
  public void doGet(
		    HttpServletRequest request,
		    HttpServletResponse response) throws IOException
  {
    // create a new "MetricProvider" context per request
    MetricProvider mp = new MetricProvider(support);
    mp.execute(request, response);  
  }
  
  /* Reponds to the PUT http method call 
   * @param request HttpServletRequest
   * @param response HttpServletResponse
   */
  public void doPut(
		    HttpServletRequest request,
		    HttpServletResponse response) throws IOException
  {
    // create a new "MetricProvider" context per request
    MetricProvider mp = new MetricProvider(support);
    mp.execute(request, response);  
  }
  
  /**
   * This inner class does all the work.
   * <p>
   * A new class is created per request, to keep all the
   * instance fields separate.  If there was only one
   * instance then multiple simultaneous requests would
   * corrupt the instance fields (e.g. the "out" stream).
   * <p>
   * This acts as a <b>context</b> per request.
   */
  private static class MetricProvider {
    
    /* Output Stream obtained from the request */
    private OutputStream out;
    
    /* since "MetricProvider" is a static inner class, here
     * we hold onto the support API.
     *
     * this makes it clear that MetricProvider only uses
     * the "support" from the outer class.
     */      
    private SimpleServletSupport support;
    private Logger log;
    
    /* Inner class constructor
     * 
     * @param support Cougaar hook
     */
    public MetricProvider(SimpleServletSupport support) {
      this.support = support;
      this.log = support.getLog();
    }
    
    /* Obtains Task Predicate */
    protected static final UnaryPredicate TASK_PRED =
      new UnaryPredicate() {
	  public boolean execute(Object o) {
	    return (o instanceof Task);
	  }
	};
    
    /*
     * Main Servlet method. Parses any parameters passed into it, collects data
     * on all Tasks and writes the object back to the client.
     * @param request HttpServletRequest
     * @param response HttpServletResponse
     */
    public void execute(HttpServletRequest request, 
			HttpServletResponse response) throws IOException
    {
      
      // There is no mode, as it only returns the list of agent
      // names, so do nothing but set up for parameter parsing.   

      if( request.getQueryString() == null) {
	response.setStatus(
			   HttpServletResponse.SC_OK);
      }
      
      // check for illegal arguments
      if( request.getQueryString() != null) {
	response.sendError(
			   HttpServletResponse.SC_BAD_REQUEST,
			   "<html><body><br><font size=4>" + 
			   "<font size = 5><B>" + 
			   request.getQueryString() + 
			   "</B></font>" +
			   " Is Not A Legal Parameter List<br>" + 
			   "This servlet expects no parameters" +
			   "</font></body></html>");
      }
      
      /**
       * Fetch CompletionData and write to output.
       */
      try {
	ArrayList list = collectData(support);
	if (list != null ){
	  // serialize back to the user
	  this.out = response.getOutputStream();
	  ObjectOutputStream p = new ObjectOutputStream(out);
	  p.writeObject(list);
	  //System.out.println("Sent Objects");  
	}
      } catch (Exception e) {
        if(log.isErrorEnabled()) {
          log.error("CSMART_MetricsServlet Exception", e);
        }
      }
    }
    
    /* Gets all sociey <code>Task</code> objects from support */
    protected static Collection getAllTasks(SimpleServletSupport support) {
      Collection col =
	support.queryBlackboard(TASK_PRED);
      if (col == null) {
	col = new ArrayList(0);
      }
      return col;
    }
    
    /* Get data from each Task 
     * @return ArrayList
     */
    protected ArrayList collectData(SimpleServletSupport support) { 
      
      Collection tasks = getAllTasks(support);
      Iterator taskIter = tasks.iterator();
      ArrayList results = new ArrayList(2);
      int nTasks = 0;
      int nLowConfidence = 0;
      int nUnallocated = 0;
      Integer[] data = new Integer[3];
      
      while(taskIter.hasNext()) {
	Task t = (Task)taskIter.next();
	nTasks++;
	PlanElement pe = t.getPlanElement();
	if(pe != null) {
	  AllocationResult estResult = pe.getEstimatedResult();
	  if( estResult != null ) {
	    double estConf = estResult.getConfidenceRating();
	    if( estConf < 0.5 ) {
	      nLowConfidence++;
	    }
	  }
	} else {
	  nUnallocated++;
	}	
      }
      data[0] = new Integer(nTasks);
      data[1] = new Integer(nUnallocated);
      data[2] = new Integer(nLowConfidence);
      results.add(support.getAgentIdentifier());
      results.add(data);
      return results;
    }
  }
}
