/**
 * Servlet to gather Metrics for CSMART
 *
 * <pre>
 *    number of tasks
 *    % and number of tasks that are not yet allocated
 *    % and number of tasks that are low confidence < 0.50
 * </pre>
 */

package org.cougaar.tools.csmart.ui.servlet;

import java.io.IOException;
import java.io.PrintWriter;

import java.util.Collection;
import java.util.Vector;
import java.util.ArrayList;
import java.util.Iterator;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpUtils;

import org.cougaar.core.servlet.ServletUtil;
import org.cougaar.core.servlet.SimpleServletSupport;
import org.cougaar.core.util.UID;
import org.cougaar.util.UnaryPredicate;
import org.cougaar.planning.ldm.plan.AllocationResult;
import org.cougaar.planning.ldm.plan.PlanElement;
import org.cougaar.planning.ldm.plan.Task;

public class MetricsServlet 
  extends HttpServlet
{
  private SimpleServletSupport support;
  
  public MetricsServlet(SimpleServletSupport support) {
    super();
    this.support = support;
  }
  
  public void doGet(
		    HttpServletRequest request,
		    HttpServletResponse response) throws IOException, ServletException
  {
    // create a new "MetricProvider" context per request
    MetricProvider mp = new MetricProvider(support);
    mp.execute(request, response);  
  }
  
  public void doPost(
		     HttpServletRequest request,
		     HttpServletResponse response) throws IOException, ServletException
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
    
    /*
     * parameters from the URL:
     */
    
    // writer from the request
    private PrintWriter out;
    
    /* since "MetricProvider" is a static inner class, here
     * we hold onto the support API.
     *
     * this makes it clear that MetricProvider only uses
     * the "support" from the outer class.
     */      
    private SimpleServletSupport support;
    
    // inner class constructor
    public MetricProvider(SimpleServletSupport support) {
      this.support = support;
    }
    

  protected static final UnaryPredicate TASK_PRED =
    new UnaryPredicate() {
      public boolean execute(Object o) {
        return (o instanceof Task);
      }
    };


  public void execute(HttpServletRequest request, 
			 HttpServletResponse response) throws IOException, ServletException 
    {
      
      this.out = response.getWriter();
      
      // create a URL parameter visitor
      ServletUtil.ParamVisitor vis = 
        new ServletUtil.ParamVisitor() {
	    public void setParam(String name, String value) {
	      /* There is no mode, as it only returns the list of agent
	       * names, so do nothing but set up for parameter parsing.   
	       */	  
	    }
	  };
      
      // visit the URL parameters; parse params
      ServletUtil.parseParams(vis, request);
      

  /**
   * Fetch CompletionData and write to output.
   */
      
    try {
      ArrayList list = collectData(support);
      out.print(list);
    } catch(Exception e) {
      System.out.println("CSMART_MetricsServlet Exception: " + e);
      e.printStackTrace();
    }
    }

    protected static Collection getAllTasks(SimpleServletSupport support) {
      Collection col =
      support.queryBlackboard(TASK_PRED);
    if (col == null) {
      col = new ArrayList(0);
    }
    return col;
    }
    
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
