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
 * Information returned from a CSV file for each organization.
 * Used as the "user object" of the tree nodes.
 */

public class CSVOrgInfo {
  public String origOrgId;
  public String baseOrgId;
  public String suffix;
  public String superiorBaseOrgId;
  public String superiorSuffix;
  public boolean deleted = false;

  /**
   * Construct an object that maintains information about an
   * organization and its superior, which was read from a csv file.
   * @param baseOrgId the organization
   * @param suffix suffix for the organization
   * @param superiorBaseOrgId organization's superior
   * @param superiorSuffix suffix for the superior
   */
  public CSVOrgInfo(String origOrgId, String baseOrgId, String suffix,
                    String superiorBaseOrgId, String superiorSuffix) {
    this.origOrgId = origOrgId;
    this.baseOrgId = baseOrgId;
    this.suffix = suffix;
    this.superiorBaseOrgId = superiorBaseOrgId;
    this.superiorSuffix = superiorSuffix;
  }

  /**
   * Returns baseOrgId.suffix
   * @return name of the organization
   */
  public String toString() {
    StringBuffer sb = new StringBuffer();
    sb.append(baseOrgId);
    sb.append(".");
    sb.append(suffix);
    return sb.toString();
  }

  /**
   * Creates an agent XML element for the document.
   * @param doc XML document
   * @return the agent element
   */
  public Element toXML(Document doc) {
    Element element = doc.createElement("agent");
    element.setAttribute("name", toString());
    element.setAttribute("classname", "org.cougaar.core.agent.SimpleAgent");
    Element facet = doc.createElement("facet");
    facet.setAttribute("org_id", toString());
    element.appendChild(facet);
    facet = doc.createElement("facet");
    facet.setAttribute("superior_org_id", 
                       superiorBaseOrgId + "." + superiorSuffix);
    element.appendChild(facet);
    return element;
  }
}

