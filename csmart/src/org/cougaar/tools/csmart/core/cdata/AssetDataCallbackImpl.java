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
package org.cougaar.tools.csmart.core.cdata;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Vector;
import org.cougaar.planning.plugin.AssetDataCallback;
import org.cougaar.tools.csmart.core.cdata.AgentAssetData;
import org.cougaar.tools.csmart.core.cdata.PGPropMultiVal;
import org.cougaar.tools.csmart.core.cdata.RelationshipData;
import org.cougaar.tools.csmart.ui.viewer.CSMART;
import org.cougaar.util.ConfigFinder;
import org.cougaar.util.TimeSpan;
import org.cougaar.util.log.Logger;


/**
 * AssetDataCallbackImpl.java
 *
 *
 * Created: Fri Mar  8 12:40:31 2002
 *
 * @author <a href="mailto:bkrisler@bbn.com">Brian Krisler</a>
 * @version 1.0
 */

public class AssetDataCallbackImpl implements AssetDataCallback {
  private AgentAssetData assetData = null;
  private DateFormat myDateFormat = DateFormat.getInstance();
  private PropGroupData propGroup = null;
  private transient Logger log = null;

  public AssetDataCallbackImpl (){
    log = CSMART.createLogger(this.getClass().getName());
  }
  // implementation of org.cougaar.planning.plugin.AssetDataCallback interface

  public String getType(String type) {
    int i;
    if ((i = type.indexOf("<")) > -1) { // deal with collections 
      int j = type.lastIndexOf(">");
      return getType(type.substring(0, i).trim()); // deal with measures
    } else if ((i = type.indexOf("/")) > -1) {
      return getType(type.substring(0, i).trim());
    } else {
      return type;
    }
  }

  public AgentAssetData getAgentAssetData() {
    if(log.isWarnEnabled() && assetData == null) {
      log.warn("Warning: assetData is null");
    }
    return assetData;
  }

  public ConfigFinder getConfigFinder() {
    return ConfigFinder.getInstance();
  }

  public void createMyLocalAsset(String assetClassName) {
    if(log.isDebugEnabled()) {
      log.debug("Creating Asset of class: " + assetClassName);
    }
    assetData = new AgentAssetData(null);
    assetData.setAssetClass(assetClassName);
  }

  public boolean hasMyLocalAsset() {
    return (assetData != null);
  }

  public void createPropertyGroup(String propGroupName) throws Exception {
    if(log.isDebugEnabled()) {
      log.debug("Create PropertyGroup: " + propGroupName);
    }
    propGroup = new PropGroupData(propGroupName);
  }

  public Object parseExpr(String type, String arg) {
    int i;

    type = type.trim();
    arg = arg.trim();

    if ((i = type.indexOf("<")) >= 0) {
      int j = type.lastIndexOf(">");
      String ctype = type.substring(0, i).trim();
      String etype = type.substring(i + 1, j).trim();
      Collection c = null;
      if (ctype.equals("Collection") || ctype.equals("List")) {
        c = new ArrayList();
      } else {
        throw new RuntimeException("Unparsable collection type: "+type);
      }

      Vector l = org.cougaar.util.StringUtility.parseCSV(arg);
      for (Iterator it = l.iterator(); it.hasNext();) {
        c.add((String)parseE(etype, (String)it.next()));
      }
      if(log.isDebugEnabled()) {
        log.debug("Collection Size: " + c.size());
      }
      return c;
    } else if ((i = type.indexOf("/")) >= 0) {
      // Handle Measure Object Here.
      return arg;
    } else {
      return arg;
    }    
  }

  private Object parseE(String type, String arg) {
    int i;

    type = type.trim();
    arg = arg.trim();

    if ((i = type.indexOf("<")) >= 0) {
      int j = type.lastIndexOf(">");
      String ctype = type.substring(0, i).trim();
      String etype = type.substring(i + 1, j).trim();
      Collection c = null;
      if (ctype.equals("Collection") || ctype.equals("List")) {
        c = new ArrayList();
      } else {
        throw new RuntimeException("Unparsable collection type: "+type);
      }

      Vector l = org.cougaar.util.StringUtility.parseCSV(arg);
      for (Iterator it = l.iterator(); it.hasNext();) {
        c.add((String)parseE(etype, (String)it.next()));
      }
      return c;
    } else if ((i = type.indexOf("/")) >= 0) {
      // Measure Object.  How should we handle this?
      return arg;
    } else {
      return arg;
    }    
  }

  public long parseDate(String dateString) throws ParseException {
    if(log.isDebugEnabled()) {
      log.debug("Parsing Date: " + dateString);
    }
    return myDateFormat.parse(dateString).getTime();
  }


  /**
   * This is a "creative interpretation" of the callSetter method.
   * This implementation Creates the PGPropData for the passed in
   * name.
   *
   */
  public void callSetter(String setterName, String type, Object[] arguments) {
    String name = setterName.substring(3);

    if(log.isWarnEnabled() && arguments.length > 1) {
      log.warn("Arguments is greater than one!");
    }

    PGPropData data = new PGPropData();
    data.setName(name);
    if(arguments[0] instanceof Collection) {
      if(log.isDebugEnabled()) {
        log.debug("MultiVal for: " + name);
      }
      Iterator iter = ((Collection)arguments[0]).iterator();
      data.setType("Collection");
      data.setSubType(type);
      PGPropMultiVal multi = new PGPropMultiVal();
      while(iter.hasNext()) {
        multi.addValue((String)iter.next());
      }
      data.setValue(multi);
    } else {
      data.setType(type);
      data.setValue(arguments[0]);
    }
    propGroup.addProperty(data);
  }

  public void setLocationSchedule(String latStr, String lonStr) {
    // Not really sure what to do here.
  }

  public long getDefaultStartTime() {
    return TimeSpan.MIN_VALUE;
  }

  public long getDefaultEndTime() {
    return TimeSpan.MAX_VALUE;
  }

  public void addPropertyToAsset() {
    if(log.isDebugEnabled()) {
      log.debug("Adding Property group: "+propGroup.getName()+" to asset");
    }
    assetData.addPropertyGroup(propGroup);
  }

  public void addRelationship(String typeId, String itemId, String otherClusterId, String roleName, long start, long end) {

    RelationshipData rd = new RelationshipData();
    rd.setType(typeId);
    rd.setItem(itemId);
    rd.setRole(roleName);
    rd.setStartTime(start);
    rd.setEndTime(end);
    rd.setSupported(otherClusterId);
    assetData.addRelationship(rd);
  }
  
}// AssetDataCallbackImpl
