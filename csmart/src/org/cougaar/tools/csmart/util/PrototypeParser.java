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
package org.cougaar.tools.csmart.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.File;
import java.io.FileReader;
import java.io.StreamTokenizer;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import org.cougaar.util.ConfigFinder;
import org.cougaar.util.Reflect;
import org.cougaar.util.TimeSpan;
import org.cougaar.util.log.Logger;

import org.cougaar.core.agent.ClusterIdentifier;

import org.cougaar.tools.csmart.core.cdata.AgentAssetData;
import org.cougaar.tools.csmart.core.cdata.AgentComponentData;
import org.cougaar.tools.csmart.core.cdata.ComponentData;
import org.cougaar.tools.csmart.core.cdata.PGPropData;
import org.cougaar.tools.csmart.core.cdata.PGPropMultiVal;
import org.cougaar.tools.csmart.core.cdata.PropGroupData;
import org.cougaar.tools.csmart.core.cdata.RelationshipData;
import org.cougaar.tools.csmart.ui.viewer.CSMART;
import org.cougaar.tools.csmart.ui.viewer.SocietyFinder;


/**
 * PrototypeParser: Parse Agent AssetData files
 *
 *
 * Created: Thu Feb 21 13:36:49 2002
 *
 * @author <a href="mailto:bkrisler@bbn.com">Brian Krisler</a>
 * @version 1.0
 */
public class PrototypeParser {

  private Logger log;
  private String clusterId;
  private DateFormat myDateFormat = DateFormat.getInstance(); 

  private static TrivialTimeSpan ETERNITY = 
    new TrivialTimeSpan(TimeSpan.MIN_VALUE,
                        TimeSpan.MAX_VALUE);
  
  public PrototypeParser (){
    log = CSMART.createLogger(this.getClass().getName());
  }


  /**
   * Describe <code>parse</code> method here.
   *
   * @param data ComponentData for agent
   */
  public static AgentAssetData parse(String agentName) {
    PrototypeParser pp = new PrototypeParser();
    return pp.parsePrototypeFile(agentName);
  }

  public String getFileName() {
    return clusterId + "-prototype-ini.dat";
  }

  public long parseDate(String dateString) throws ParseException {
    return myDateFormat.parse(dateString).getTime();
  }

  public long getDefaultStartTime() {
    return TimeSpan.MIN_VALUE;
  }

  public long getDefaultEndTime() {
    return TimeSpan.MAX_VALUE;
  }

  public AgentAssetData parsePrototypeFile(String cId) {
    clusterId = cId;
    String dataItem = "";
    int newVal;

    String filename = getFileName();
    BufferedReader input = null;
    Reader fileStream = null;
    AgentAssetData aad = new AgentAssetData(null);
    if(log.isDebugEnabled()) {
      log.debug("Setting INI Format to: OLD_FORMAT");
    }
    aad.setIniFormat(AgentAssetData.OLD_FORMAT);

    try {
      File tryfile = new File(filename);
      if (! tryfile.exists()) {
	fileStream = 
	  //	  new InputStreamReader(ConfigFinder.getInstance().open(filename));
	  new InputStreamReader(SocietyFinder.getInstance().open(filename));
      } else {
	fileStream = new FileReader(tryfile);
      }
      input = new BufferedReader(fileStream);
      StreamTokenizer tokens = new StreamTokenizer(input);
      tokens.commentChar('#');
      tokens.wordChars('[', ']');
      tokens.wordChars('_', '_');
      tokens.wordChars('<', '>');      
      tokens.wordChars('/', '/');      
      tokens.ordinaryChars('0', '9');      
      tokens.wordChars('0', '9');      

      newVal = tokens.nextToken();
      // Parse the prototype-ini file
      while (newVal != StreamTokenizer.TT_EOF) {
        if (tokens.ttype != StreamTokenizer.TT_WORD)
          formatError("ttype: " + tokens.ttype + " sval: " + tokens.sval);
        dataItem = tokens.sval;
        if (dataItem.equals("[Prototype]")) {
          newVal = tokens.nextToken();
          String assetClassName = tokens.sval;
          aad.setAssetClass(assetClassName);
          if(log.isDebugEnabled()) {
            log.debug("AgentAssetData Class: " + aad.getAssetClass());
          }
          if(assetClassName.equals("Entity")) {
            aad.setType(AgentAssetData.ENTITY);
          }
          newVal = tokens.nextToken();
          continue;
        }
        if (dataItem.equals("[Relationship]")) {
          newVal = fillRelationships(newVal, tokens, aad);
          continue;
        }
        if (dataItem.equals("[LocationSchedulePG]")) {
          if(log.isDebugEnabled()) {
            log.debug("Parsing LocationSchedulePG");
          }
          // parser language is currently incapable of expressing a 
          // complex schedule, so here we hack in some minimal support.
//           newVal = setLocationSchedulePG(dataItem, newVal, tokens);
          continue;
        }
        if (dataItem.equals("[UniqueId]")) {
          newVal = tokens.nextToken();
          aad.setUniqueID(tokens.sval);
          if(log.isDebugEnabled()) {
            log.debug("Parsing UniqueId: " + aad.getUniqueID());
          }
          newVal = tokens.nextToken();
          continue;
        }
        if (dataItem.equals("[UIC]")) {
          newVal = tokens.nextToken();
          aad.setUIC(tokens.sval);
          if(log.isDebugEnabled()) {
            log.debug("Parsing UIC: " + aad.getUIC());
          }
          newVal = tokens.nextToken();
          continue;
        }
        if (dataItem.startsWith("[")) {
          // We've got a property or capability
           newVal = setPropertyForAsset(dataItem, newVal, tokens, aad);
          continue;
        }
        // if The token you read is not one of the valid
        // choices from above
        formatError("Incorrect token: " + dataItem);
      }

      // Closing BufferedReader
      if (input != null)
	input.close();

      // Only generates a NoSuchMethodException for AssetSkeleton
      // because of a coding error. If we are successul in creating it
      // here it then the AssetSkeleton will end up with two copies
      // the add/search criteria in AssetSkeleton is for a Vector and
      // does not gurantee only one instance of each class. Thus the
      // Org allocator plugin fails to recognize the correct set of
      // capabilities.
      
    } catch (Exception e) {
      if(log.isErrorEnabled()) {
        log.error("Exception", e);
      }
    }
    return aad;
  } 

  private void formatError(String msg) {
    throw new RuntimeException("Error parsing " + getFileName() + ": "
                               + msg);
  }

  /**
   * Fills in myRelationships with arrays of relationship, 
   * clusterName and capableroles triples.
   */
  protected int fillRelationships(int newVal, StreamTokenizer tokens,
                                  AgentAssetData aad) throws IOException {

    RelationshipData rd = null;

    newVal = tokens.nextToken();
    while ((newVal != StreamTokenizer.TT_EOF) &&
           (!tokens.sval.substring(0,1).equals("["))) {

      String type = "";
      String supportedCluster = "";
      String role = ""; 

      for (int i = 0; i < 3; i++) {
        if ((tokens.sval.length()) > 0  &&
            (tokens.sval.substring(0,1).equals("["))) {
          throw new RuntimeException("Unexpected character: " + 
                                     tokens.sval);
        }
            
        switch (i) {
        case 0:
          type = tokens.sval.trim();
          break;

        case 1:
          supportedCluster = tokens.sval.trim();
          break;

        case 2:
          role = tokens.sval.trim();
          break;
        }
        newVal = tokens.nextToken();
      }
      // This attempts to fix a problem with the
      // ini files.
      if(type.equalsIgnoreCase("Superior")) {
        role = "Subordinate";
      }
      rd = new RelationshipData();
      rd.setType(type);
      rd.setRole(role);
      rd.setSupported(supportedCluster);
      if(log.isDebugEnabled()) {
        log.debug("New Relationship: type: \"" + type + "\" Role: \"" + role +
                  "\" Supported: \"" + supportedCluster + "\"");
      }
      aad.addRelationship(rd);
    } //while
    return newVal;
  }


  /**
   * Creates the property, fills in the slots based on what's in the
   * prototype-ini file and then sets it for (or adds it to) the asset
   **/
  protected int setPropertyForAsset(String prop, int newVal,
                                    StreamTokenizer tokens, AgentAssetData aad)
    throws IOException
  {
    String propertyName = prop.substring(1, prop.length()-1).trim();
    if(log.isDebugEnabled()) {
      log.debug("Creating PropGroupData: " + propertyName);
    }
    PropGroupData pgData = new PropGroupData(propertyName);
    PGPropData propData = null;
    try {
      newVal = tokens.nextToken();
      String member = tokens.sval;
      // Parse through the property section of the file
      while (newVal != StreamTokenizer.TT_EOF) {
        if ((tokens.ttype == StreamTokenizer.TT_WORD)
            && !(tokens.sval.substring(0,1).equals("["))) {
          propData = new PGPropData();
          propData.setName(member);
          newVal = tokens.nextToken();
          String dataType = tokens.sval;
          newVal = tokens.nextToken();
          propData.setType(getType(dataType));
          String subType = getSubType(dataType);
          if(subType != null) {
            propData.setSubType(subType);
          }
          propData.setValue(getValue(parseArgs(dataType, tokens.sval)));

          newVal = tokens.nextToken();
          member = tokens.sval;
          pgData.addProperty(propData);
        } else {
          // Reached a left bracket "[", want to exit block
          break;
        }
      } //while

      // Add the property to the asset
      aad.addPropertyGroup(pgData);
    } catch (IOException e) {
      throw e;
    } catch (Exception e) {
      if(log.isErrorEnabled()) {
        log.error("Exception during parse", e);
      }
    }
    return newVal;
  }

  private String parseHomeLocation(String argument) {
    return argument.substring(argument.indexOf("=")+1, argument.indexOf(","));
  }

  private Object getValue(Object arg) {
    if(arg instanceof Collection) {
      Iterator iter = ((Collection)arg).iterator();
      PGPropMultiVal multi = new PGPropMultiVal();
      while(iter.hasNext()) {
        multi.addValue((String)iter.next());
      }
      return multi;
    } else {
      return arg;
    }
  }

  private PropGroupData setTypeIdentificationPG() {
    PropGroupData pgData = new PropGroupData(PropGroupData.TYPE_IDENTIFICATION);
    PGPropData propData = new PGPropData();
    propData.setName("TypeIdentification");
    propData.setType("String");
    propData.setValue("UTC/RTOrg");
    pgData.addProperty(propData);
    
    return pgData;
  }

  private PropGroupData setItemIdentificationPG(String id, 
                                                String nomenclature, 
                                                String altid) {

    if(log.isDebugEnabled()) {
      log.debug("In setItemIdentificationPG("+id+", "+nomenclature+", "+altid+")");
    }

    PropGroupData pgData = new PropGroupData(PropGroupData.ITEM_IDENTIFICATION);
    PGPropData propData = new PGPropData();
    propData.setName("ItemIdentification");
    propData.setType("String");
    propData.setValue(id);
    pgData.addProperty(propData);

    propData = new PGPropData();
    propData.setName("Nomenclature");
    propData.setType("String");
    propData.setValue(nomenclature);
    pgData.addProperty(propData);

    // Note: Currently the database doesn't support an AltId for Item.
    // why is this?  
//     propData = new PGPropData();
//     propData.setName("AlternateItemIdentification");
//     propData.setType("String");
//     propData.setValue(altid);
//     pgData.addProperty(propData);

    return pgData;
  }

  private PropGroupData setClusterPG(String name) {
    if(log.isDebugEnabled()) {
      log.debug("Added ClusterPG: " + name);
    }

    PropGroupData pgData = new PropGroupData(PropGroupData.CLUSTER);
    PGPropData propData = new PGPropData();
    propData.setName("ClusterIdentifier");
    propData.setType("ClusterIdentifier");
    propData.setValue(name);
    pgData.addProperty(propData);

    return pgData;
  }

  private PropGroupData setCommunityPG() {
    PropGroupData pgData = new PropGroupData(PropGroupData.COMMUNITY);
    PGPropData propData = new PGPropData();
    propData.setName("TimeSpan");
    propData.setType("TimeSpan");
    propData.setValue("");       // Leave empty
    pgData.addProperty(propData);
    
    propData = new PGPropData();
    propData.setName("Communities");
    propData.setType("Collection");
    propData.setSubType("String");
    PGPropMultiVal values = new PGPropMultiVal();
    values.addValue("COUGAAR");
    propData.setValue(values);
    pgData.addProperty(propData);

    // add in default community
    //    communityPG.setTimeSpan(DEFAULT_START_TIME, DEFAULT_END_TIME);

    return pgData;
  }


  protected int setAssignmentPG(int newVal, 
                                StreamTokenizer tokens,
                                AgentAssetData aad) {

    PropGroupData pgData = new PropGroupData(PropGroupData.ASSIGNMENT);
    PGPropData propData = null;

    try {
      newVal = tokens.nextToken();
      // Parse through the property section of the file
      while (newVal != StreamTokenizer.TT_EOF) {
        propData = new PGPropData();
        propData.setName(tokens.sval);

        if ((tokens.ttype == StreamTokenizer.TT_WORD) && 
            !(tokens.sval.substring(0,1).equals("["))) {

          newVal = tokens.nextToken();
          propData.setType(tokens.sval);
          newVal = tokens.nextToken();
          propData.setValue(tokens.sval);
          newVal = tokens.nextToken();
          pgData.addProperty(propData);
        } else {
          // Reached a left bracket "[", want to exit block
          break;
        }
      } //while
    } catch (Exception e) {
      if(log.isErrorEnabled()) {
        log.error("Exception", e);
      }
    }
    aad.addPropertyGroup(pgData);
    return newVal;
  }

  // This needs to be modified to just return values.
  // It should not create the types.
  protected Object parseArgs(String type, String arg) {
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

  private String getType(String type) {
    int i;
    if ((i = type.indexOf("<")) > -1) { // deal with collections 
      return type.substring(0,i);
    } else {
      return type;
    }
  }

  private String getSubType(String type) {
    int i;
    if ((i = type.indexOf("<")) > -1) { // deal with collections 
      int j = type.lastIndexOf(">");
      return type.substring(i+1,j);
    } else {
      return type;
    }
  }

  private static class TrivialTimeSpan implements TimeSpan {
    long myStart;
    long myEnd;

    public TrivialTimeSpan(long start, long end) {
      myStart = start;
      myEnd = end;
    }

    public long getStartTime() {
      return myStart;
    }

    public long getEndTime() {
      return myEnd;
    }
  }

}// PrototypeParser
