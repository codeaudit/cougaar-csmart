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

import java.util.List;

import org.cougaar.core.society.Message;

/**
 * In/Out buffering and release API for the 
 * <code>SlowMessageTransportServiceProxy</code>.
 */
public interface MessageReleaseScheduler {

  /**
   * @see SlowMessageTransportServiceProxyController
   */
  public void degradeReleaseRate(
      double factor, 
      long duration);

  /**
   * Add an output message to the outgoing-queue.
   */
  public void sendMessage(Message m);

  /**
   * Add an input message to the ingoing-queue.
   */
  public void receiveMessage(Message m);

  /**
   * Get the I/O <code>Message</code>s that are now due for release.
   * <p>
   * This may block or return empty lists -- the MessageReleaseScheduler
   * implementation is free to spawn I/O watcher threads.
   * <p>
   * The user should just do<pre><tt>
   *
   *    while (true) {
   *      // get "ready" messages     
   *      mrSched.getIO(inBuf, outBuf);
   *
   *      // release
   *      receiveAll(inBuf);
   *      sendAll(outBuf);
   *    }
   *
   * <tt></pre>
   *
   * @param toIn  input messages are added to this List
   * @param toIn output messages are added to this List
   */
  public void getDueMessages(List toIn, List toOut);

}
