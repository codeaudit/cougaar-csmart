/*
 * <copyright>
 *  Copyright 2000-2001 BBNT Solutions, LLC
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

import java.awt.Color;
import javax.swing.*;

import org.cougaar.tools.server.NodeEvent;

public class ConsoleFontChooser extends JDialog {

  public ConsoleFontChooser() {
    // create a modal dialog
    super((java.awt.Frame)null, "Select Font", true); 

    JPanel panel = new JPanel();
    // define the gui here
    getContentPane().add(panel);
    pack();
  }

  // implement these methods to return the values the user selects
  // message type will be one of the types defined in NodeEvent
  // plus define two new types in this file (SEARCH and NOTIFY)

  public int getFontSize(int messageType) {
    return 0;
  }

  public String getFontStyle(int messageType) {
    return "";
  }

  public Color getFontColor(int messageType) {
    return null;
  }

  // for testing
  public static void main(String[] args) {
    ConsoleFontChooser fontChooser = new ConsoleFontChooser();
    fontChooser.setVisible(true);
  }
}
