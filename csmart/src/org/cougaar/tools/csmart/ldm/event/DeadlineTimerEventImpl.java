/* 
 * <copyright>
 * This software is to be used only in accordance with the COUGAAR license
 * agreement. The license agreement and other information can be found at
 * http://www.cougaar.org
 * 
 *       © Copyright 2001 by BBNT Solutions LLC.
 * </copyright>
 */
package org.cougaar.tools.csmart.ldm.event;

import org.cougaar.core.society.UID;
import org.cougaar.core.cluster.ClusterIdentifier;

import org.cougaar.domain.planning.ldm.plan.Aggregation;
import org.cougaar.domain.planning.ldm.plan.Allocation;
import org.cougaar.domain.planning.ldm.plan.AllocationResult;
import org.cougaar.domain.planning.ldm.plan.AssetTransfer;
import org.cougaar.domain.planning.ldm.plan.AuxiliaryQueryType;
import org.cougaar.domain.planning.ldm.plan.Disposition;
import org.cougaar.domain.planning.ldm.plan.Expansion;
import org.cougaar.domain.planning.ldm.plan.PlanElement;
import org.cougaar.domain.planning.ldm.plan.Task;

/**
 * A timer that appears when some deadline has been reached.  It refers,
 * usually, to a <code>PlanElement</code> with which this deadline
 * is associated.  The typical semantics are that an <code>AllocationResult</code>
 * should bet set on the <code>PlanElement</code> in advance of the deadline.
 *
 * @author <a href="mailto:ahelsing@bbn.com">Aaron Helsinger</a>
 * @see PlanElement
 * @see org.cougaar.tools.csmart.plugin.AllocatorPlugIn
 */
public class DeadlineTimerEventImpl implements NewDeadlineTimerEvent {
  private PlanElement pe = null;

  private long time = 0l;

  private UID id;

  protected String publisher;
  protected ClusterIdentifier source;

  public DeadlineTimerEventImpl(UID uid) {
    this.id = uid;
  }
  
  // Need access to the thing for which you are expecting an answer
  public PlanElement getRegarding() {
    return pe;
  }

  // Time at which this deadline timer goes off
  public long getTime() {
    return time;
  }

  // Who is expecting this answer? What Cluster is it in?
  // should I put in something like that?
  public void setPublisher(String publisher) {
    if (this.publisher != null) {
      throw new IllegalArgumentException("Publisher already set");
    }
    this.publisher = publisher;
  }

  public String getPublisher() {
    return this.publisher;
  }
  
  // origCluster
  public ClusterIdentifier getSource() {
    return source;
  }
  
  public void setSource(ClusterIdentifier source) {
    if (this.source != null) {
      throw new IllegalArgumentException("Source already set");
    }
    this.source = source;
    
  }
  
  public void setRegarding(PlanElement pe) {
    if (this.pe != null) {
      throw new IllegalArgumentException("Regarding already set");
    }
    this.pe = pe;
  }

  public void setTime(long time) {
    if (this.time != 0) {
      throw new IllegalArgumentException("Time already set");
    }
    this.time = time;
  }
  
  // Publishable gives us boolean isPersistable(), for which for now we can say no
  public boolean isPersistable() { return false; }
  
  // UniqueObject gives UID getUID() and void setUID(UID uid)
  public UID getUID() {
    return id;
  }

  public void setUID(UID uid) {
    if (this.id != null) {
      throw new IllegalArgumentException("UID already set");
    }
    this.id = uid;
  }

  /**
   * Give a short (25-35) character description of the Event,
   * one which does not include the Class, times, or ID.
   * For use by the UI.
   *
   * @return a <code>String</code> description
   */
  public String getDescription() {
    String foo = "null";
    if (pe != null) {
      // What kind of Plan Element was it?
      // Was it successful?
      if (pe instanceof Allocation) {
	foo = "Allocation";
      } else if (pe instanceof Expansion) {
	foo = "Expansion";
      } else if (pe instanceof Disposition) {
	foo = "Disposition";
      } else if (pe instanceof Aggregation) {
	foo = "Aggregation";
      } else if (pe instanceof AssetTransfer) {
	foo = "Asset Transfer";
      }
      Task pet = pe.getTask();
      if (pet != null) {
	String tdesc = pet.getVerb().toString();
	if (tdesc != null) {
	  foo = foo + " of " + tdesc + " Task";
	}
      }
      AllocationResult ar = pe.getReportedResult();
      if (ar != null) {
	if (ar.isSuccess()) {
	  foo = foo + " successfull";
	} else {
	  foo = foo + " failed";
	}
	double conf = ar.getConfidenceRating();
	if (conf > 0.5) {
	  foo = foo + " w/ confidence";
	} else {
	  foo = foo + " w/o confidence";
	}
	
	String desc = ar.auxiliaryQuery(AuxiliaryQueryType.FAILURE_REASON);
	if (desc != null) {
	  foo = foo + " cause " + desc;
	}
      }
    }
    
    return "Deadline (" +
      foo + 
      ")";
  }
  
  public String toString() {
    StringBuffer buf = new StringBuffer();
    buf.append("<DeadlineTimerEvent ");
    buf.append(getUID().toString());
    buf.append(" Time: ");
    buf.append(getTime());
    if(getRegarding() != null) {
      buf.append(" Regarding ");
      buf.append(getRegarding().toString());
    }
    buf.append(">");

    return buf.toString();
  }
} // end of DeadlineTimerEventImpl

