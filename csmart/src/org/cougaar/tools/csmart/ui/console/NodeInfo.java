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
  private final String processName;
  private final Properties properties;
  private final List args;

  public NodeInfo(RemoteHost appServer,
                  String nodeName, String hostName, String processName,
                  Properties properties, List args) {
    this.appServer = appServer;
    this.nodeName = nodeName;
    this.hostName = hostName;
    this.processName = processName;
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

  public String getProcessName() {
    return processName;
  }

  public Properties getProperties() {
    return properties;
  }

  public List getArgs() {
    return args;
  }
}
