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

/**
 * A controller API for the <code>SlowMessageTransportServiceProxy</code>.
 */
public interface SlowMessageTransportServiceProxyController {

  /**
   * Slow both the input and output release rates to 
   * <tt>(100% * intensity)</tt> of their existing capacity for now until 
   * the end of the specified (millisecond) duration.
   * <p>
   * Example factors and their effect upon the I/O release rates:<ul>
   *   <li>0.0 --&gt; stop all I/O</li>
   *   <li>0.5 --&gt; slow all I/O to half-speed</li>
   *   <li>1.0 --&gt; no effect</li>
   * <ul>
   * <p>
   * This API could be enhanced to support point-to-point in/out degrades,
   * such as "degrade send-to-AgentX to 5 messages/second".
   *
   * @param factor a double that is &gt;= to 0.0 and &lt;= to 1.0
   * @param duration the number of milliseconds for this slow-down
   */
  void degradeReleaseRate(
      double factor,
      long duration);

}
