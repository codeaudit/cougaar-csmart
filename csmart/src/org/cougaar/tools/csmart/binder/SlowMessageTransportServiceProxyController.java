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
  public void degradeReleaseRate(
      double factor,
      long duration);

}
