/*
 * <copyright>
 *  Copyright 2000-2002 BBNT Solutions, LLC
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
package org.cougaar.tools.csmart.society.file;

import java.io.File;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import org.cougaar.planning.plugin.AssetDataFileReader;
import org.cougaar.tools.csmart.core.cdata.AgentAssetData;
import org.cougaar.tools.csmart.core.cdata.AgentComponentData;
import org.cougaar.tools.csmart.core.cdata.AssetDataCallbackImpl;
import org.cougaar.tools.csmart.core.cdata.ComponentData;
import org.cougaar.tools.csmart.core.cdata.PropGroupData;
import org.cougaar.tools.csmart.core.cdata.RelationshipData;
import org.cougaar.tools.csmart.core.property.ModifiableConfigurableComponent;
import org.cougaar.tools.csmart.core.property.Property;
import org.cougaar.tools.csmart.society.AssetComponent;
import org.cougaar.tools.csmart.society.ContainerBase;
import org.cougaar.tools.csmart.society.PropGroupBase;
import org.cougaar.tools.csmart.society.PropGroupComponent;
import org.cougaar.tools.csmart.society.RelationshipBase;
import org.cougaar.tools.csmart.util.FileParseUtil;
import org.cougaar.tools.csmart.util.PrototypeParser;
import org.cougaar.util.TimeSpan;

public class AssetFileComponent
  extends ModifiableConfigurableComponent
  implements AssetComponent {

  /** Property Definitions **/
//    public static final String PROP_TYPE = "Asset Type";
//    public static final String PROP_TYPE_DESC = "Type of Asset";
//    public static final String PROP_CLASS = "Asset Class";
//    public static final String PROP_CLASS_DESC = "Class of the Asset";
//    public static final String PROP_UID = "UID";
//    public static final String PROP_UID_DESC = "UID of the Asset";
//    public static final String PROP_UNITNAME = "Unit Name";
//    public static final String PROP_UNITNAME_DESC = "Unit Name of the Asset";
//    public static final String PROP_UIC = "UIC";
//    public static final String PROP_UIC_DESC = "UIC of the Asset";

  private Property propAssetClass;
  private Property propUniqueID;
  private Property propUnitName;
  private Property propUIC;
  private String filename;
  private String clusterName;
  private int iniFormat;
  
  public AssetFileComponent(String filename, String clusterName) {
    super("AssetData");
    this.filename = filename;
    this.clusterName = clusterName;
  }

  /**
   * Init properties from a file named:
   * filename base + -prototype-ini.dat
   */
  public void initProperties() {
    // strip off extension if it exists
    int index = filename.lastIndexOf('.');
    if (index != -1) 
      filename = filename.substring(0, index);
    // Since there are two possible types of asset files, we
    // need to determine which parser to use.
    // We will determine this by looking for the "UniqueId"
    // in the <agent>-ini.dat file.
    AgentAssetData aad = null;
    if(FileParseUtil.isOldStyleIni(filename + "-prototype-ini.dat")) {
      if(log.isDebugEnabled()) {
        log.debug("Using oldStyle Parser");
      }
      aad = PrototypeParser.parse(filename);
    } else {
      if(log.isDebugEnabled()) {
        log.debug("Using newStyle Parser");
      }
      AssetDataCallbackImpl callback = new AssetDataCallbackImpl(clusterName);
      AssetDataFileReader reader = new AssetDataFileReader();
      // If this filename is a Windows style filename, then absolute paths wont work.
      // Try to let the ConfigFinder find it.
      if (filename.indexOf(':') == 1) {
	String nfilename = filename.substring(filename.lastIndexOf(File.separatorChar) + 1);
	if (log.isDebugEnabled()) {
	  log.debug("Agent filename was windows style (" + filename + "), trying short version (" + nfilename + ") & letting ConfigFinder find it. This requires the files be on the ConfigPath");
	}
	filename = nfilename;
      }
      reader.readAsset(filename, callback);
      // Warning: the above line will cause a FileNotFoundException
      // Any time you use a new-style INI
      // file that is not your config path
      aad = callback.getAgentAssetData();
    }

    if (aad == null) {
      if (log.isErrorEnabled()) {
	log.error("Got no AgentAssetData from which to initialize! Perhaps retry with the directory you are loading from on your org.cougaar.config.path?");
      }
      return;
    }

    this.iniFormat = aad.getIniFormat();
    propAssetClass = addProperty(PROP_CLASS, aad.getAssetClass());
    propAssetClass.setToolTip(PROP_CLASS_DESC);

    String uniqueId = (aad.getUniqueID() != null) ? aad.getUniqueID() : "";
    propUniqueID = addProperty(PROP_UID, uniqueId);
    propUniqueID.setToolTip(PROP_UID_DESC);

    
    // Unit name is allowed to be null, if it is, give an empty string.
    String unitname = (aad.getUnitName() == null) ? "" : aad.getUnitName();
    propUnitName = addProperty(PROP_UNITNAME, unitname);
    propUnitName.setToolTip(PROP_UNITNAME_DESC);
    
    propUIC = addProperty(PROP_UIC, (aad.getUIC() != null) ? aad.getUIC() : "UIC/" + clusterName);
    propUIC.setToolTip(PROP_UIC_DESC);

    addPropGroups(aad);
    addRelationships(aad.getRelationshipData());
  }

  /**
   * Add component data for asset properties, relationships,
   * and property groups.
   */

  public ComponentData addComponentData(ComponentData data) {
    AgentAssetData assetData = new AgentAssetData((AgentComponentData)data);
    assetData.setIniFormat(this.iniFormat);
    assetData.setAssetClass((String)propAssetClass.getValue());
    assetData.setUniqueID((String)propUniqueID.getValue());
    assetData.setUnitName((String)propUnitName.getValue());
    assetData.setUIC((String)propUIC.getValue());

    // Add Relationships.
    Iterator iter = 
      ((Collection)getDescendentsOfClass(ContainerBase.class)).iterator();
    while(iter.hasNext()) {
      ContainerBase container = (ContainerBase)iter.next();
      if(container.getShortName().equals("Relationships")) {
        for(int i=0; i < container.getChildCount(); i++) {
          RelationshipBase rel = (RelationshipBase) container.getChild(i);
          RelationshipData rData = new RelationshipData();
          rData.setType((String)rel.getProperty(RelationshipBase.PROP_TYPE).getValue());
          if(this.iniFormat == AgentAssetData.NEW_FORMAT) {
            // Since role is stored in type in the new format, we must adjust.
            rData.setRole((String)rel.getProperty(RelationshipBase.PROP_TYPE).getValue());
          } else {
            rData.setRole((String)rel.getProperty(RelationshipBase.PROP_ROLE).getValue());
          }

          rData.setItemId((String)rel.getProperty(RelationshipBase.PROP_ITEM).getValue());
          rData.setTypeId((String)rel.getProperty(RelationshipBase.PROP_TYPEID).getValue());
          rData.setSupported((String)rel.getProperty(RelationshipBase.PROP_SUPPORTED).getValue());
          DateFormat df = DateFormat.getInstance();
          try {
            Date start = df.parse((String)rel.getProperty(RelationshipBase.PROP_STARTTIME).getValue());
            Date end = df.parse((String)rel.getProperty(RelationshipBase.PROP_STOPTIME).getValue());
            rData.setStartTime(start.getTime());
            rData.setEndTime(end.getTime());
          } catch(ParseException pe) {
            if(log.isErrorEnabled()) {
              log.error("Caught Exception parsing Date, using default dates.", pe);
            }
            rData.setStartTime(TimeSpan.MIN_VALUE);
            rData.setEndTime(TimeSpan.MAX_VALUE);
          }

          assetData.addRelationship(rData);
        }
      }
    }

    // Add Property Groups.
    iter = 
      ((Collection)getDescendentsOfClass(ContainerBase.class)).iterator();
    while(iter.hasNext()) {
      ContainerBase container = (ContainerBase)iter.next();
      if(container.getShortName().equals("Property Groups")) {
        for(int i=0; i < container.getChildCount(); i++) {
          PropGroupComponent pg = (PropGroupComponent)container.getChild(i);
          assetData.addPropertyGroup(pg.getPropGroupData());
        }
      }
    }

    data.addAgentAssetData(assetData);
    return data;
  }

  private void addRelationships(RelationshipData[] rel) {
    ContainerBase relContainer = new ContainerBase("Relationships");
    relContainer.initProperties();
    addChild(relContainer);
    for(int i=0; i < rel.length; i++) {
      RelationshipBase newR = new RelationshipBase(rel[i]);
      newR.initProperties();
      relContainer.addChild(newR);
    }
  }

  private void addPropGroups(AgentAssetData aad) {
    ContainerBase pgContainer = new ContainerBase("Property Groups");
    pgContainer.initProperties();
    addChild(pgContainer);
    Iterator iter = aad.getPropGroupsIterator();
    while(iter.hasNext()) {
      PropGroupData pgd = (PropGroupData)iter.next();
      PropGroupComponent newPG = new PropGroupBase(pgd);
      if(log.isDebugEnabled()) {
        log.debug("Adding: " + pgd.getName());
      }
      newPG.initProperties();
      pgContainer.addChild(newPG);
    }
  }

}
