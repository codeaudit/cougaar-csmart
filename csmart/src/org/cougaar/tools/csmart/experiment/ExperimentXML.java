/**
 * <copyright>
 *  
 *  Copyright 2002-2004 BBNT Solutions, LLC
 *  under sponsorship of the Defense Advanced Research Projects
 *  Agency (DARPA).
 * 
 *  You can redistribute this software and/or modify it under the
 *  terms of the Cougaar Open Source License as published on the
 *  Cougaar Open Source Website (www.cougaar.org).
 * 
 *  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 *  "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 *  LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 *  A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 *  OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 *  SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 *  LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 *  DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 *  THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 *  OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *  
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
  private ComponentData parseExperimentFile(String filename) {
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
