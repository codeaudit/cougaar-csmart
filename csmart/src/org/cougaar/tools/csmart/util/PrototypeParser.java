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
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StreamTokenizer;
import java.util.Calendar;
import org.cougaar.tools.csmart.core.cdata.AgentAssetData;
import org.cougaar.tools.csmart.core.cdata.AgentComponentData;
import org.cougaar.tools.csmart.core.cdata.ComponentData;
import org.cougaar.tools.csmart.core.cdata.PGPropData;
import org.cougaar.tools.csmart.core.cdata.PGPropMultiVal;
import org.cougaar.tools.csmart.core.cdata.PropGroupData;
import org.cougaar.tools.csmart.core.cdata.RelationshipData;
import org.cougaar.tools.csmart.ui.viewer.CSMART;
import org.cougaar.util.ConfigFinder;
import org.cougaar.util.log.Logger;


/**
 * PrototypeParser.java
 *
 *
 * Created: Thu Feb 21 13:36:49 2002
 *
 * @author <a href="mailto:bkrisler@bbn.com">Brian Krisler</a>
 * @version 1.0
 */

public class PrototypeParser {

  private static Calendar myCalendar = Calendar.getInstance();

  private static long DEFAULT_START_TIME = -1;
  private static long DEFAULT_END_TIME = -1;

  private Logger log;


  static {
    myCalendar.set(1990, 0, 1, 0, 0, 0);
    DEFAULT_START_TIME = myCalendar.getTime().getTime();

    myCalendar.set(2010, 0, 1, 0, 0, 0);
    DEFAULT_END_TIME = myCalendar.getTime().getTime();   
  }

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

  private AgentAssetData parsePrototypeFile(String agentName) {
    String filename = agentName + "-prototype-ini.dat";

    // Use the same domainname for all org assets now
    String uic = "";
    String className = "";
    String unitName = null;
    String dataItem = "";
    int newVal;

    BufferedReader input = null;
    Reader fileStream = null;

    AgentAssetData aad = new AgentAssetData(null);
    aad.setType(AgentAssetData.ORG);

    try {
      fileStream = 
        new InputStreamReader(ConfigFinder.getInstance().open(filename));
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
          if (tokens.ttype == StreamTokenizer.TT_WORD) {
            dataItem = tokens.sval;
            if (dataItem.equals("[Prototype]")) {
              tokens.nextToken();
              className = tokens.sval;
              aad.setAssetClass(className);
              if(log.isDebugEnabled()) {
                log.debug("AgentAssetData Class: " + aad.getAssetClass());
              }
              newVal = tokens.nextToken();
            } else if (dataItem.equals("[UniqueId]")) {
              tokens.nextToken();
              aad.setUniqueID(tokens.sval);
              if(log.isDebugEnabled()) {
                log.debug("AgentAssetData UniqueID: " + aad.getUniqueID());
              }
              newVal = tokens.nextToken();
            } else if (dataItem.equals("[UnitName]")) {
              // This field is optional 
              tokens.nextToken();
              aad.setUnitName(tokens.sval);
              if(log.isDebugEnabled()) {
                log.debug("AgentAssetData UnitName: " + aad.getUnitName());
              }
              newVal = tokens.nextToken();
            } else if (dataItem.equals("[UIC]")) {
              if (className != null) {
                tokens.nextToken();
	      	uic = tokens.sval;

		// This is a silly fix to a dumb bug
		if (!uic.startsWith("UIC/")) {
		  uic = "UIC/" + uic;
		}
                aad.setUIC(uic);
                if(log.isDebugEnabled()) {
                  log.debug("AgentAssetData UIC: " + aad.getUIC());
                }
//                 aad.addPropertyGroup(setTypeIdentificationPG());
	      	// Use unitName if it occurred, else use agentName
                aad.addPropertyGroup(setItemIdentificationPG(uic, 
                                                 (unitName!=null) ? unitName : agentName,
                                                 agentName));

                aad.addPropertyGroup(setClusterPG(agentName));                
              } else {
                if(log.isErrorEnabled()) {
                  log.error("PrototypeParser Error: [Prototype] value is null");
                }
              }
              newVal = tokens.nextToken();
            } else if (dataItem.equals("[Relationship]")) {
              newVal = setRelationshipData(newVal, tokens, aad);
            } else if (dataItem.equals("[AssignmentPG]")) {
              newVal = setAssignmentPG(newVal, tokens, aad);
            } else if (dataItem.substring(0,1).equals("[")) {
              // We've got a property or capability
              newVal = setGenericPropertyPG(dataItem, newVal, tokens, aad);
            } else {
              // If the token you read is not one of the valid
              // choices from above
              if(log.isErrorEnabled()) {
                log.error("PrototypeParser Incorrect token: " + dataItem);
              }
            }
          } else {
            throw new RuntimeException("Format error in \""+filename+"\".");
          }
      }

      // For each organization, the following code sets
      // CapableRoles and Relationship slots for the
      // AssignedPG property
      // It adds the property to the organization and
      // adds the organization to ccv2 collections
//       NewAssignedPG assignedCap = (NewAssignedPG)getFactory().createPropertyGroup(AssignedPGImpl.class);
//       Collection roles =  org.getOrganizationPG().getRoles();
//       if (roles != null) {
//         assignedCap.setRoles(new ArrayList(roles));
//       }

//       org.setAssignedPG(assignedCap);
      
//       // set up this asset's available schedule
//       myCalendar.set(1990, 0, 1, 0, 0, 0);
//       Date start = myCalendar.getTime();
//       // set the end date of the available schedule to 01/01/2010
//       myCalendar.set(2010, 0, 1, 0, 0, 0);
//       Date end = myCalendar.getTime();
//       NewSchedule availsched = getFactory().newSimpleSchedule(start, end);
//       // set the available schedule
//       ((NewRoleSchedule)org.getRoleSchedule()).setAvailableSchedule(availsched);

//       if (relationship.equals(GLMRelationship.SELF)) {
//         Relationship selfRelationship = 
//           getFactory().newRelationship(Constants.Role.SELF, org, org,
//                                        ETERNITY);  
//         org.getRelationshipSchedule().add(selfRelationship);
//         org.setLocal(true);

//         publish(org);
//       	selfOrg = org;
//       } 

//       // Closing BufferedReader
//       if (input != null)
// 	input.close();

//       //only generates a NoSuchMethodException for AssetSkeleton because of a coding error
//       //if we are successul in creating it here  it then the AssetSkeletomn will end up with two copies
//       //the add/search criteria in AssetSkeleton is for a Vecotr and does not gurantee only one instance of 
//       //each class.  Thus the Org allocator plugin fails to recognixe the correct set of cpabilities.
      
    } catch (Exception e) {
      if(log.isErrorEnabled()) {
        log.error("Exception", e);
      }
    }

    return aad;
  } 


  protected int setRelationshipData(int newVal, 
                                    StreamTokenizer tokens,
                                    AgentAssetData aad) {

    if(log.isDebugEnabled()) {
      log.debug("Entering setRelationship");
    }

    RelationshipData rd = null;

    int x = 0;
    try {
      while (newVal != StreamTokenizer.TT_EOF) {
        rd = new RelationshipData();
        String organization_array[] = new String[3]; // An array of relationship, clusterName and capableroles triples
        newVal = tokens.nextToken();
        // Parse [Relationship] part of prototype-ini file
        if ((tokens.ttype == StreamTokenizer.TT_WORD) && 
            !(tokens.sval.substring(0,1).equals("["))) {
          rd.setType(tokens.sval);
          if(log.isDebugEnabled()) {
            log.debug("Relationship Type: " + rd.getType());
          }
          newVal = tokens.nextToken();
          rd.setItem(tokens.sval);
          if(log.isDebugEnabled()) {
            log.debug("Relationship Item: " + rd.getItem());
          }
          newVal = tokens.nextToken();
          rd.setRole(tokens.sval);
          if(log.isDebugEnabled()) {
            log.debug("Relationship Role: " + rd.getRole());
          }
        } else {
          if(log.isDebugEnabled()) {
            log.debug("break");
          }
          // Reached a left bracket "[", want to exit block
          break;
        }
        x++;
        aad.addRelationship(rd);
      } //while
    } catch (Exception e) {
      if(log.isErrorEnabled()) {
        log.error("Exception", e);
      }
    }

    if(log.isDebugEnabled()) {
      log.debug("Exiting setRelationship");
    }

    return newVal;
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

    propData = new PGPropData();
    propData.setName("AlternateItemIdentification");
    propData.setType("String");
    propData.setValue(altid);
    pgData.addProperty(propData);

    return pgData;
  }

  private PropGroupData setClusterPG(String name) {

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

    if(log.isDebugEnabled()) {
      log.debug("Entering setAssignmentForOrganization");
    }

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

  /**
   * Creates the property, fills in the slots based on what's in the prototype-ini file
   * and then sets it for (or adds it to) the organization
   */
  protected int setGenericPropertyPG(String prop, int newVal, 
                                           StreamTokenizer tokens, AgentAssetData aad) {

    if(log.isDebugEnabled()) {
      log.debug("Entering setPropertyForOrganization: " + prop);
    }

    String propertyName = prop.substring(1, prop.length()-1);

    PropGroupData pgData = new PropGroupData(propertyName);
    
    try {
      newVal = tokens.nextToken();
      String propName = "New" + propertyName;
      // Parse through the property section of the file
      while (newVal != StreamTokenizer.TT_EOF) {
        PGPropData propData = new PGPropData();
        propData.setName(tokens.sval);
        if ((tokens.ttype == StreamTokenizer.TT_WORD) && !(tokens.sval.substring(0,1).equals("["))) {
          newVal = tokens.nextToken();
          propData.setType(tokens.sval);
          newVal = tokens.nextToken();
          propData.setValue(tokens.sval);
          // Call appropriate setters for the slots of the property
//           Object arg = parseExpr(dataType, tokens.sval);

//           createAndCallSetter(property, propName, "set" + member, 
//                               getType(dataType), arg);
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


}// PrototypeParser
