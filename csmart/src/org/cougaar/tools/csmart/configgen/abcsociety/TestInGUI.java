/* 
 * <copyright>
 * This software is to be used only in accordance with the COUGAAR license
 * agreement. The license agreement and other information can be found at
 * http://www.cougaar.org
 * 
 *       © Copyright 2001 by BBNT Solutions LLC.
 * </copyright>
 */
package org.cougaar.tools.csmart.configgen.abcsociety;

import org.cougaar.tools.csmart.ui.component.*;
import javax.swing.JButton;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;

public class TestInGUI extends TestGUI {
    ABCSociety society;

    public TestInGUI(ABCSociety society) {
        this.society = society;
        displayInFrame(society);
    }

    protected JButton[] getTestButtons() {
        JButton button1 = new JButton("Test");
        button1.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                society.getProperty(ABCSociety.PROP_COMMUNITYCOUNT)
                    .setValue(new Integer(5));
                society.getProperty(ABCSociety.PROP_LEVELCOUNT)
                    .setValue(new Integer(3));
		society.getProperty(ABCSociety.PROP_STARTTIME)
		  .setValue(new Integer(0));
		society.getProperty(ABCSociety.PROP_STOPTIME)
		  .setValue(new Integer(25000));
            }
        });
        JButton button2 = new JButton("Revert");
        button2.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                society.getProperty(ABCSociety.PROP_COMMUNITYCOUNT)
                    .setValue(new Integer(8));
                society.getProperty(ABCSociety.PROP_LEVELCOUNT)
                    .setValue(new Integer(3));
		society.getProperty(ABCSociety.PROP_STARTTIME)
		  .setValue(new Integer(0));
		society.getProperty(ABCSociety.PROP_STOPTIME)
		  .setValue(new Integer(5000));
            }
        });
        JButton button3 = new JButton("Write Ini Files");
        button3.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
//                  try {
//                      society.writeConfigurationFiles(new File("."));
//                  } catch (Exception ex) {
//                      ex.printStackTrace();
//                  }
            }
        });
        return new JButton[] {button1, button2, button3};
    }
    public static void main(String[] args) {
        ABCSociety society = new ABCSociety("test1");
        society.initProperties();
        new TestInGUI(society);
    }
}
