/* 
 * <copyright>
 *  Copyright 2000-2002 BBNT Solutions, LLC
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

package org.cougaar.tools.csmart.runtime.plugin;

import org.cougaar.core.service.DomainService;
import org.cougaar.core.domain.RootFactory;

import org.cougaar.core.service.BlackboardService;
import org.cougaar.core.component.ServiceRevokedListener;
import org.cougaar.core.component.ServiceRevokedEvent;
import org.cougaar.core.blackboard.Subscription;
import org.cougaar.core.plugin.ComponentPlugin;
import org.cougaar.core.util.UID;

import org.cougaar.tools.csmart.util.*;

import org.cougaar.util.StateModelException;
import org.cougaar.util.UnaryPredicate;
import org.cougaar.core.service.LoggingService;

/**
 * CSMART Base Plugin. Provide convenience methods.<br>
 * Get a hook to the <code>RootFactory</code>
 */
public abstract class CSMARTPlugin
    extends ComponentPlugin
{

  //
  // CSMART hooks
  //

  private DomainService domainService = null;
  protected LoggingService log = null;

  /** Root LDM Factory */
  protected RootFactory theLDMF;

  /** Identifier of Plugin */
  private UID id = null;

  
  // 
  // constructor
  //
  public CSMARTPlugin() {
  }

  /**
   * Get the UID for this Plugin (same as its PluginAsset)
   * Only to be called after the Plugin has been loaded
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

    log = (LoggingService)
      getServiceBroker().getService(this, LoggingService.class, null);

    if (log == null) {
      // Print to STDERR?
      log = LoggingService.NULL;    
    }

    // Also get the Root factory
    this.theLDMF =
      ((domainService != null) ?
       ((RootFactory)domainService.getFactory()) : null);
    
    // Give each Plugin a UID to uniquely identify it
    this.id = theLDMF.getNextUID();

  } // end of load()

  // A couple convenience functions to provide backwards compatibility
  protected Subscription subscribe(UnaryPredicate pred) {
    return getBlackboardService().subscribe(pred);
  }
  
  //
  // Timer related functions
  //

  //
  // LogPlan changes publishing
  //

  // A convenience backwards-compatibility method
  protected final void publishAdd(Object o) {
    getBlackboardService().publishAdd(o);
  }

  protected final void publishAdd(Object o, UnaryPredicate pred) {
    if ((pred == null) ||
        (pred.execute(o))) {
      getBlackboardService().publishAdd(o);
    } else {
      // pred cancelled the add
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

  /** Called during initialization to set up subscriptions.
   * More precisely, called in the plugin's Thread of execution
   * inside of a transaction before execute will ever be called.
   **/
  protected abstract void setupSubscriptions();

  /**
   * Called inside of an open transaction whenever the plugin was
   * explicitly told to run or when there are changes to any of
   * our subscriptions.
   **/
  protected abstract void execute();

} // end of CSMARTPlugin.java

