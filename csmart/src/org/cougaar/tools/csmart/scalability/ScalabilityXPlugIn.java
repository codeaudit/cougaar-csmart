/*
 * <copyright>
 * Copyright 1997-2001 Defense Advanced Research Projects
 * Agency (DARPA) and ALPINE (a BBN Technologies (BBN) and
 * Raytheon Systems Company (RSC) Consortium).
 * This software to be used only in accordance with the
 * COUGAAR licence agreement.
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
