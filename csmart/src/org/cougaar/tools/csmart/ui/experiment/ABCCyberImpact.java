/* 
 * <copyright>
 *  Copyright 2001 BBNT Solutions, LLC
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
package org.cougaar.tools.csmart.ui.experiment;

import org.cougaar.tools.csmart.ui.component.*;

import java.io.Serializable;

public class ABCCyberImpact
  extends ModifiableConfigurableComponent
  implements PropertiesListener, Serializable {

  
  public static final String PROP_TARGET = "Target";
  public static final String PROP_TARGET_DFLT = new String();
  public static final String PROP_TARGET_DESC = "Target agent for Cyber Attack";

  public static final String PROP_TIME = "Time";
  public static final String PROP_TIME_DFLT = new String();
  public static final String PROP_TIME_DESC = "Time cyber attack starts (milliseconds)";
  
  public static final String PROP_TYPE = "Type";
  public static final String PROP_TYPE_DFLT = new String();
  public static final String PROP_TYPE_DESC = "Type of cyber attack";

  public static final String PROP_INTENSITY = "Intensity";
  public static final Double PROP_INTENSITY_DFLT = new Double(0.0);
  public static final String PROP_INTENSITY_DESC = "Intensity of the attack";

  public static final String PROP_DURATION = "Duration";
  public static final Long   PROP_DURATION_DFLT = new Long(0);
  public static final String PROP_DURATION_DESC = "Duration of the attack";

  private Property propTarget;
  private Property propTime;
  private Property propType;
  private Property propIntensity;
  private Property propDuration;

  public ABCCyberImpact() {
    this("Cyber Event");
  }

  public ABCCyberImpact(String name) {
    super(name);
  }

  public void initProperties() {
    propTarget = addProperty(PROP_TARGET, PROP_TARGET_DFLT, 
			       new ConfigurableComponentPropertyAdapter() {
				 public void propertyValueChanged(PropertyEvent e) {
				 }
			       });
    propTarget.setToolTip(PROP_TARGET_DESC);

    propTime = addProperty(PROP_TIME, PROP_TIME_DFLT, 
			       new ConfigurableComponentPropertyAdapter() {
				 public void propertyValueChanged(PropertyEvent e) {
				 }
			       });
    propTime.setToolTip(PROP_TIME_DESC);

    propType = addProperty(PROP_TYPE, PROP_TYPE_DFLT, 
			       new ConfigurableComponentPropertyAdapter() {
				 public void propertyValueChanged(PropertyEvent e) {
				 }
			       });
    propType.setToolTip(PROP_TYPE_DESC);

    propIntensity = addProperty(PROP_INTENSITY, PROP_INTENSITY_DFLT, 
			       new ConfigurableComponentPropertyAdapter() {
				 public void propertyValueChanged(PropertyEvent e) {
				 }
			       });
    propIntensity.setToolTip(PROP_INTENSITY_DESC);

    propDuration = addProperty(PROP_DURATION, PROP_DURATION_DFLT, 
			       new ConfigurableComponentPropertyAdapter() {
				 public void propertyValueChanged(PropertyEvent e) {
				 }
			       });
    propDuration.setToolTip(PROP_DURATION_DESC);
  }

  public StringBuffer getXML() {
    // This should eventually use Xerces to construct itself.
    StringBuffer sb = new StringBuffer();

    sb.append("   <CYBER TARGET=\"");
    sb.append(getProperty(PROP_TARGET).getValue());
    sb.append("\" TIME=\"");
    sb.append(getProperty(PROP_TIME).getValue());
    sb.append("\" TYPE=\"");
    sb.append(getProperty(PROP_TYPE).getValue());
    sb.append("\">\n");
    sb.append("      <PARAM NAME=\"Intensity\" VALUE=\"");
    sb.append(getProperty(PROP_INTENSITY).getValue());
    sb.append("\"></PARAM>\n");
    sb.append("      <PARAM NAME=\"Duration\" VALUE=\"");
    sb.append(getProperty(PROP_DURATION).getValue());
    sb.append("\"></PARAM>\n");
    sb.append("   </CYBER>\n");

    return sb;
  }

  /**
   * Called when a new property has been added to the
   * society. 
   *
   * @param PropertyEvent Event for the new property
   */
  public void propertyAdded(PropertyEvent e) {
    Property addedProperty = e.getProperty();
    Property myProperty = getProperty(addedProperty.getName().last().toString());
    if (myProperty != null) {
      setPropertyVisible(addedProperty, true);
    }
  }
  /**
   * Called when a property has been removed from the society
   */
  public void propertyRemoved(PropertyEvent e) {}

}
