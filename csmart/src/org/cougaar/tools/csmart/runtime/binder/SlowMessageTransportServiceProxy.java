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

import java.util.ArrayList;
import java.util.List;

import org.cougaar.core.mts.*;
import org.cougaar.core.mts.Message;
import org.cougaar.core.mts.MessageAddress;
import org.cougaar.core.service.MessageTransportService;
import org.cougaar.util.log.Logger;
import org.cougaar.tools.csmart.ui.viewer.CSMART;


/**
 * A <code>MessageTransportService</code> that uses a 
 * <code>MessageReleaseScheduler</code> to buffer and periodically 
 * release <code>Message</code>s after a customizable delay.
 *
 * @see MessageReleaseScheduler
 */
public class SlowMessageTransportServiceProxy 
  implements MessageTransportService, 
             SlowMessageTransportServiceProxyController {

  private static final boolean VERBOSE = false;
  private transient Logger log;

  private MessageTransportClient mtc;
  private MessageTransportService mt;
  
  private MessageTransportClient wrappedClient;

  private MessageReleaseScheduler mrs;

  public SlowMessageTransportServiceProxy(
      MessageTransportService mt,
      Object requestor,
      MessageReleaseScheduler mrs) {
    createLogger();
    this.mt = mt;
    this.mtc = 
      ((requestor instanceof MessageTransportClient) ? 
       ((MessageTransportClient)requestor) :
       null);
    this.mrs = mrs;

    // spawn our release-thread
    ReleaseRunner rr = new ReleaseRunner();
    Thread rt = new Thread(rr);
    rt.start();

  }

  private void createLogger() {
    log = CSMART.createLogger(this.getClass().getName());
  }

  public void degradeReleaseRate(
      double factor,
      long duration) {
    mrs.degradeReleaseRate(factor, duration);
  }

  public void sendMessage(Message m) {
    mrs.sendMessage(m);
  }

  public void registerClient(final MessageTransportClient mtc) {
    if (mtc != this.mtc) {
      if(log.isErrorEnabled()) {
        log.error("Expecting the service-requestor to be the client (OK)");
      }
    }
    this.wrappedClient = 
      new MessageTransportClient() {
        public void receiveMessage(Message m) {
          mrs.receiveMessage(m);
        }
        public MessageAddress getMessageAddress() {
          return mtc.getMessageAddress();
        }
      };
    mt.registerClient(wrappedClient);
  }

  public void unregisterClient(MessageTransportClient client) {
    // FIXME!!!!
    if (client == this.mtc)
      mt.unregisterClient(wrappedClient);
  }
  
  public ArrayList flushMessages() {
    // FIXME!!!!
    return mt.flushMessages();
  }
  
  public String getIdentifier() {
    return mt.getIdentifier();
  }

  public boolean addressKnown(MessageAddress a) {
    return mt.addressKnown(a);
  }

  class ReleaseRunner implements Runnable {
    public void run() {

      // in/out buffers of Messages
      List inBuf = new ArrayList();
      List outBuf = new ArrayList();
      Logger log = 
        CSMART.createLogger(this.getClass().getName());

      while (true) {

        if (log.isDebugEnabled()) {
          log.debug("+ getIO");
        }
        mrs.getDueMessages(inBuf, outBuf);

        if (log.isDebugEnabled()) {
          log.debug(
              "+ sendIO(in: "+inBuf.size()+", out:"+outBuf.size()+")");
        }
        int nIn = inBuf.size();
        if (nIn > 0) {
          for (int i = 0; i < nIn; i++) {
            mtc.receiveMessage((Message)inBuf.get(i));
          }
          inBuf.clear();
        }
        int nOut = outBuf.size();
        if (nOut > 0) {
          for (int i = 0; i < nOut; i++) {
            mt.sendMessage((Message)outBuf.get(i));
          }
          outBuf.clear();
        }
      }
    }
  }


}
