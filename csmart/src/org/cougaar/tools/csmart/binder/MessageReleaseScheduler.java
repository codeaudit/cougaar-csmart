/* 
 * <copyright>
 *  Copyright 2001 BBNT Solutions, LLC
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
