/**
 * <copyright>
 *  Copyright 2002 BBNT Solutions, LLC
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
 *  </copyright>
 */
package org.cougaar.tools.csmart.recipe;



import java.io.Serializable;
import java.net.URL;
import org.cougaar.tools.csmart.core.cdata.ComponentData;
import org.cougaar.tools.csmart.core.property.Property;


/**
 * SwitchPluginRecipe.java
 *
 *
 * Created: Wed Mar 27 09:48:41 2002
 *
 * @author <a href="mailto:bkrisler@bbn.com">Brian Krisler</a>
 * @version 1.0
 */

public class SwitchPluginRecipe extends RecipeBase 
  implements Serializable {

  private static final String PROP_OLD_CLASS = "Old Plugin Class";
  private static final String PROP_OLD_CLASS_DFLT = "";
  private static final String PROP_OLD_CLASS_DESC = "Plugin Class to be replaced";

  private static final String PROP_NEW_CLASS = "New Plugin Class";
  private static final String PROP_NEW_CLASS_DFLT = "";
  private static final String PROP_NEW_CLASS_DESC = "New Plugin Class";

  private Property propOldPluginClass;
  private Property propNewPluginClass;

  private static final String DESCRIPTION_RESOURCE_NAME = 
    "switch-plugin-recipe-description.html";

  public SwitchPluginRecipe (){
    this("Switch Plugin Recipe");
  }
  
  public SwitchPluginRecipe(String name) {
    super(name);
  }

  public void initProperties() {
    propOldPluginClass = addProperty(PROP_OLD_CLASS, PROP_OLD_CLASS_DFLT);
    propOldPluginClass.setToolTip(PROP_OLD_CLASS_DESC);

    propNewPluginClass = addProperty(PROP_NEW_CLASS, PROP_NEW_CLASS_DFLT);
    propNewPluginClass.setToolTip(PROP_NEW_CLASS_DESC);
  }

  /**
   * Gets the name of the html help file for this component.
   *
   * @return an <code>URL</code> value
   */
  public URL getDescription() {
    return getClass().getResource(DESCRIPTION_RESOURCE_NAME);
  }

  public ComponentData addComponentData(ComponentData data) {
    return data;
  }

  public ComponentData modifyComponentData(ComponentData data) {
    return data;
  }

}// SwitchPluginRecipe
