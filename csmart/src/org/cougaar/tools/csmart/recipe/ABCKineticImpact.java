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
package org.cougaar.tools.csmart.recipe;

import java.io.Serializable;

import org.cougaar.tools.csmart.core.property.Property;
import org.cougaar.tools.csmart.core.property.ConfigurableComponentPropertyAdapter;
import org.cougaar.tools.csmart.core.property.PropertyEvent;
import java.net.URL;

public class ABCKineticImpact
  extends RecipeBase
  implements Serializable {


  private static final String DESCRIPTION_RESOURCE_NAME = "kinetic-impact-description.html";
  private static final String BACKUP_DESCRIPTION =  "Defines a Kinetic Impact";
  
  public static final String PROP_LATITUDE = "Latitude";
  public static final Double PROP_LATITUDE_DFLT = new Double(0.0);
  public static final String PROP_LATITUDE_DESC = "Latitude for center of Kinetic Event";

  public static final String PROP_LONGITUDE = "Longitude";
  public static final Double PROP_LONGITUDE_DFLT = new Double(0.0);
  public static final String PROP_LONGITUDE_DESC = "Longitude for center of Kinetic Event";

  public static final String PROP_TIME = "Time";
  public static final String PROP_TIME_DFLT = new String();
  public static final String PROP_TIME_DESC = "Time cyber attack starts (milliseconds)";
  
  public static final String PROP_TYPE = "Type";
  public static final String PROP_TYPE_DFLT = new String();
  public static final String PROP_TYPE_DESC = "Type of kinetic attack";

  public static final String PROP_INTENSITY = "Intensity";
  public static final Double PROP_INTENSITY_DFLT = new Double(0.0);
  public static final String PROP_INTENSITY_DESC = "Intensity of the attack";

  public static final String PROP_DURATION = "Duration";
  public static final Long   PROP_DURATION_DFLT = new Long(0);
  public static final String PROP_DURATION_DESC = "Duration of the attack";

  private Property propLatitude;
  private Property propLongitude;
  private Property propTime;
  private Property propType;
  private Property propIntensity;
  private Property propDuration;

  public ABCKineticImpact() {
    this("Kinetic Event");
  }

  public ABCKineticImpact(String name) {
    super(name);
  }
  
  public void initProperties() {
    propLatitude = addProperty(PROP_LATITUDE, PROP_LATITUDE_DFLT, 
			       new ConfigurableComponentPropertyAdapter() {
				 public void propertyValueChanged(PropertyEvent e) {
				 }
			       });
    propLatitude.setToolTip(PROP_LATITUDE_DESC);

    propLongitude = addProperty(PROP_LONGITUDE, PROP_LONGITUDE_DFLT, 
			       new ConfigurableComponentPropertyAdapter() {
				 public void propertyValueChanged(PropertyEvent e) {
				 }
			       });
    propLongitude.setToolTip(PROP_LONGITUDE_DESC);

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

  /**
   * Gets the name of the html help file for this component.
   *
   * @return an <code>URL</code> value
   */
  public URL getDescription() {
    return getClass().getResource(DESCRIPTION_RESOURCE_NAME);
  }

  public StringBuffer getXML() {
    // This should eventually use Xerces to construct itself.
    StringBuffer sb = new StringBuffer();

    sb.append("   <KINETIC LATITUDE=\"");
    sb.append(getProperty(PROP_LATITUDE).getValue());
    sb.append("\" LONGITUDE=\"");
    sb.append(getProperty(PROP_LONGITUDE).getValue());
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
}
