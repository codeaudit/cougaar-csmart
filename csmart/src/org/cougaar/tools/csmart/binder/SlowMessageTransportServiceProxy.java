/* 
 * <copyright>
 * This software is to be used only in accordance with the COUGAAR license
 * agreement. The license agreement and other information can be found at
 * http://www.cougaar.org
 * 
 *       © Copyright 2001 by BBNT Solutions LLC.
 * </copyright>
 */
package org.cougaar.tools.csmart.binder;

import java.util.ArrayList;
import java.util.List;

import org.cougaar.core.mts.*;
import org.cougaar.core.society.Message;
import org.cougaar.core.society.MessageAddress;


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

  private MessageTransportClient mtc;
  private MessageTransportService mt;
  
  private MessageTransportClient wrappedClient;

  private MessageReleaseScheduler mrs;

  public SlowMessageTransportServiceProxy(
      MessageTransportService mt,
      Object requestor,
      MessageReleaseScheduler mrs) {
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
      if (VERBOSE) {
        System.err.println(
            "Expecting the service-requestor to be the client (OK)");
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

      while (true) {

        if (VERBOSE) {
          System.out.println("+ getIO");
        }
        mrs.getDueMessages(inBuf, outBuf);

        if (VERBOSE) {
          System.out.println(
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
