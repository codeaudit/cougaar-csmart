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

package org.cougaar.tools.csmart.ui.util;

import org.cougaar.tools.csmart.ui.viewer.CSMART;
import org.cougaar.util.ConfigFinder;
import org.cougaar.util.log.Logger;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.ArrayList;

/**
 * Utilities for CSMART GUI.
 */

public class Util {

  /**
   * Return a path for the specified filename; the path is determined
   * using ConfigFinder.  If there is any error, null is returned.
   * @param filename the filename for which to get the path
   * @return the pathname or null if an error
   */
  public static String getPath(String filename) {
    Logger log = CSMART.createLogger("org.cougaar.tools.csmart.ui.util.Util");
    ConfigFinder configFinder = ConfigFinder.getInstance();
    File file = configFinder.locateFile(filename);
    String path = null;
    if (file != null) {
      try {
	path = file.getCanonicalPath();
      } catch (Exception e) {
	if(log.isWarnEnabled()) {
	  log.warn("Could not find: " + filename);
	}
      }
    } else {
      if(log.isWarnEnabled()) {
	log.warn("Could not find: " + filename);
      }
    }
    return path;
  }

  /**
   * Get the single NamedFrame object for the CSMARTUL application.
   * The NamedFrame object ensures that all frames in the application
   * have unique names, and it notifies the CSMARTUL application when frames
   * are added/removed so that the menu of existing frames can be updated.
   * This method is pointless.
   * @deprecated The singleton NamedFrame should be obtained directly
   * using NamedFrame.getNamedFrame().
   * @return the NamedFrame object for the application
   **/
  public static NamedFrame getNamedFrame() {
    return NamedFrame.getNamedFrame();
  }

  /**
   * Display a list of objects in a dialog.
   * @param parent the parent component of the dialog
   * @param values a list of values
   * @param title the title for the dialog
   * @param prompt a prompt displayed above the list
   */
  public static void showObjectsInList(Component parent,
                                       ArrayList values, String title, 
                                       String prompt) {
    displayList(parent, values, title, prompt, false);
  }

  /**
   * Display a list of objects in a dialog and return an
   * array of objects selected by the user.
   * @param parent the parent component of the dialog
   * @param values a list of values
   * @param title the title for the dialog
   * @param prompt a prompt displayed above the list
   * @return the objects selected by the user
   */
  public static Object[] getObjectsFromList(Component parent,
                                            ArrayList values, String title,
                                            String prompt) {
    return displayList(parent, values, title, prompt, true);
  }

  private static  Object[] displayList(Component parent,
                                       ArrayList values, String title,
                                       String prompt, boolean allowSelection) {
    JList list = new JList(values.toArray());
    JScrollPane jsp = 
      new JScrollPane(list,
                      ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
                      ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
    jsp.setPreferredSize(new Dimension(400, 100));
    JPanel infoPanel = new JPanel();
    infoPanel.setLayout(new GridBagLayout());
    list.setBackground(infoPanel.getBackground());
    int x = 0;
    int y = 0;
    infoPanel.add(new JLabel(prompt),
                   new GridBagConstraints(x, y++, 1, 1, 0.0, 0.0,
                                          GridBagConstraints.WEST,
                                          GridBagConstraints.NONE,
                                          new Insets(10, 0, 5, 5),
                                          0, 0));
    infoPanel.add(jsp,
                   new GridBagConstraints(x, y++, 1, 1, 0.0, 0.0,
                                          GridBagConstraints.WEST,
                                          GridBagConstraints.NONE,
                                          new Insets(0, 0, 5, 0),
                                          0, 0));
    if (allowSelection) {
      int result = JOptionPane.showOptionDialog(parent, infoPanel, 
                                                title,
                                                JOptionPane.OK_CANCEL_OPTION,
                                                JOptionPane.PLAIN_MESSAGE,
                                                null, null, null);
      if (result == JOptionPane.OK_OPTION) 
        return list.getSelectedValues();
      else
        return null;
    } else {
      // FIXME: Bug 1929: Selection still possible.
      // maybe do list.setEnabled(false);?
      // But that greys out the text, making it hard to read
      //list.setEnabled(false);
      Object[] options = { "OK" };
      JOptionPane.showOptionDialog(parent, infoPanel,
                                   title,
                                   JOptionPane.OK_OPTION,
                                   JOptionPane.PLAIN_MESSAGE, null,
                                   options, options[0]);
      return null;
    }
  }



}




