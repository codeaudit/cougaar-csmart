/* 
 * <copyright>
 *  Copyright 2001-2002 BBNT Solutions, LLC
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
package org.cougaar.tools.csmart.runtime.binder;

import java.util.List;

import org.cougaar.core.component.*;
import org.cougaar.core.mts.*;
import org.cougaar.core.agent.*;
import org.cougaar.core.domain.*;
import org.cougaar.core.blackboard.*;
import org.cougaar.core.mts.*;
import org.cougaar.core.node.*;
import org.cougaar.core.mts.*;
import org.cougaar.core.service.MessageTransportService;
import org.cougaar.tools.csmart.ui.viewer.CSMART;
import org.cougaar.util.log.Logger;

/**
 * A <code>ServiceFilter</code> to wrap the <code>MessageTransportService</code> 
 * and simulate a degradation in I/O capacity (via increased-latency).
 * <p>
 * Specify in the Node.ini file as:<pre>
 *   Node.AgentManager = org.cougaar.tools.csmart.runtime.binder.SlowMessageTransportServiceFilter(X, Y, Z)
 * </pre>
 * See <tt>setParameter(..)</tt> for the required parameters.
 *
 * @see #setParameter(Object) required parameters for this Component
 *
 * @see SlowMessageTransportServiceProxyController controller API for further
 *    I/O degradation
 */
public class SlowMessageTransportServiceFilter 
  extends ServiceFilter 
{
  private double samplesPerSecond;
  private double inMessagesPerSecond;
  private double outMessagesPerSecond;
  private transient Logger log;

  public SlowMessageTransportServiceFilter() {
    createLogger();
    if(log.isInfoEnabled()) {
      log.info("\n\n SlowMT created\n\n");
    }
  }

  private void createLogger() {
    log = CSMART.createLogger(this.getClass().getName());
  }

  protected Class getBinderClass(Object child) {
    return SlowMessageTransportServiceFilterBinder.class;
  }

  /**
   * There are three necessary parameters:<ol>
   *   <li>a String double for     samplesPerSecond</li>
   *   <li>a String double for  inMessagesPerSecond</li>
   *   <li>a String double for outMessagesPerSecond</li>
   * </ol>.
   * <p>
   * For example, a very slow MessageTransport might use "(1, 0.5, 0.5)" to 
   * send and receive a message every other second.  More typical values
   * might be "(2, 10, 10)".
   * <p>
   * The Container calls this method when loading this ServiceFilter.
   *
   * @param o a List matching the above specification
   */
  public void setParameter(Object o) {
    try {
      List l = (List)o;
      samplesPerSecond = Double.parseDouble((String)l.get(0));
      inMessagesPerSecond = Double.parseDouble((String)l.get(1));
      outMessagesPerSecond = Double.parseDouble((String)l.get(2));
    } catch (Exception e) {
      throw new IllegalArgumentException(
          "SlowMessageTransportServiceFilter expecting three arguments:\n"+
          "  double samplesPerSecond,\n"+
          "  double inMessagesPerSecond,\n"+
          "  double outMessagesPerSecond\n"+
          "e.g. (2, 10, 10)");
    }
  }

  /**
   * Wrap a <code>MessageTransportService</code>.
   */
  public SlowMessageTransportServiceProxy createSlowMessageTransportServiceProxy(
      MessageTransportService origMT,
      Object requestor) {

    if(log.isInfoEnabled()) {
      log.info("\n\nCreate slow MT for "+requestor+"\n\n");
    }

    // create a new wrapped MessageTransportService
    //
    // requestor should be an Agent
    MessageReleaseScheduler mrs = 
      new MessageReleaseSchedulerImpl(
          samplesPerSecond, 
          inMessagesPerSecond, 
          outMessagesPerSecond);
    return
      new SlowMessageTransportServiceProxy(
          origMT,
          requestor,
          mrs);
  }

  //
  // Lots of Container guts...
  //

  public static class SlowMessageTransportServiceFilterBinder 
    extends ServiceFilterBinder 
  {
    private SlowMessageTransportServiceFilter 
      SlowMessageTransportServiceFilter_this;

    public SlowMessageTransportServiceFilterBinder(
        BinderFactory bf, 
        Object child) {
      super(bf, child);
      SlowMessageTransportServiceFilter_this = 
        (SlowMessageTransportServiceFilter)bf;
    }

    protected ContainerAPI createContainerProxy() {
      return new SlowMessageTransportFilteringBinderProxy();
    }

    protected final AgentManagerForBinder getAgentManager() {
      return (AgentManagerForBinder) getContainer();
    }

    protected class SlowMessageTransportFilteringBinderProxy 
      extends ServiceFilterContainerProxy
      implements AgentManagerForBinder, ClusterManagementServesCluster
    {
      public String getName() {
        return getAgentManager().getName();
      }

      public void registerAgent(Agent agent) {
        getAgentManager().registerAgent(agent);
      }
    }


    protected ServiceBroker createFilteringServiceBroker(ServiceBroker sb) {
      return new SlowMessageTransportFilteringServiceBroker(sb);
    }

    protected class SlowMessageTransportFilteringServiceBroker
      extends FilteringServiceBroker 
    {
      private SlowMessageTransportServiceProxy smt;
      private transient Logger log;

      public SlowMessageTransportFilteringServiceBroker(ServiceBroker sb) {
        super(sb);
        log = CSMART.createLogger(this.getClass().getName());
        if (log.isDebugEnabled()) {
          log.debug("created smtfsb with "+sb);
        }
      }

      public Object getService(
          Object requestor, 
          Class serviceClass,
          ServiceRevokedListener srl) {
        if (serviceClass == SlowMessageTransportServiceProxyController.class) {
          if (log.isDebugEnabled()) {
            log.debug("lookup controller!");
          }
          // request for the Controller API for our wrapped service
          return smt;
        } else {
          if (log.isDebugEnabled()) {
            log.debug("smtfsb.askSuper for "+serviceClass);
          }
          return super.getService(requestor, serviceClass, srl);
        }
      }
      protected Object getServiceProxy(
          Object service, 
          Class serviceClass, 
          Object client) {
        if (log.isDebugEnabled()) {
          log.debug("getServiceProxy");
        }
        if (service instanceof MessageTransportService) {
          if (log.isDebugEnabled()) {
            log.debug("get mt service");
          }
          if (smt == null) {
            // create a new wrapped MessageTransportService
            this.smt = 
              SlowMessageTransportServiceFilter_this.createSlowMessageTransportServiceProxy(
                  (MessageTransportService)service,
                  client);
          }
          return smt;
        } else {
          if (log.isDebugEnabled()) {
            log.debug("lack other service: "+serviceClass);
          }
        }
        return null;
      }
    }
  }

}
