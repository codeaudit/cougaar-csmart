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

package org.cougaar.tools.csmart.ui.monitor.topology;

import java.util.*;
import javax.swing.JOptionPane;
import org.cougaar.tools.csmart.ui.monitor.viewer.CSMARTUL;
import org.cougaar.tools.csmart.ui.util.ClientServletUtil;
import org.cougaar.tools.csmart.ui.util.ServletResponse;

public class TopologyService {
  ArrayList parameterNames;
  ArrayList parameterValues;

  StringBuffer errors = null; // Collection of errors from Agents

  public TopologyService() {
    parameterNames = new ArrayList(2);
    parameterValues = new ArrayList(2);
    parameterNames.add("method");
    parameterValues.add("entries");
    parameterNames.add("format");
    parameterValues.add("csvdata");
  }

  public ArrayList getAgentLocations() {
    errors = new StringBuffer(200); // re-init the errors received
    Vector urls = CSMARTUL.getAgentURLs();
    if (urls == null)
      return null;
    for (int i = 0; i < urls.size(); i++) {
      // Bug 1585: If the servlet is not loaded, don't
      // pop up an error message for each Agent, but instead want one for all of them
      ArrayList results = getAgentLocations((String)urls.get(i));
      if (results != null)
        return results;
    }
    // If we fall through to here, no one could answer the question. Put up an error.
    JOptionPane.showMessageDialog(null, errors.toString());
    errors = null;
    return null;
  }

  private ArrayList getAgentLocations(String url) {
    ServletResponse response =
      ClientServletUtil.getCollectionFromAgent(url,
                                               "/topology",
                                               parameterNames,
                                               parameterValues,
                                               null, -1);
    Collection result = response.getCollection();
    String s = response.getErrorMessage();
    if (s != null) {
      // Had an error of some sort
      errors.append("Contacting: ");
      errors.append(response.getURL());
      errors.append(" ");
      errors.append(response.getErrorMessage());
      errors.append('\n');
    } 
    ArrayList resultList = null;
    if (result == null)
      return resultList;
    if (result instanceof ArrayList) 
      resultList = (ArrayList)result;
    else
      resultList = new ArrayList(result);
    Collections.sort(resultList);
    return resultList;
    // for debugging
    //    return getFakeData();
  }

  ///////////////////////////////////////////
  // for debugging
  private int incarnation = -1;

  private ArrayList getFakeData() {
    incarnation++;
    if (incarnation == 0) 
      return null; // first time, return no data
    String inc = String.valueOf(incarnation);
    ArrayList tmp = new ArrayList();
    tmp.add("victoria, 10-TCBN-NODE, 10-TCBN-HHC," + inc + ",false,true,false");
    tmp.add("rainier, 1-BDE-NODE, 11-ENGBN," + inc + ",false,true,false");
    tmp.add("rainier, 1-BDE-NODE, 1-64-ARBN," + inc + ",false,true,false");
    tmp.add("rainier, 1-BDE-NODE, 1-9-FABN," + inc + ",false,true,false");
    tmp.add("rainier, 1-BDE-NODE, 1-BDE-3ID-HHC," + inc + ",false,true,false");
    tmp.add("rainier, 1-BDE-NODE, 26-FSB," + inc + ",false,true,false");
    tmp.add("rainier, 1-BDE-NODE, 3-15-INFBN," + inc + ",false,true,false");
    tmp.add("rainier, 1-BDE-NODE, 3-7-INFBN," + inc + ",false,true,false");
    tmp.add("mtblanc, 24-SPTGP-NODE, 92-ENGBN-CBTHVY," + inc + ",false,true,false");
    tmp.add("mtblanc, 24-SPTGP-NODE, 10-TCBN-HHC," + inc + ",false,true,false");
    tmp.add("mtblanc, 24-SPTGP-NODE, 110-QMCO-POLSPLY," + inc + ",false,true,false");
    tmp.add("mtblanc, 24-SPTGP-NODE, 119-TCCO-CGO," + inc + ",false,true,false");
    tmp.add("mtblanc, 24-SPTGP-NODE, 147-MEDLOGBN," + inc + ",false,true,false");
    tmp.add("mtblanc, 24-SPTGP-NODE, 157-QMCO-FSVC," + inc + ",false,true,false");
    tmp.add("mtblanc, 24-SPTGP-NODE, 180-TCBN-HHD," + inc + ",false,true,false");
    tmp.add("mtblanc, 24-SPTGP-NODE, 186-WTRPURDET," + inc + ",false,true,false");
    tmp.add("mtblanc, 24-SPTGP-NODE, 18-QMPLT," + inc + ",false,true,false");
    tmp.add("mtblanc, 24-SPTGP-NODE, 202-WTRPURDET," + inc + ",false,true,false");
    tmp.add("mtblanc, 24-SPTGP-NODE, 21-MED-CSHOSP," + inc + ",false,true,false");
    tmp.add("mtblanc, 24-SPTGP-NODE, 226-QMCO-SPLY," + inc + ",false,true,false");
    tmp.add("mtblanc, 24-SPTGP-NODE, 24-CSB-HHD," + inc + ",false,true,false");
    tmp.add("mtblanc, 24-SPTGP-NODE, 24-ODCO," + inc + ",false,true,false");
    tmp.add("mtblanc, 24-SPTGP-NODE, 24-SPTGP-HHC," + inc + ",false,true,false");
    tmp.add("mtblanc, 24-SPTGP-NODE, 259-QMCO-FSVC," + inc + ",false,true,false");
    tmp.add("mtblanc, 24-SPTGP-NODE, 343-QMCO-SPLY," + inc + ",false,true,false");
    tmp.add("mtblanc, 24-SPTGP-NODE, 36-MEDEVACBN-GAMBCO," + inc + ",false,true,false");
    tmp.add("mtblanc, 24-SPTGP-NODE, 396-TCCO-PLS," + inc + ",false,true,false");
    tmp.add("mtblanc, 24-SPTGP-NODE, 406-TCDET-TPT," + inc + ",false,true,false");
    tmp.add("mtblanc, 24-SPTGP-NODE, 416-TKCO-POL," + inc + ",false,true,false");
    tmp.add("mtblanc, 24-SPTGP-NODE, 418-TKCO-POL," + inc + ",false,true,false");
    tmp.add("mtblanc, 24-SPTGP-NODE, 512-QMCO-WTRSPLY," + inc + ",false,true,false");
    tmp.add("mtblanc, 24-SPTGP-NODE, 553-CSB-HHD," + inc + ",false,true,false");
    tmp.add("mtblanc, 24-SPTGP-NODE, 571-MEDDET-AMBL," + inc + ",false,true,false");
    tmp.add("mtblanc, 24-SPTGP-NODE, 602-MAINTCO," + inc + ",false,true,false");
    tmp.add("mtblanc, 24-SPTGP-NODE, 61-ASMEDBN-MEDCO," + inc + ",false,true,false");
    tmp.add("mtblanc, 24-SPTGP-NODE, 632-MAINTCO," + inc + ",false,true,false");
    tmp.add("mtblanc, 24-SPTGP-NODE, 89-TKCO-CGO," + inc + ",false,true,false");
    tmp.add("mtblanc, 24-SPTGP-NODE, 96-TCCO-HET," + inc + ",false,true,false");
    tmp.add("victoria, 296-SPTBN-NODE, 296-SPTBN," + inc + ",false,true,false");
    tmp.add("victoria, 2-BDE-NODE, 10-ENGBN," + inc + ",false,true,false");
    tmp.add("victoria, 2-BDE-NODE, 1-41-FABN," + inc + ",false,true,false");
    tmp.add("victoria, 2-BDE-NODE, 2-7-INFBN," + inc + ",false,true,false");
    tmp.add("victoria, 2-BDE-NODE, 2-BDE-3ID-HHC," + inc + ",false,true,false");
    tmp.add("victoria, 2-BDE-NODE, 3-69-ARBN," + inc + ",false,true,false");
    tmp.add("victoria, 2-BDE-NODE, 3-FSB," + inc + ",false,true,false");
    tmp.add("victoria, 2-BDE-NODE, 4-64-ARBN," + inc + ",false,true,false");
    return tmp;
  }

}
