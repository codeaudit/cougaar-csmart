/*
 * <copyright>
 *  
 *  Copyright 2000-2004 BBNT Solutions, LLC
 *  under sponsorship of the Defense Advanced Research Projects
 *  Agency (DARPA).
 * 
 *  You can redistribute this software and/or modify it under the
 *  terms of the Cougaar Open Source License as published on the
 *  Cougaar Open Source Website (www.cougaar.org).
 * 
 *  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 *  "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 *  LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 *  A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 *  OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 *  SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 *  LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 *  DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 *  THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 *  OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *  
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
