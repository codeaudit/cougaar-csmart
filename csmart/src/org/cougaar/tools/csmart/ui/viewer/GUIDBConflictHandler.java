/*
 * <copyright>
 *  
 *  Copyright 2000-2004 BBNT Solutions, LLC
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

package org.cougaar.tools.csmart.ui.viewer;

import org.cougaar.tools.csmart.core.db.DBConflictHandler;

import javax.swing.*;
import java.awt.*;

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
