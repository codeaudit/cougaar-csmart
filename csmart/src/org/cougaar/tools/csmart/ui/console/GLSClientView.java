/*
 * <copyright>
 *  Copyright 2000-2003 BBNT Solutions, LLC
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

package org.cougaar.tools.csmart.ui.console;

import javax.swing.*;
import java.util.Observable;
import java.util.Observer;
import org.cougaar.mlm.ui.glsinit.GLSClient;

public class GLSClientView extends JComponent implements Observer {
  CSMARTConsoleModel model;

  public GLSClientView(CSMARTConsoleModel model) {
    this.model = model;
    model.addObserver(this);
  }

  /**
   * When the user selects the GLS init menu item,
   * add a new frame to the list of frames maintained by the model.
   */

  public void update(Observable o, Object arg) {
    if (arg.equals(CSMARTConsoleModel.SELECT_GLS_INIT)) {
    //TODO:  Uncomment and fix.
//      model.addFrame(getGLSClientView());
    }
  }

  /**
   * Create an internal frame that contains the GLSClient gui.
   */

  public JInternalFrame getGLSClientView() {
    // resizable, not closeable, maximizable, iconifiable
    JInternalFrame jif =
        new JInternalFrame("GLS", true, false, true, true);
    jif.getContentPane().add(new GLSClient("http", "localhost", "8800", "NCA"));
    jif.setSize(350, 400);
    jif.setLocation(0, 0);
    jif.setVisible(true);

    // I think that the JInternalFrame can only be iconified
    // after it's added to the desktop
    // this operation could be done in CSMARTConsoleView

    //    desktop.add(jif, JLayeredPane.DEFAULT_LAYER);
    //    try {
    //      jif.setIcon(true);
    //    } catch (PropertyVetoException e) {
    //    }
    return jif;
  }
}
