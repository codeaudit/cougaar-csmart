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
 * Support for an organization; from a org_support_cmd_assign.csv file
 * Note that there may be more than one entry per organization.
 */

public class CSVSupportOrg {
  String baseOrgId;
  String suffix;
  String supportedBaseOrgId;
  String supportedSuffix;
  String echelonOfSupport;

  public CSVSupportOrg(String baseOrgId, String suffix,
                       String supportedBaseOrgId, String supportedSuffix,
                       String echelonOfSupport) {
    this.baseOrgId = baseOrgId;
    this.suffix = suffix;
    this.supportedBaseOrgId = supportedBaseOrgId;
    this.supportedSuffix = supportedSuffix;
    this.echelonOfSupport = echelonOfSupport;
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
    sb.append(supportedBaseOrgId);
    sb.append(".");
    sb.append(supportedSuffix);
    sb.append(",");
    sb.append(echelonOfSupport);
    return sb.toString();
  }

  public void appendXML(Document doc, Element element) {
    Element facet = doc.createElement("facet");
    facet.setAttribute("sca_supported_org", 
                       supportedBaseOrgId + "." + supportedSuffix);
    facet.setAttribute("sca_echelon_of_support", echelonOfSupport);
    element.appendChild(facet);
  }
}

