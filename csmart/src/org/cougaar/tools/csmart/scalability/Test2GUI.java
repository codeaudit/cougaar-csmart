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

import org.cougaar.tools.csmart.ui.component.*;
import javax.swing.JButton;
import java.awt.event.*;

public class Test2GUI extends TestGUI {
    ScalabilityXLevel level;

    public Test2GUI(ScalabilityXLevel level) {
        this.level = level;
        displayInFrame(level);
    }

    protected JButton[] getTestButtons() {
        return new JButton[0];
    }
    public static void main(String[] args) {
        ScalabilityXLevel level = new ScalabilityXLevel(1, null);
        level.initProperties();
        new Test2GUI(level);
    }
}
