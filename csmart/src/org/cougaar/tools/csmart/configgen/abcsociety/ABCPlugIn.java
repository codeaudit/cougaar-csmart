/* 
 * <copyright>
 * This software is to be used only in accordance with the COUGAAR license
 * agreement. The license agreement and other information can be found at
 * http://www.cougaar.org
 * 
 *       © Copyright 2001 by BBNT Solutions LLC.
 * </copyright>
 */
package org.cougaar.tools.csmart.configgen.abcsociety;

import org.cougaar.tools.csmart.ui.component.*;
import java.io.*;

public class ABCPlugIn
  extends ConfigurableComponent
  implements Serializable
{
  public static final String PROP_PARAM = "param-";
  private int nParameters = 0;
  private String pluginClass;

  public ABCPlugIn(String name, String pluginClass) {
    super(name);
    this.pluginClass = pluginClass;
  }

  public void initProperties() {
  }

  public String getPlugInClassName() {
    return pluginClass;
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
    if(nParameters != 0 ) {
      buf.append("(");
      for (int i = 0; i < nParameters; i++) {
	Object param = getProperty(PROP_PARAM + i).getValue();
	if (i > 0) {
	  buf.append(",");
	}
	buf.append(param);
      }
      buf.append(")");
    }
    return buf.substring(0);
  }
}
