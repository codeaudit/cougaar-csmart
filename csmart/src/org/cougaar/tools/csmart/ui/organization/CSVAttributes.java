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

package org.cougaar.tools.csmart.ui.organization;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Attributes for an organization; from a org_attribute.csv file
 */

public class CSVAttributes {
  String baseOrgId;
  String suffix;
  String combatSupportEchelon;
  String echelon;
  String echelonGroup;
  boolean isDeployable;
  boolean hasPhysicalAssets;
  boolean hasEquipmentAssets;
  boolean hasPersonnelAssets;
  String uic;
  String homeLocation;
  
  public CSVAttributes(String baseOrgId, String suffix,
                       String combatSupportEchelon, String echelon,
                       String echelonGroup,
                       boolean isDeployable, boolean hasPhysicalAssets,
                       boolean hasEquipmentAssets, boolean hasPersonnelAssets,
                       String uic, String homeLocation) {
    this.baseOrgId = baseOrgId;
    this.suffix = suffix;
    this.combatSupportEchelon = combatSupportEchelon;
    this.echelon = echelon;
    this.echelonGroup = echelonGroup;
    this.isDeployable = isDeployable;
    this.hasPhysicalAssets = hasPhysicalAssets;
    this.hasEquipmentAssets = hasEquipmentAssets;
    this.hasPersonnelAssets = hasPersonnelAssets;
    this.uic = uic;
    this.homeLocation = homeLocation;
  }
  
  public String getName() {
    StringBuffer sb = new StringBuffer();
    sb.append(baseOrgId);
    sb.append(".");
    sb.append(suffix);
    return sb.toString();
  }
  
  public String toString() {
    StringBuffer sb = new StringBuffer();
    sb.append(baseOrgId);
    sb.append(".");
    sb.append(suffix);
    sb.append(",");
    sb.append(combatSupportEchelon);
    sb.append(",");
    sb.append(echelon);
    sb.append(",");
    sb.append(echelonGroup);
    sb.append(",");
    sb.append(isDeployable);
    sb.append(",");
    sb.append(hasPhysicalAssets);
    sb.append(",");
    sb.append(hasEquipmentAssets);
    sb.append(",");
    sb.append(hasPersonnelAssets);
    sb.append(",");
    sb.append(uic);
    sb.append(",");
    sb.append(homeLocation);
    return sb.toString();
  }
  
  private void addFacet(Document doc, Element element, 
                        String name, String value) {
    Element facet = doc.createElement("facet");
    facet.setAttribute(name, value);
    element.appendChild(facet);
  }
  
  private void addFacet(Document doc, Element element, 
                        String name, boolean flag) {
    if (flag)
      addFacet(doc, element, name, "T");
    else
      addFacet(doc, element, name, "F");
  }
  
  public void appendXML(Document doc, Element element) {
    addFacet(doc, element, "home_location", homeLocation);
    addFacet(doc, element, "uic", uic);
    addFacet(doc, element, "combat_support", combatSupportEchelon);
    addFacet(doc, element, "echelon", echelon);
    addFacet(doc, element, "echelon_group", echelonGroup);
    addFacet(doc, element, "is_deployable", isDeployable);
    addFacet(doc, element, "has_physical_assets", hasPhysicalAssets);
    addFacet(doc, element, "has_equipment_assets", hasEquipmentAssets);
    addFacet(doc, element, "has_personnel_assets", hasPersonnelAssets);
  }
}
