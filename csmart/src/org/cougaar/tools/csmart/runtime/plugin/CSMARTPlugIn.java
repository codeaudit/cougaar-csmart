/* 
 * <copyright>
 *  Copyright 2000-2001 BBNT Solutions, LLC
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

package org.cougaar.tools.csmart.plugin;

import org.cougaar.core.service.DomainService;
import org.cougaar.core.domain.RootFactory;

import org.cougaar.core.service.BlackboardService;
import org.cougaar.core.component.ServiceRevokedListener;
import org.cougaar.core.component.ServiceRevokedEvent;
import org.cougaar.core.agent.service.alarm.Alarm;
import org.cougaar.core.blackboard.Subscription;
import org.cougaar.core.agent.ClusterIdentifier;
import org.cougaar.core.plugin.ComponentPlugin;
import org.cougaar.core.util.UID;

import org.cougaar.tools.csmart.runtime.ldm.CSMARTFactory;
import org.cougaar.tools.csmart.util.*;

import org.cougaar.util.StateModelException;
import org.cougaar.util.UnaryPredicate;

/**
 * CSMART Base PlugIn. Provide support for publishing after a delay and convenience methods.<br>
 * Get a hook to the <code>RootFactory</code> and the <code>CSMARTFactory</code>
 */
public abstract class CSMARTPlugIn
    extends ComponentPlugin
{

  /**
   * Minimum delay used in publish[Add|Change|Remove][At|After].
   *
   * Could be controlled by system property and/or cluster's timer.
   */
  protected static final long MINIMUM_DELAY_MILLIS = 100;

  //
  // CSMART hooks, i.e. CSMARTFactory, etc
  //

  private DomainService domainService = null;

  /** CSMART factory */
  protected CSMARTFactory theCSMARTF;

  /** Root LDM Factory */
  protected RootFactory theLDMF;

  /** Identifier of PlugIn */
  private UID id = null;
  
  /** Log output for this PlugIn */
  protected LogStream log;
  
  // 
  // constructor
  //
  public CSMARTPlugIn() {
  }

  /**
   * Get the UID for this PlugIn (same as its PlugInAsset)
   * Only to be called after the PlugIn has been loaded
   *
   * @return an <code>UID</code> value
   */
  public UID getUID() {
    return this.id;
  }

  //
  // load extensions
  //

  /** Watch for who we are plugging into **/
  public void load() throws StateModelException {
    // FIXME!!! What about ThreadingChoice??
    super.load();
    domainService = (DomainService)
      getServiceBroker().getService(this, DomainService.class, 
				    new ServiceRevokedListener() {
					public void serviceRevoked(ServiceRevokedEvent re) {
					  if (DomainService.class.equals(re.getService()))
					    domainService  = null;
					}
				      });        
    // get the CSMART factory
    this.theCSMARTF = 
      ((domainService != null) ?
       ((CSMARTFactory)domainService.getFactory("csmart"))  :
       null);
    
    // Also get the Root factory
    this.theLDMF =
      ((domainService != null) ?
       ((RootFactory)domainService.getFactory()) : null);
    
    if (this.theCSMARTF == null) {
      throw new RuntimeException(
        "Loaded plugin "+this+" without CSMART Factory? Did you include the csmart domain in configs/common/LDMDomains.ini?");
    }
    
    // Give each PlugIn a UID to uniquely identify it
    this.id = theCSMARTF.getNextUID();
    this.log = theCSMARTF.getLogStream();
  } // end of load()

  // A couple convenience functions to provide backwards compatibility
  protected Subscription subscribe(UnaryPredicate pred) {
    return getBlackboardService().subscribe(pred);
  }
  
  protected ClusterIdentifier getAgentIdentifier() {
    return getBindingSite().getAgentIdentifier();
  }
  
  //
  // Timer related functions
  //

  /**
   * Wait for <tt>delayMillis</tt> milliseconds of execution time to pass, 
   * during which time the PlugIn and all other PlugIns in the Cluster will
   * wait.
   *
   * In CSMART only the PSP server runs in it's own Thread, so this will
   * halt all PlugIn activity!
   */
  protected void waitMillis(long delayMillis) {
    if (delayMillis <= MINIMUM_DELAY_MILLIS) {
      // no wait required
    } else {
      SyncAlarm sa = 
        new SyncAlarm(
            (currentTimeMillis() + delayMillis));
      alarmService.addAlarm(sa);
      //      getAlarmService().addAlarm(sa);
      synchronized (sa) {
        try {
          sa.wait();
        } catch (InterruptedException e) {
        }
      }
    }
  }

  //
  // LogPlan changes publishing
  //

  // A convenience backwards-compatibility method
  protected final void publishAdd(Object o) {
    getBlackboardService().publishAdd(o);
  }

  /**
   * Equivalent to
   *   <tt>publishAddAfter(o, 0, pred)</tt>.
   *
   * @see #publishAddAfter(Object,long,UnaryPredicate)
   */
  protected final void publishAdd(Object o, UnaryPredicate pred) {
    if ((pred == null) ||
        (pred.execute(o))) {
      getBlackboardService().publishAdd(o);
    } else {
      // pred cancelled the add
    }
  }

  /**
   * @see #publishAddAt(Object,long,UnaryPredicate)
   */
  protected final void publishAddAt(Object o, long timeMillis) {
    publishAddAt(o, timeMillis, null);
  }

  /**
   * Equivalent to
   *   <tt>publishAddAfter(o, (timeMillis - currentTimeMillis()), pred)</tt>.
   *
   * @see #publishAddAfter(Object,long,UnaryPredicate)
   */
  protected final void publishAddAt(
      Object o, 
      long timeMillis, 
      UnaryPredicate pred) {
    publishAddAfter(o, (timeMillis - currentTimeMillis()), pred);
  }

  /**
   * @see #publishAddAfter(Object,long,UnaryPredicate)
   */
  protected final void publishAddAfter(Object o, long delayMillis) {
    publishAddAfter(o, delayMillis, null);
  }

  /**
   * Publish-Add the given Object <tt>o</tt> to the LogPlan after waiting 
   * at least <tt>delayMillis</tt> milliseconds of execution time, but 
   * allow this <code>PlugIn</code> to continue it's execution during 
   * this wait.
   * <p>
   * The Plugin can also specify a <code>UnaryPredicate</code> to run
   * just before the action will take place.  This <tt>pred</tt> can
   * be used to delay a modification to the Object <tt>o</tt> and/or to
   * cancel the ADD operation.
   * 
   * @param o the Object to add
   * @param delayMillis the number of milliseconds in the future for the ADD
   * @param pred an optional UnaryPredicate that will ".execute(o)" just 
   *   before the future ADD time
   *
   * @see #waitMillis(long)
   */
  protected final void publishAddAfter(
      Object o, 
      long delayMillis, 
      UnaryPredicate pred) {
    if (delayMillis <= MINIMUM_DELAY_MILLIS) {
      // immediate add
      publishAdd(o, pred);
    } else {
      // add after at least "delayMillis" have passed
      DelayPublishAlarm dpa = 
        new DelayPublishAlarm(
            DelayPublishAlarm.PUBLISH_ADD,
            o,
            (currentTimeMillis() + delayMillis), 
            pred);
      alarmService.addAlarm(dpa);
    }
  }

  // Convenience backwards-compatibility
  protected final void publishChange(Object o) {
    getBlackboardService().publishChange(o);
  }
  
  /**
   * @see #publishAdd(Object,UnaryPredicate)
   */
  protected final void publishChange(Object o, UnaryPredicate pred) {
    if ((pred == null) ||
        (pred.execute(o))) {
      getBlackboardService().publishChange(o);
    } else {
      // pred cancelled the change
    }
  }

  /**
   * @see #publishAddAt(Object,long)
   */
  protected final void publishChangeAt(Object o, long timeMillis) {
    publishChangeAt(o, timeMillis, null);
  }

  /**
   * @see #publishAddAt(Object,long,UnaryPredicate)
   */
  protected final void publishChangeAt(
      Object o, 
      long timeMillis, 
      UnaryPredicate pred) {
    publishChangeAfter(o, (timeMillis - currentTimeMillis()), pred);
  }

  /**
   * @see #publishAddAfter(Object,long)
   */
  protected final void publishChangeAfter(Object o, long delayMillis) {
    publishChangeAfter(o, delayMillis, null);
  }

  /**
   * @see #publishAddAfter(Object,long,UnaryPredicate)
   */
  protected final void publishChangeAfter(
      Object o,
      long delayMillis,
      UnaryPredicate pred) {
    if (delayMillis <= MINIMUM_DELAY_MILLIS) {
      // immediate change
      publishChange(o, pred);
    } else {
      // change after at least "delayMillis" have passed
      DelayPublishAlarm dpa = 
        new DelayPublishAlarm(
            DelayPublishAlarm.PUBLISH_CHANGE,
            o,
            (currentTimeMillis() + delayMillis),
            pred);
      alarmService.addAlarm(dpa);
    }
  }

  // Convenience
  protected final void publishRemove(Object o) {
    getBlackboardService().publishRemove(o);
  }

  /**
   * @see #publishAdd(Object,UnaryPredicate)
   */
  protected final void publishRemove(Object o, UnaryPredicate pred) {
    if ((pred == null) ||
        (pred.execute(o))) {
      getBlackboardService().publishRemove(o);
    } else {
      // pred cancelled the remove
    }
  }

  /**
   * @see #publishAddAt(Object,long)
   */
  protected final void publishRemoveAt(Object o, long timeMillis) {
    publishRemoveAt(o, timeMillis, null);
  }

  /**
   * @see #publishAddAt(Object,long,UnaryPredicate)
   */
  protected final void publishRemoveAt(
      Object o, 
      long timeMillis,
      UnaryPredicate pred) {
    publishRemoveAfter(o, (timeMillis - currentTimeMillis()), pred);
  }

  /**
   * @see #publishAddAfter(Object,long)
   */
  protected final void publishRemoveAfter(Object o, long delayMillis) {
    publishRemoveAfter(o, delayMillis, null);
  }

  /**
   * @see #publishAddAfter(Object,long,UnaryPredicate)
   */
  protected final void publishRemoveAfter(
      Object o, 
      long delayMillis,
      UnaryPredicate pred) {
    if (delayMillis <= MINIMUM_DELAY_MILLIS) {
      // immediate remove
      publishRemove(o, pred);
    } else {
      // remove after at least "delayMillis" have passed
      DelayPublishAlarm dpa = 
        new DelayPublishAlarm(
            DelayPublishAlarm.PUBLISH_REMOVE,
            o,
            (currentTimeMillis() + delayMillis),
            pred);
      alarmService.addAlarm(dpa);
    }
  }

  /////////////////////////////////////////
  // More alarm methods
  
  /**
   * <code>Alarm</code> used by <tt>waitMillis</tt>.
   */
  protected class SyncAlarm implements Alarm {

    private final long expTime;

    private boolean expired = false;

    public SyncAlarm(long expTime) {
      this.expTime = expTime;
      if (log.isApplicable(log.VERY_VERBOSE)) {
        String msg = 
          "@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@"+
          "\n Create \"waitMillis\" Alarm"+
          "\n PlugIn: "+CSMARTPlugIn.this.toString()+
          "\n currTime: "+currentTimeMillis()+
          "\n expTime:  "+expTime+
          "\n notify:  "+super.toString()+
          "\n @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@";
        log.log(this.getClass().getName(), log.VERY_VERBOSE, msg);
      }
    }

    public long getExpirationTime() { 
      return expTime; 
    }

    public synchronized void expire() {
      if (!expired) {
        if (log.isApplicable(log.VERY_VERBOSE)) {
          String msg = 
            "@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@"+
            "\n Expire \"waitMillis\" Alarm"+
            "\n PlugIn: "+CSMARTPlugIn.this.toString()+
            "\n currTime: "+currentTimeMillis()+
            "\n expTime:  "+expTime+
            "\n notify:  "+super.toString()+
            "\n @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@";
          log.log(this.getClass().getName(), log.VERY_VERBOSE, msg);
        }
        expired = true;
        this.notify();
      }
    }

    public boolean hasExpired() { 
      return expired; 
    }

    public synchronized boolean cancel() {
      boolean was = expired;
      expired = true;
      return was;
    }

    public String toString() {
      return 
        "<SyncAlarm ("+super.toString()+
        ") at time "+expTime+
        (expired?" (Expired)":"")+
        " for "+CSMARTPlugIn.this.toString()+">";
    }
  }

  /**
   * <code>Alarm</code> used by<ul>
   *   <li><tt>publishAddAt(Object,long)</tt></li>
   *   <li><tt>publishAddAt(Object,long,UnaryPredicate)</tt></li>
   *   <li><tt>publishAddAfter(Object,long)</tt></li>
   *   <li><tt>publishAddAfter(Object,long,UnaryPredicate)</tt></li>
   *   <li><tt>publishChangeAt(Object,long)</tt></li>
   *   <li><tt>publishChangeAt(Object,long,UnaryPredicate)</tt></li>
   *   <li><tt>publishChangeAfter(Object,long)</tt></li>
   *   <li><tt>publishChangeAfter(Object,long,UnaryPredicate)</tt></li>
   *   <li><tt>publishRemoveAt(Object,long)</tt></li>
   *   <li><tt>publishRemoveAt(Object,long,UnaryPredicate)</tt></li>
   *   <li><tt>publishRemoveAfter(Object,long)</tt></li></ul>
   *   <li><tt>publishRemoveAfter(Object,long,UnaryPredicate)</tt></li></ul>.
   *
   * @see #publishAddAfter(Object,long,UnaryPredicate)
   */
  protected class DelayPublishAlarm implements Alarm {

    private static final byte EXPIRED        = 0;
    public  static final byte PUBLISH_ADD    = 1;
    public  static final byte PUBLISH_CHANGE = 2;
    public  static final byte PUBLISH_REMOVE = 3;
    private byte pubStyle;

    private final Object o;

    private final long expTime;

    private final UnaryPredicate pred;

    public DelayPublishAlarm(
        byte pubStyle,
        Object o,
        long expTime,
        UnaryPredicate pred) {
      this.pubStyle = pubStyle;
      this.o = o;
      this.expTime = expTime;
      this.pred = pred;
      if (log.isApplicable(log.VERY_VERBOSE)) {
        String msg = 
          "#################################"+
          "\n Create \"publish"+
          ((pubStyle == PUBLISH_ADD) ?
           "Add":
           ((pubStyle == PUBLISH_CHANGE) ?
            "Change":
            ((pubStyle == PUBLISH_REMOVE) ?
             "Remove":
             "???")))+
          "\" Alarm"+
          "\n PlugIn: "+CSMARTPlugIn.this.toString()+
          "\n currTime: "+currentTimeMillis()+
          "\n expTime:  "+expTime+
          "\n pred:  "+pred+
          "\n Object:  "+o.toString()+
          "\n #################################";
        log.log(this.getClass().getName(), log.VERY_VERBOSE, msg);
      }
    }

    public long getExpirationTime() { 
      return expTime; 
    }

    public synchronized void expire() {
      if (pubStyle != EXPIRED) {
        if (log.isApplicable(log.VERY_VERBOSE)) {
          String msg = 
            "#################################"+
            "\n Release \"publish"+
            ((pubStyle == PUBLISH_ADD) ?
             "Add":
             ((pubStyle == PUBLISH_CHANGE) ?
              "Change":
              ((pubStyle == PUBLISH_REMOVE) ?
               "Remove":
               "???")))+
            "\" Alarm"+
            "\n PlugIn: "+CSMARTPlugIn.this.toString()+
            "\n currTime: "+currentTimeMillis()+
            "\n expTime:  "+expTime+
            "\n Object:  "+o.toString()+
            "\n #################################";
          log.log(this.getClass().getName(), log.VERY_VERBOSE, msg);
        }
//  	if (log.isApplicable(log.VERY_VERBOSE)) {
//  	  log.log(this, log.VERY_VERBOSE, "expire: " + this + " about to get blackboard service");
//  	}
        BlackboardService bbs = CSMARTPlugIn.this.getBlackboardService();
//  	if (log.isApplicable(log.VERY_VERBOSE)) {
//  	  log.log(this, log.VERY_VERBOSE, "expire: " + this + " got blackboard service");
//  	}
        try {
          bbs.openTransaction();
//  	  if (log.isApplicable(log.VERY_VERBOSE)) {
//  	    log.log(this, log.VERY_VERBOSE, "expire: " + this + " just got BBS transaction");
//  	  }
          // allow the user to alter and/or rethink the publish
          // 
          // must do the "pred.execute(o)" within the transaction
          if ((pred == null) ||
              (pred.execute(o))) {
            // do the publish
            switch (pubStyle) {
              case PUBLISH_ADD: 
                bbs.publishAdd(o);
                break;
              case PUBLISH_CHANGE:
                bbs.publishChange(o);
                break;
              case PUBLISH_REMOVE:
                bbs.publishRemove(o);
                break;
              default:
                // invalid style!
                break;
            }
          } else {
            // don't publish
          }
//  	  if (log.isApplicable(log.VERY_VERBOSE)) {
//  	    log.log(this, log.VERY_VERBOSE, "expire: " + this + " did publish");
//  	  }
        } finally {
          pubStyle = EXPIRED;
          bbs.closeTransaction();
//  	  if (log.isApplicable(log.VERY_VERBOSE)) {
//  	    log.log(this, log.VERY_VERBOSE, "expire: " + this + " closed transaction");
//  	  }
        }
      }
    }

    public boolean hasExpired() { 
      return (pubStyle == EXPIRED); 
    }

    public synchronized boolean cancel() {
      boolean tmp = (pubStyle == EXPIRED);
      pubStyle = EXPIRED;
      return tmp;
    }

    public String toString() {
      String s = "<";
      switch (pubStyle) {
        case EXPIRED:        s += "Expired";       break;
        case PUBLISH_ADD:    s += "PublishAdd";    break;
        case PUBLISH_CHANGE: s += "PublishChange"; break;
        case PUBLISH_REMOVE: s += "PublishRemove"; break;
        default:             s += "???";           break;
      }
      s += 
        " at time ("+expTime+") for "+
        CSMARTPlugIn.this.toString()+">";
      return s;
    }
  }

  //
  // From org.cougaar.core.plugin.SimplePlugIn
  //

  /** call initialize within an open transaction. **/
  protected final void prerun() {
    try {
      getBlackboardService().openTransaction();
      //      super.openTransaction();
      setupSubscriptions();
    } catch (Exception e) {
      synchronized (System.err) {
        System.err.println(getAgentIdentifier().toString()+"/"+this+" caught "+e);
        e.printStackTrace();
      }
    } finally {
      getBlackboardService().closeTransaction(false);
    }
  }

  /** Called during initialization to set up subscriptions.
   * More precisely, called in the plugin's Thread of execution
   * inside of a transaction before execute will ever be called.
   **/
  protected abstract void setupSubscriptions();

  /** Call execute in the right context.
   * Note that this transaction boundary does NOT reset
   * any subscription changes.
   * @See execute() documentation for details
   **/
  protected final void cycle() {
    //boolean doExecute = false; // Synonymous with resetTransaction
    try {
        getBlackboardService().openTransaction();
//      super.openTransaction();
	// FIXME!!! Replace this if block?
        //      if (wasAwakened() || 
        //getBlackboardService().haveCollectionsChanged()) {
        //doExecute = true;
        execute();
        //}
    } catch (Exception e) {
      synchronized (System.err) {
        System.err.println(getAgentIdentifier().toString()+"/"+this+" caught "+e);
        e.printStackTrace();
      }
      //doExecute = true;
    } finally {
      getBlackboardService().closeTransaction(true);
    }
  }


  /**
   * Called inside of an open transaction whenever the plugin was
   * explicitly told to run or when there are changes to any of
   * our subscriptions.
   **/
  protected abstract void execute();

} // end of CSMARTPlugIn.java

