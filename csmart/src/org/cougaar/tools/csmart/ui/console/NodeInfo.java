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

package org.cougaar.tools.csmart.ui.console;

import org.cougaar.tools.server.RemoteHost;

import java.util.List;
import java.util.Properties;

/**
 * The information needed to start, stop or attach to a running node.
 * Used to exchange information between the CSMARTConsole and
 * AppServerSupport classes.
 */

public final class NodeInfo {
  private final RemoteHost appServer;
  private final String nodeName;
  private final String hostName;
  private final Properties properties;
  private final List args;

  public NodeInfo(final RemoteHost appServer, final String nodeName, final String hostName,
                  final Properties properties, final List args) {
    this.appServer = appServer;
    this.nodeName = nodeName;
    this.hostName = hostName;
    this.properties = properties;
    this.args = args;
  }

  public RemoteHost getAppServer() {
    return appServer;
  }

  public String getNodeName() {
    return nodeName;
  }

  public String getHostName() {
    return hostName;
  }

  public Properties getProperties() {
    return properties;
  }

  public List getArgs() {
    return args;
  }

  public boolean equals(final Object o) {
    if (this == o) return true;
    if (!(o instanceof NodeInfo)) return false;

    final NodeInfo nodeInfo = (NodeInfo) o;

    if (!appServer.equals(nodeInfo.appServer)) return false;
    if (!args.equals(nodeInfo.args)) return false;
    if (!hostName.equals(nodeInfo.hostName)) return false;
    if (!nodeName.equals(nodeInfo.nodeName)) return false;
    if (!properties.equals(nodeInfo.properties)) return false;

    return true;
  }

  public int hashCode() {
    int result;
    result = appServer.hashCode();
    result = 29 * result + nodeName.hashCode();
    result = 29 * result + hostName.hashCode();
    result = 29 * result + properties.hashCode();
    result = 29 * result + args.hashCode();
    return result;
  }
}
