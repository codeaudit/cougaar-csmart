/* 
 * <copyright>
 * This software is to be used only in accordance with the COUGAAR license
 * agreement. The license agreement and other information can be found at
 * http://www.cougaar.org
 * 
 *       © Copyright 2001 by BBNT Solutions LLC.
 * </copyright>
 */
package org.cougaar.tools.csmart.ui.experiment;

import java.io.IOException;
import java.io.File;
import java.util.List;

import org.cougaar.tools.server.ConfigurationWriter;

import org.cougaar.tools.csmart.ui.component.NodeComponent;
import org.cougaar.tools.csmart.ui.component.AgentComponent;
import org.cougaar.tools.csmart.ui.component.SocietyComponent;

/**
 * Write out all config data for an Experiment. Delegate
 * to each of the  Societies in turn, then each of the Impacts.<br>
 *
 * @author <a href="mailto:ahelsing@bbn.com">Aaron Helsinger</a>
 */
public class ExperimentConfigWriter implements ConfigurationWriter {
  NodeComponent[] nodes;
  List societies;
  List impacts;

  public ExperimentConfigWriter(List societies, List impacts, NodeComponent[] nodes) {
    this.nodes = nodes;
    this.societies = societies;
    this.impacts = impacts;
  }

    // IF there are Impacts (ABCImpacts?) then I need to get
    // the Node.ini modification line from the impacts,
    // and pass that to each of the societies for them to
    // insert before they write the rest
  public void writeConfigFiles(File configDir) throws IOException {
    String addition = null;
    for (int i = 0; i < impacts.size(); i++) {
      Impact impact = (Impact)impacts.get(i);
      addition = (addition != null ? addition : "") + impact.getNodeFileAddition();
      ConfigurationWriter writer = impact.getConfigurationWriter(nodes);
      if (writer != null)
	writer.writeConfigFiles(configDir);
    }
    for (int i = 0; i < societies.size(); i++) {
      ((SocietyComponent)societies.get(i)).getConfigurationWriter(nodes, addition).writeConfigFiles(configDir);
    }
  }
}
