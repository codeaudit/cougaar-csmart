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
package org.cougaar.tools.csmart.society.abc;

import org.cougaar.tools.csmart.ui.component.*;

import java.io.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Collections;

/**
 * Defines a Local Asset File.
 * <br>
 * The local asset line contains:    
 * <ul>
 * <li> Name:                Name of the local asset.
 * <li> Decrement Amount:    Amount to decrement local resources.
 * <li> Average Time:        Average time to complete asset request.
 * <li> Inventory Deviation: Amount of chaos to apply to local inventory levels.
 * <li> Time Deviation:      Amount of chaos to apply to average time.
 * <li> Roles:               Roles this asset can satisify.
 * </ul>
 * <br>
 *
 * @see ConfigurableComponent
 */
public class ABCLocalAsset
  extends ConfigurableComponent
  implements Serializable
{

  /** Asset file name property definitions **/
  public static final String PROP_ASSETFILENAME = "Asset File Name";
  public static final String PROP_ASSETFILENAME_DESC = "Name of the Local Asset file";

  /** Asset Name property definitions **/
  public static final String PROP_NAME = "Asset Name";
  public static final String PROP_NAME_DFLT = "ClassOneDepot";
  public static final String PROP_NAME_DESC = "Name of the local asset";

  /** Decrement Amount property definitions **/
  public static final String PROP_DECAMOUNT = "Decrement Amount";
  public static final Long   PROP_DECAMOUNT_DFLT = new Long(25);
  public static final String PROP_DECAMOUNT_DESC = "Amount to decrement local asset supply for each use";

  /** Average Time property definitions **/
  public static final String PROP_AVGTIME = "Average Time";
  public static final Long   PROP_AVGTIME_DFLT = new Long(12000);
  public static final String PROP_AVGTIME_DESC = "Average time (milliseconds) it takes to satisify asset request";

  /** Inventory Deviation property definitions **/
  public static final String PROP_INVDEV = "Inventory Deviation";
  public static final Long   PROP_INVDEV_DFLT = new Long(0);
  public static final String PROP_INVDEV_DESC = "Amount of Chaos to apply to the Inventory levels";

  /** Time Deviation property definitions **/
  public static final String PROP_TIMEDEV = "Time Deviation";
  public static final Long   PROP_TIMEDEV_DFLT = new Long(0);
  public static final String PROP_TIMEDEV_DESC = "Amount of Chaos to apply to the time it takes to satisify asset";

  /** Roles property definitions **/
  public static final String PROP_ROLES = "Roles";
  public static final String[] PROP_ROLES_DFLT = new String[0];
  public static final String PROP_ROLES_DESC = "Roles that this asset can satisify";

  private Property propAssetName;
  private Property propDecAmount;
  private Property propAvgTime;
  private Property propInvDeviation;
  private Property propTimeDeviation;
  private Property propRoles;

  ABCLocalAsset() {
    this("LocalAssets");
  }

  ABCLocalAsset(String name) {
    super(name);
  }

  /**
   * Initializes all the properties
   */
  public void initProperties() {
    propAssetName = addProperty(PROP_NAME, PROP_NAME_DFLT);
    propAssetName.setToolTip(PROP_NAME_DESC);

    propDecAmount = addProperty(PROP_DECAMOUNT, PROP_DECAMOUNT_DFLT);
    propDecAmount.setToolTip(PROP_DECAMOUNT_DESC);
    propDecAmount.setAllowedValues(Collections.singleton(new LongRange(0, 100)));

    propAvgTime = addProperty(PROP_AVGTIME, PROP_AVGTIME_DFLT);
    propAvgTime.setToolTip(PROP_AVGTIME_DESC);
    propAvgTime.setAllowedValues(Collections.singleton(new LongRange(0, Long.MAX_VALUE)));

    propInvDeviation = addProperty(PROP_INVDEV, PROP_INVDEV_DFLT);
    propInvDeviation.setToolTip(PROP_INVDEV_DESC);
    propInvDeviation.setAllowedValues(Collections.singleton(new LongRange(0, Long.MAX_VALUE)));

    propTimeDeviation = addProperty(PROP_TIMEDEV, PROP_TIMEDEV_DFLT);
    propTimeDeviation.setToolTip(PROP_TIMEDEV_DESC);
    propTimeDeviation.setAllowedValues(Collections.singleton(new LongRange(0, Long.MAX_VALUE)));

    propRoles = addProperty(PROP_ROLES, PROP_ROLES_DFLT);
    propRoles.setToolTip(PROP_ROLES_DESC);
  }

  /**
   * Generates a configuration line containing all values required
   * for a Local Asset.  <br>
   * A Local asset line is in the form of:   <br>
   * [name], [dec amt], [avg time], [inv_stdev], [time_stdev], [roles]
   *
   * @return String Configuration Line
   */  
  private String getConfigLine() {
    String ret =  (String)propAssetName.getValue() + ", " +
      (String)((Long)propDecAmount.getValue()).toString() + ", " +
      (String)((Long)propAvgTime.getValue()).toString() + ", " +
      (String)((Long)propInvDeviation.getValue()).toString() + ", " +
      (String)((Long)propTimeDeviation.getValue()).toString();

    String[] roles = (String[])propRoles.getValue();
    for(int i=0; i < roles.length; i++) {
      ret = ret + ", " + roles[i];
    }

    return ret;
  }

  /**
   * Writes a Local Asset File to the specified directory. <br>
   * Local Asset files are of the format:
   * [name], [dec amt], [avg time], [inv_stdev], [time_stdev], [roles]
   * <br><br>
   * @param File directory to place taskfile in.
   * @throws IOException if the file cannot be created.
   */
  public void writeAssetFile(File configDir) throws IOException {
    File taskFile = new File(configDir, (String)getProperty(PROP_ASSETFILENAME).getValue());
    PrintWriter writer = new PrintWriter(new FileWriter(taskFile));

    try {
      writer.println("# [name], [dec amt], [avg time], [inv_stdev], [time_stdev], [roles]");
      writer.println(getConfigLine());
    }
    finally {
      writer.close();
    }
  }			     

  public LeafComponentData createAssetFileLeaf() {
    GenericLeafComponentData lcd = new GenericLeafComponentData();
    StringBuffer sb = new StringBuffer();
    
    sb.append("# [name], [dec amt], [avg time], [inv_stdev], [time_stdev], [roles]\n");
    sb.append(getConfigLine() + "\n");

    lcd.setName((String)getProperty(PROP_ASSETFILENAME).getValue());
    lcd.setType(LeafComponentData.FILE);
    lcd.setValue(sb);

    return (LeafComponentData) lcd;
  }

}
