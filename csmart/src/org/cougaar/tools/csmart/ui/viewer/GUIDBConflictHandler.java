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

package org.cougaar.tools.csmart.ui.viewer;

import java.awt.Component;
import javax.swing.JOptionPane;

import org.cougaar.tools.csmart.core.db.DBConflictHandler;

/**
 * Create a <code>JOptionPane</code> to prompt the user to handle
 * any conflicts in saving to the database. 
 * Created from <code>GUIUtils</code>.
 */
public class GUIDBConflictHandler implements DBConflictHandler {
  Component parent;

  public GUIDBConflictHandler(Component parent) {
    this.parent = parent;
  }

  public int handleConflict(Object msg, Object[] choices, 
                            Object defaultChoice) {
    return JOptionPane.showOptionDialog(parent,
                                        msg,
                                        "Database Conflict",
                                        JOptionPane.WARNING_MESSAGE,
                                        JOptionPane.DEFAULT_OPTION,
                                        null,
                                        choices,
                                        defaultChoice);
  }
}
