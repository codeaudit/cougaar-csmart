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

package org.cougaar.tools.csmart.runtime.ldm;

import java.util.*;
import java.io.InputStream;

import org.cougaar.core.agent.ClusterContext;
import org.cougaar.core.agent.ClusterIdentifier;
import org.cougaar.core.agent.ClusterServesPlugIn;
import org.cougaar.core.service.UIDServer;
import org.cougaar.core.util.UID;
import org.cougaar.core.domain.Factory;
import org.cougaar.core.domain.RootFactory;
import org.cougaar.core.domain.LDMServesPlugIn;
import org.cougaar.planning.ldm.asset.*;

import org.cougaar.tools.csmart.runtime.ldm.asset.*;
import org.cougaar.tools.csmart.runtime.ldm.event.*;
import org.cougaar.tools.csmart.runtime.plugin.CSMARTPlugIn;
import org.cougaar.tools.csmart.util.*;

/**
 * The CSMART Factory.
 * It creates Events and other CSMART specific objects.<br>
 * It also provides the common logger for CSMART components, and starts it.
 * @see LogStream
 **/
public class CSMARTFactory
    implements Factory {

  protected ClusterIdentifier selfClusterId;
  protected UIDServer myUIDServer;

  protected LogStream logStream;

  /**
   * @see #getNumberOfEvents()
   */
  protected long nEvents;

  /**
   * Constructor for use by domain specific Factories
   * extending this class
   */
  public CSMARTFactory() { }
  
  public CSMARTFactory(LDMServesPlugIn ldm) {
    // attach our CSMART domain factories to the root factory
    RootFactory rf = ldm.getFactory();
    rf.addAssetFactory(
        new org.cougaar.tools.csmart.runtime.ldm.asset.AssetFactory());
    rf.addPropertyGroupFactory(
        new org.cougaar.tools.csmart.runtime.ldm.asset.PropertyGroupFactory());

    logStream = new LogStreamImpl();

    ClusterServesPlugIn cspi = (ClusterServesPlugIn)ldm;
    selfClusterId = cspi.getClusterIdentifier();
    myUIDServer = ((ClusterContext)ldm).getUIDServer();

    // Start the logging Stream.
    logStream.start();

  }

  // Assets next
  // Other misc objects go here

  /**
   * @return a new <code>UID</code>
   */
  public UID getNextUID() {
    return myUIDServer.nextUID();
  }

  /**
   * Get the next UID and increment the <code>Event</code> counter.
   *
   * @return a new <code>UID</code>
   */
  public UID getNextEventUID() {
    ++nEvents;
    return myUIDServer.nextUID();
  }

  /**
   * Get the <code>LogStream</code>.
   **/
  public LogStream getLogStream() {
     return logStream;
  }

  /**
   * Get the total number of <code>Event</code>s created by this 
   * factory.
   * <p>
   * Note that this counter is not rollback-aware, and therefore
   * <code>PlugIn</code>s should not examine it!
   */
  public long getNumberOfEvents() {
    return nEvents;
  }

  // Events

  /**
   * Creates a new <code>HappinessChangeEvent</code> that is issued
   * by the Customer to inform whoever cares that its happiness has changed.
   * <br>
   *
   */
  public NewHappinessChangeEvent newHappinessChangeEvent() {
     return new HappinessChangeEventImpl(getNextEventUID());
  }

  /**
   * Creates a new <code>DeadlineTimerEvent</code> used by a PlugIn to
   * inform itself that a deadline has excceeded.
   * <br>
   *
   */
  public NewDeadlineTimerEvent newDeadlineTimerEvent() {
     return new DeadlineTimerEventImpl(getNextEventUID());
  }

  /**
   * Creates a new <code>CyberAttackEvent</code> with which to afflict
   * the simulated system
   * <br>
   *
   */
    public NewCyberAttackEvent newCyberAttackEvent() {
      return new CyberAttackEventImpl(getNextEventUID());
    }

  /**
   * Creates a new <code>SimpleKEvent</code> with which to afflict
   * the simulated system
   * <br>
   *
   */
  public NewSimpleKEvent newSimpleKEvent() {
    return new SimpleKEventImpl(getNextEventUID());
  }

  /**
   * Create a new InfrastructureEvent specifying the specific impacts on an Agent
   *
   * @return a <code>NewInfrastructureEvent</code>
   */
  public NewInfrastructureEvent newInfrastructureEvent() {
    return new InfrastructureEventImpl(getNextEventUID());
  }

  /**
   * Create a new InfrastructureEvent with the given values to specifically
   * impact an Agent.
   *
   * @param dest a <code>ClusterIdentifier</code> Agent to impact
   * @param type a <code>String</code> constant from <code>org.cougaar.tools.csmart.Constants.InfEventType</code>
   * @param duration a <code>long</code> impact duration
   * @param intensity a <code>double</code> magnitude from 0 to 1
   * @return a <code>NewInfrastructureEvent</code>
   */
  public NewInfrastructureEvent newInfrastructureEvent(ClusterIdentifier dest,
                   String type,
                   long duration,
                   double intensity) {
    return new InfrastructureEventImpl(getNextEventUID(),
               dest,
               type,
               duration,
               intensity);
  }

//    /**
//     * Create a new SnifferEvent
//     *
//     * @return a <code>NewSnifferEvent</code>
//     */
//    public NewSnifferEvent newSnifferEvent() {
//      return new SnifferEventImpl(getNextEventUID());
//    }

//    /**
//     * Create a new SnifferEvent with the given values
//     *
//     * @param dest a <code>String</code> the Agent to report to
//     * @param startTime the time the collection period started
//     * @param endTime the time the collection period ended
//     * @return a <code>NewSnifferEvent</code>
//     */
//    public NewSnifferEvent newSnifferEvent(String dest,
//                     long startTime,
//                     long endTime) {
//      return new SnifferEventImpl(getNextEventUID(),
//                  dest, startTime, endTime);
//    }

} // end of CSMARTFactory.java


