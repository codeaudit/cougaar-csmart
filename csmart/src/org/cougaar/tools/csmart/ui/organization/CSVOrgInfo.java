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
  public String baseOrgId;
  public String suffix;
  public String superiorBaseOrgId;
  public String superiorSuffix;
  public String rollupCode;
  public boolean deleted = false;

  public CSVOrgInfo(String baseOrgId, String suffix,
                    String superiorBaseOrgId, String superiorSuffix,
                    String rollupCode) {
    this.baseOrgId = baseOrgId;
    this.suffix = suffix;
    this.superiorBaseOrgId = superiorBaseOrgId;
    this.superiorSuffix = superiorSuffix;
    this.rollupCode = rollupCode;
  }

  public String toString() {
    StringBuffer sb = new StringBuffer();
    sb.append(baseOrgId);
    sb.append(".");
    sb.append(suffix);
    return sb.toString();
  }

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

