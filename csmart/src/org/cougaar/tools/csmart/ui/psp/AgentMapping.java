/*
 * <copyright>
 * This software is to be used only in accordance with the COUGAAR license
 * agreement. The license agreement and other information can be found at
 * http://www.cougaar.org
 *
 * © Copyright 2000, 2001 BBNT Solutions LLC
 * </copyright>
 */

package org.cougaar.tools.csmart.ui.psp;

/**
 * Maps an agent name to the PSP host and port for that agent, for
 * use by <tt>ThreadUtils</code>.
 *
 * @see ThreadUtils
 */
public interface AgentMapping {

  /**
   * Get the host for an agent's PSP server.
   * <p>
   * PSP queries are more efficient if the host for this specific agent 
   * is known, but the existing PSP proxy mechanism should allow the user to
   * specify one PSP host for all agents.
   */
  public String getHost(String agentName);

  /**
   * Get the port address for an agent's PSP server.
   * <p>
   * Typically this is "5555", but the user might configure this 
   * port to a different address.
   *
   * @see #getHost(String)
   */
  public int getPort(String agentName);

}
