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
package org.cougaar.tools.csmart.experiment;

import java.io.File;
import java.io.IOException;
import org.cougaar.tools.csmart.core.cdata.ComponentData;
import org.cougaar.tools.csmart.core.cdata.ComponentDataXML;
import org.cougaar.tools.csmart.ui.viewer.CSMART;
import org.cougaar.util.log.Logger;

/**
 * ExperimentXML.java
 *
 *
 * Created: Wed Jun  5 11:38:56 2002
 *
 * @author <a href="mailto:bkrisler@bbn.com">Brian Krisler</a>
 * @version 1.0
 */

public class ExperimentXML extends ComponentDataXML {
  private Logger log;

  public ExperimentXML (){
    log = CSMART.createLogger("org.cougaar.tools.csmart.experiment.ExperimentDump");
  }
  
  public ComponentData parseExperimentFile(String filename) {
    if(filename == null || filename.equals("")) {
      if(log.isErrorEnabled()) {
        log.error("Invaild filename: " + filename);
      }
      return null;
    }

    return createComponentData(filename);

  }

  public void createExperimentFile(ComponentData data, File configDir) {
    if(data == null) {
      if(log.isErrorEnabled()) {
        log.error("Cannot create Experiment XML File, ComponentData was null!");
      }
      return;
    }
    try {
      writeXMLFile(configDir, createXMLDocument(data), data.getName());
    } catch(IOException ioe) {
      if(log.isErrorEnabled()) {
        log.error("Caught an Excpetion trying to write Experiment XML File", ioe);
      }
    }
  }

  public static void main(String[] args) {
    ExperimentXML exp = new ExperimentXML();
    ComponentData data = exp.parseExperimentFile(args[0]);
    String dump = org.cougaar.tools.csmart.core.cdata.CDataDebugUtils.createDataDump(data);

    System.out.println(dump);

    File file = new File("/tmp/");
    exp.createExperimentFile(data, file);
  }

}// ExperimentXML
