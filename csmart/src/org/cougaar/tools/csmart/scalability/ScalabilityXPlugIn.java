/*
 * <copyright>
 *  Copyright 1997-2001 BBNT Solutions, LLC
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
package org.cougaar.tools.csmart.scalability;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import org.cougaar.tools.csmart.ui.component.*;

public class ScalabilityXPlugIn
  extends ConfigurableComponent
  implements Serializable
{
  private static final long serialVersionUID = -8876469926326973836L;

  public static final String PROP_PARAM = "param-";
  private int nParameters = 0;
  private String pluginClass;

  public ScalabilityXPlugIn(String name, String pluginClass) {
    super(name);
    this.pluginClass = pluginClass;
  }

  public void initProperties() {
  }

  public String getPlugInClassName() {
    return pluginClass;
  }

  public int getIntParameter(int ix) {
    return ((Integer) getProperty(PROP_PARAM + ix).getValue()).intValue();
  }

  public Property addParameter(int param) {
    return addProperty(PROP_PARAM + nParameters++, new Integer(param));
  }

  public Property addParameter(String param) {
    return addProperty(PROP_PARAM + nParameters++, param);
  }

  /**
   * Add a parameter that is based on some other property (typically a
   * property of our parnt.
   **/
  public Property addParameter(Property prop) {
    return addProperty(new PropertyAlias(this, PROP_PARAM + nParameters++, prop));
  }

  public String getConfigLine() {
    StringBuffer buf = new StringBuffer();
    buf.append("plugin = ");
    buf.append(getPlugInClassName());
    buf.append("(");
    for (int i = 0; i < nParameters; i++) {
      Object param = getProperty(PROP_PARAM + i).getValue();
      if (i > 0) {
        buf.append(",");
      }
      buf.append(param);
    }
    buf.append(")");
    return buf.substring(0);
  }
}
