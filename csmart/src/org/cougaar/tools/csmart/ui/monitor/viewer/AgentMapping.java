/*
 * <copyright>
 *  Copyright 2000-2003 BBNT Solutions, LLC
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

package org.cougaar.tools.csmart.ui.monitor.viewer;

/**
 * Maps an agent name to the Servlet host and port for that agent, for
 * use by <tt>ThreadUtils</code>.
 *
 * @see ThreadUtils
 */
public interface AgentMapping {

  /**
   * Get the host for an agent's Servlet server.
   * <p>
   * Servlet queries are more efficient if the host for this specific agent 
   * is known, but the URLConnection redirect mechanism should allow the user to
   * specify one Servlet host for all agents.
   *
   * @param agentName Agent to get Host For
   * @return a Host Name as a <code>String</code> value
   */
  String getHost(String agentName);

  /**
   * Get the port address for an agent's HTTP server.
   * <p>
   * Typically this is "8800", but the user might configure this 
   * port to a different address.
   *
   * @param agentName Agent to get port for
   * @return an port as an <code>int</code> value
   * @see #getHost(String)
   */
  int getPort(String agentName);
}
