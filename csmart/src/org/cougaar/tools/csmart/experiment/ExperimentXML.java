/**
 * <copyright>
 *  Copyright 2002-2003 BBNT Solutions, LLC
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
package org.cougaar.tools.csmart.experiment;

import org.cougaar.tools.csmart.core.cdata.ComponentData;
import org.cougaar.tools.csmart.core.cdata.ComponentDataXML;
import org.cougaar.tools.csmart.ui.viewer.CSMART;
import org.cougaar.util.log.Logger;

import java.io.File;
import java.io.IOException;

/**
 * ExperimentXML.java
 *
 * Allows the conversion of an Experiment to XML and from XML to
 * an Experiment ComponentData (Host / Node / Agent mapping)
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
  
  /**
   * Parses the specified file into and Experiment ComponentData.
   * An Experiment ComponentData contains no detailed Agent information.
   * Just Host, Node, Agent mappings.
   *
   * @param filename - Name of file to parse
   * @return a <code>ComponentData</code> value
   */
  public ComponentData parseExperimentFile(String filename) {
    // TODO: Parse out and store Agent details from the new ComponentData
    // this should be just a pure HNA mapping.
    if(filename == null || filename.equals("")) {
      if(log.isErrorEnabled()) {
        log.error("Invaild filename: " + filename);
      }
      return null;
    }

    return createComponentData(filename);

  }

  /**
   * Parses the specified file into and Experiment ComponentData.
   * An Experiment ComponentData contains no detailed Agent information.
   * Just Host, Node, Agent mappings.
   *
   * @param file - Handle to file to parse.
   * @return a <code>ComponentData</code> value
   */
  public ComponentData parseExperimentFile(File file) {
    // TODO: Parse out and store Agent details from the new ComponentData
    // this should be just a pure HNA mapping.
    return createComponentData(file);

  }

  /**
   * Creates an Experiment XML file from a ComponentData object.
   *
   * @param data ComponentData to convert into XML
   * @param configDir - Directory to store file.
   */
  public void createExperimentFile(ComponentData data, File configDir) {
    // TODO: Parse out all Agent details from the component data.
    // make it a pure HNA mapping.
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
