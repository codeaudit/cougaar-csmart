/* 
 * <copyright>
 * This software is to be used only in accordance with the COUGAAR license
 * agreement. The license agreement and other information can be found at
 * http://www.cougaar.org
 * 
 *       © Copyright 2001 by BBNT Solutions LLC.
 * </copyright>
 */
 
package org.cougaar.tools.csmart.ui.psp;

import java.io.*;
import java.net.*;
import java.util.*;

import org.cougaar.core.cluster.Subscription;
import org.cougaar.core.society.UID;
import org.cougaar.domain.planning.ldm.asset.*;
import org.cougaar.domain.planning.ldm.plan.*;

import org.cougaar.lib.planserver.*;

import org.cougaar.util.UnaryPredicate;

/**
 * PSP to gather Metrics for CSMART
 *
 * <pre>
 *    number of tasks
 *    % and number of tasks that are not yet allocated
 *    % and number of tasks that are low confidence < 0.50
 * </pre>
 */
public class PSP_Metrics 
extends PSP_BaseAdapter
implements PlanServiceProvider, UISubscriber
{

  /** 
   * A zero-argument constructor is required for dynamically loaded PSPs,
   * required by Class.newInstance()
   **/
  public PSP_Metrics() {
    super();
  }

  protected static final UnaryPredicate TASK_PRED =
    new UnaryPredicate() {
      public boolean execute(Object o) {
        return (o instanceof Task);
      }
    };


  public void execute(
      PrintStream out,
      HttpInput query_parameters,
      PlanServiceContext psc,
      PlanServiceUtilities psu) throws Exception
  {
    // parse parameters
    MyPSPState myState = new MyPSPState(this, query_parameters, psc);
    myState.configure(query_parameters);

    // run
    execute(myState, out);
  }

  /**
   * Fetch CompletionData and write to output.
   */
  protected void execute(
      MyPSPState myState,
      PrintStream out) {
    // get result

    try {
      ArrayList list = collectData(myState);
      ObjectOutputStream oos = new ObjectOutputStream(out);
      oos.writeObject(list);
    } catch(Exception e) {
      System.out.println("PSP_Metric Exception: " + e);
      e.printStackTrace();
    }
  }

  protected static Collection getAllTasks(
      MyPSPState myState) {
    Collection col =
      myState.psc.getServerPlugInSupport().queryForSubscriber(
          TASK_PRED);
    if (col == null) {
      col = new ArrayList(0);
    }
    return col;
  }

  protected ArrayList collectData(MyPSPState myState) {

    Collection tasks = getAllTasks(myState);
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
    results.add(myState.clusterID);
    results.add(data);
    return results;
  }

  /** 
   * Holds PSP state.
   */
  protected static class MyPSPState extends PSPState {

    /** my additional fields **/
    public boolean anyArgs;
    public int format;
    public boolean showTables;

    public static final int FORMAT_CSV = 0;
    public static final int FORMAT_HTML = 1;

    /** constructor **/
    public MyPSPState(
        UISubscriber xsubscriber,
        HttpInput query_parameters,
        PlanServiceContext xpsc) {
      super(xsubscriber, query_parameters, xpsc);
      // default to HTML
      format = FORMAT_HTML;
    }

    /** use a query parameter to set a field **/
    public void setParam(String name, String value) {
      //super.setParam(name, value);
      if (eq("format", name)) {
        anyArgs = true;
        if (eq("csV", value)) {
          format = FORMAT_CSV;
        } else if (eq("html", value)) {
          format = FORMAT_HTML;
        }
      } else if (eq("csv", name)) {
        anyArgs = true;
        format = FORMAT_CSV;
      } else if (eq("html", name)) {
        anyArgs = true;
        format = FORMAT_HTML;
      }
    }

    // startsWithIgnoreCase
    private static final boolean eq(String a, String b) {
      return a.regionMatches(true, 0, b, 0, a.length());
    }
  }

  //
  // uninteresting and/or obsolete methods 
  //
  public PSP_Metrics(
      String pkg, String id) 
    throws RuntimePSPException {
      setResourceLocation(pkg, id);
    }
  public boolean test(
      HttpInput query_parameters, PlanServiceContext sc) {
    super.initializeTest();
    return false;
  }
  public void subscriptionChanged(Subscription subscription) {
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

}
