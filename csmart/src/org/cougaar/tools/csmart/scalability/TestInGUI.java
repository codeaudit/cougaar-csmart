/* 
 * <copyright>
 * This software is to be used only in accordance with the COUGAAR license
 * agreement. The license agreement and other information can be found at
 * http://www.cougaar.org
 * 
 *       © Copyright 2001 by BBNT Solutions LLC.
 * </copyright>
 */
package org.cougaar.tools.csmart.scalability;

import org.cougaar.tools.server.ConfigurationWriter;
import org.cougaar.tools.csmart.ui.component.*;
import javax.swing.JButton;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;

public class TestInGUI extends TestGUI {
    ScalabilityXSociety society;

    public TestInGUI(ScalabilityXSociety society) {
        this.society = society;
        displayInFrame(society);
    }

    protected JButton[] getTestButtons() {
        JButton button1 = new JButton("Test");
        button1.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                society.getProperty(ScalabilityXSociety.PROP_AGENTCOUNT)
                    .setValue(new int[] {1, 2, 3});
                society.getProperty(ScalabilityXSociety.PROP_LEVELCOUNT)
                    .setValue(new Integer(3));
            }
        });
        JButton button2 = new JButton("Revert");
        button2.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                society.getProperty(ScalabilityXSociety.PROP_LEVELCOUNT)
                    .setValue(new Integer(1));
                society.getProperty(ScalabilityXSociety.PROP_AGENTCOUNT)
                    .setValue(new int[] {1});
            }
        });
        JButton button3 = new JButton("Write Ini Files");
        button3.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
//                  NodeComponent[] nodes = society.getNodes();
//                  for (int i = 0; i < nodes.length; i++) {
//                      try {
//                          ConfigurationWriter cw =
//                              (ConfigurationWriter) nodes[i];
//                          cw.writeConfigFiles(new File("."));
//                      } catch (Exception ex) {
//                          ex.printStackTrace();
//                      }
//                  }
            }
        });
        return new JButton[] {button1, button2, button3};
    }
    public static void main(String[] args) {
        ScalabilityXSociety society = new ScalabilityXSociety("test1");
        society.initProperties();
        new TestInGUI(society);
    }
}
