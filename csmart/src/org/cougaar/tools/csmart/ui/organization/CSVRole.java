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
 * Roles for an organization; from a org_role.csv file
 * Note that there may be more than one entry per organization.
 */

public class CSVRole {
  private String baseOrgId;
  private String suffix;
  private String role;
  private String echelonOfSupport;
  private String roleMechanism;

  /**
   * Creates an object which records information about an organization's
   * roles, read from a csv file.
   * @param baseOrgId name of the organization
   * @param suffix suffix for the organization
   * @param role organization's role
   * @param echelonOfSupport echelon of support for this role
   * @param roleMechanism mechanism for this role
   */
  public CSVRole(String baseOrgId, String suffix,
                 String role, String echelonOfSupport, String roleMechanism) {
    this.baseOrgId = baseOrgId;
    this.suffix = suffix;
    this.role = role;
    this.echelonOfSupport = echelonOfSupport;
    this.roleMechanism = roleMechanism;
  }

  /**
   * Returns name of the organization, i.e. baseOrgId . suffix
   * @return organization name
   */
  public String getName() {
    StringBuffer sb = new StringBuffer();
    sb.append(baseOrgId);
    sb.append(".");
    sb.append(suffix);
    return sb.toString();
  }

  /**
   * Returns name of organization and comma separated fields
   * describing the role
   * @return baseOrgId.suffix, role, echelonOfSupport, roleMechanism
   */
    public String toString() {
    StringBuffer sb = new StringBuffer();
    sb.append(baseOrgId);
    sb.append(".");
    sb.append(suffix);
    sb.append(",");
    sb.append(role);
    sb.append(",");
    sb.append(echelonOfSupport);
    sb.append(",");
    sb.append(roleMechanism);
    return sb.toString();
  }

  /**
   * Appends an element describing the role to the XML document.
   * @param doc XML document
   * @param element parent element for the new role element
   */
  public void appendXML(Document doc, Element element) {
    Element facet = doc.createElement("facet");
    facet.setAttribute("role", role);
    facet.setAttribute("echelon_of_support", echelonOfSupport);
    if (roleMechanism != "")
      facet.setAttribute("mechanism", roleMechanism);
    element.appendChild(facet);
  }
}

