/* 
 * <copyright>
 *  
 *  Copyright 2001-2004 BBNT Solutions, LLC
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

package org.cougaar.tools.csmart.ui.monitor.community;

import att.grappa.*;
import org.cougaar.tools.csmart.ui.monitor.PropertyNames;
import org.cougaar.tools.csmart.ui.viewer.CSMART;
import org.cougaar.util.log.Logger;

import javax.swing.table.AbstractTableModel;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Vector;

/**
 * Provide the values for the attribute table for the specified node
 * in a community graph.
 * This gets the attributes from the grappa Node and returns the
 * values to display in the attribute table.
 */

public class ULCommunityTableModel extends AbstractTableModel {
  Vector names;
  Vector values;
  Node node;
  private transient Logger log;

  /**
   * Create table model for the specified node.
   * @param node the node for which to create the table model
   */

  public ULCommunityTableModel(Node node) {
    this.node = node;
    names = new Vector();
    values = new Vector();
    addAttribute(PropertyNames.COMMUNITY_MEMBERS);
    makePrettyNames();
    createLogger();
  }

  private void createLogger() {
    log = CSMART.createLogger(this.getClass().getName());
  }

  public int getColumnCount() { 
    return 2;
  }

  public int getRowCount() { 
    return names.size();
  }

  public String getColumnName(int col) {
    if (col == 0)
      return "Name";
    else if (col == 1)
      return "Value";
    return "";
  }

  public Object getValueAt(int row, int col) { 
    if (col == 0)
      return names.elementAt(row);
    else if (col == 1)
      return values.elementAt(row);
    return "";
  }

  /** 
   * Modify the names by substituting spaces for underlines.
   */

  private void makePrettyNames() {
    for (int i = 0; i < names.size(); i++) {
      String name = (String)names.elementAt(i);
      name = name.replace('_', ' ');
      names.setElementAt(name, i);
    }
  }

  private void addAttribute(String name) {
    Attribute a = node.getLocalAttribute(name);
    if (a == null) {
//       if(log.isWarnEnabled()) {
//         log.warn("ULCommunityTableModel: attribute not found: " +
//                  name);
//       }
      return;
    }
    names.addElement(name);
    values.addElement(a.getValue());
  }

  private void readObject(ObjectInputStream ois)
    throws IOException, ClassNotFoundException
  {
    ois.defaultReadObject();
    createLogger();
  }
    
}
