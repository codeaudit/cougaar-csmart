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
package org.cougaar.tools.csmart.runtime.binder;

import java.util.List;

import org.cougaar.core.mts.Message;

/**
 * An implementation of <tt>MessageReleaseScheduler</tt>.
 * <p>
 * This implementation just keeps an "inQ" and "outQ" and degrades
 * both.  One could extend this class to support point-to-point
 * in/out degrades (e.g. "degrade send-to-AgentX to 5 messages/second").
 * <p>
 * This class might examine the <code>Message</code> data structures
 * in the future (e.g. examine the destination, etc).
 */
public class MessageReleaseSchedulerImpl 
  implements MessageReleaseScheduler 
{

  /**
   * Keep "samplesPerSecond" &lt;= 4.
   */
  private static final long MIN_SLEEP_MILLIS = 250;

  private static final boolean VERBOSE = false;

  private boolean anyWork = false;
  private Object workLock = new Object();

  private Object qLock = new Object();
  private ObjectReleaseQueue outQ;
  private ObjectReleaseQueue inQ;

  private long sleepMS;
  private long prevTime = System.currentTimeMillis();

  public MessageReleaseSchedulerImpl(
      double samplesPerSecond,
      double inMessagesPerSecond,
      double outMessagesPerSecond) {
    long t = (int)(1000.0 / samplesPerSecond);
    if (t < MIN_SLEEP_MILLIS) {
      t = MIN_SLEEP_MILLIS;
    }
    this.sleepMS = t;
    this.inQ = new ObjectReleaseQueue(inMessagesPerSecond);
    this.outQ = new ObjectReleaseQueue(outMessagesPerSecond);
  }

  public void degradeReleaseRate(
      double factor,
      long duration) {
    if (VERBOSE) {
      System.out.println(
          "Degrade release rate to ("+factor+", "+duration+")");
    }
    synchronized (qLock) {
      outQ.degradeReleaseRate(factor, duration);
      inQ.degradeReleaseRate(factor, duration);
    }
  }

  public void sendMessage(Message m) {
    synchronized (qLock) {
      boolean wasEmpty = outQ.isEmpty();
      if (VERBOSE) {
        System.out.println(
            "  # send-lock ("+wasEmpty+") ["+outQ.size()+"]");
      }
      try {
        outQ.add(m);
        if (false || wasEmpty) {
          signalActivity();
        }
      } catch (Exception e) {
        // did we overflow a buffer!
      }
    }
  }

  public void receiveMessage(Message m) {
    synchronized (qLock) {
      boolean wasEmpty = inQ.isEmpty();
      inQ.add(m);
      if (false || wasEmpty) {
        signalActivity();
      }
    }
  }

  //
  // Future enhancement idea:
  //   Could use a Thread-interrupt mechanism and the queues could 
  //   suggest a sleep interval (where this does "sleep(min of suggestions)").
  //   This would remove some of the idle looping...
  //

  public void getDueMessages(List toIn, List toOut) {

    // block if queues are empty
    boolean shouldWait;
    synchronized (qLock) {
      // FIXME: this is questionable -- shouldn't the "wait" be within
      //   a sync(qLock), but then we deadlock.  I'll leave this for 
      //   now...
      shouldWait = inQ.isEmpty() && outQ.isEmpty();
    }
    if (shouldWait) {
      if (VERBOSE) {
        System.out.println("@waiting for a non-empty queue");
      }
      waitForActivity();
    }

    // sleep a bit
    try {
      if (VERBOSE) {
        System.out.println("@sleep("+sleepMS+")");
      }
      Thread.sleep(sleepMS);
    } catch (InterruptedException ie) {
    }

    // get the messages
    synchronized (qLock) {
      long nowTime = System.currentTimeMillis();
      if (VERBOSE) {
        System.out.println("@get queue messages("+prevTime+", "+nowTime+")");
      }
      // in:
      inQ.getDueObjects(toIn, prevTime, nowTime);
      // out:
      outQ.getDueObjects(toOut, prevTime, nowTime);
      //
      if (VERBOSE) {
        System.out.println("@got messages");
      }
      prevTime = nowTime;
    }
  }

  private void waitForActivity() {
    synchronized (workLock) {
      if (VERBOSE) {
        System.out.println("  * wait-locked ("+anyWork+")");
      }
      while (!(anyWork)) {
        try {
          if (VERBOSE) {
            System.out.println("  * wait-wait");
          }
          workLock.wait();
        } catch (InterruptedException ie) {
        }
      }
      if (VERBOSE) {
        System.out.println("  * wait-release ("+anyWork+" -> false)");
      }
      anyWork = false;
    }
  }

  private void signalActivity() {
    // isn't this double-check locking BAD?  Well, the core does it...
    if (!(anyWork)) {
      synchronized (workLock) {
        if (!(anyWork)) {
          anyWork = true; 
          if (VERBOSE) {
            System.out.println("  * signal-notify");
          }
          workLock.notify();
        }
      }
    }
  }

}
