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

package org.cougaar.tools.csmart.ui.monitor.xml;

import java.util.*;
import javax.swing.table.AbstractTableModel;
import att.grappa.*;

import org.cougaar.tools.csmart.ui.monitor.PropertyNames;
import org.cougaar.util.log.Logger;
import org.cougaar.tools.csmart.ui.viewer.CSMART;
import java.io.ObjectInputStream;
import java.io.IOException;

/**
 * Provide the values for the attribute table for the specified node.
 * This gets the attributes from the grappa Node and returns the
 * values to display in the attribute table.
 */

public class XMLTableModel extends AbstractTableModel {
  Vector names;
  Vector values;
  Node node;
  private transient Logger log;
  private static Vector grappaKeys = null;

  /**
   * Create table model for the specified node.
   * @param node the node for which to create the table model
   */

  public XMLTableModel(Node node) {
    this.node = node;
    // these are attributes NOT to display in the table
    if (grappaKeys == null) {
      grappaKeys = new Vector();
      grappaKeys.add(GrappaConstants.BBOX_ATTR);
      grappaKeys.add(GrappaConstants.CLUSTERRANK_ATTR);
      grappaKeys.add(GrappaConstants.COLOR_ATTR);
      grappaKeys.add(GrappaConstants.CUSTOM_ATTR);
      grappaKeys.add(GrappaConstants.DIR_ATTR);
      grappaKeys.add(GrappaConstants.DISTORTION_ATTR);
      grappaKeys.add(GrappaConstants.FONTCOLOR_ATTR);
      grappaKeys.add(GrappaConstants.FONTNAME_ATTR);
      grappaKeys.add(GrappaConstants.FONTSIZE_ATTR);
      grappaKeys.add(GrappaConstants.FONTSTYLE_ATTR);
      grappaKeys.add(GrappaConstants.HEIGHT_ATTR);
      grappaKeys.add(GrappaConstants.IMAGE_ATTR);
      grappaKeys.add(GrappaConstants.LABEL_ATTR);
      grappaKeys.add(GrappaConstants.LP_ATTR);
      grappaKeys.add(GrappaConstants.MARGIN_ATTR);
      grappaKeys.add(GrappaConstants.MCLIMIT_ATTR);
      grappaKeys.add(GrappaConstants.MINBOX_ATTR);
      grappaKeys.add(GrappaConstants.MINLEN_ATTR);
      grappaKeys.add(GrappaConstants.MINSIZE_ATTR);
      grappaKeys.add(GrappaConstants.NODESEP_ATTR);
      grappaKeys.add(GrappaConstants.ORIENTATION_ATTR);
      grappaKeys.add(GrappaConstants.PERIPHERIES_ATTR);
      grappaKeys.add(GrappaConstants.POS_ATTR);
      grappaKeys.add(GrappaConstants.PRINTLIST_ATTR);
      grappaKeys.add(GrappaConstants.RANKDIR_ATTR);
      grappaKeys.add(GrappaConstants.RANKSEP_ATTR);
      grappaKeys.add(GrappaConstants.RECTS_ATTR);
      grappaKeys.add(GrappaConstants.ROTATION_ATTR);
      grappaKeys.add(GrappaConstants.SHAPE_ATTR);
      grappaKeys.add(GrappaConstants.SIDES_ATTR);
      grappaKeys.add(GrappaConstants.SIZE_ATTR);
      grappaKeys.add(GrappaConstants.SKEW_ATTR);
      grappaKeys.add(GrappaConstants.STYLE_ATTR);
      grappaKeys.add(GrappaConstants.TAG_ATTR);
      grappaKeys.add(GrappaConstants.TIP_ATTR);
      grappaKeys.add(GrappaConstants.WEIGHT_ATTR);
      grappaKeys.add(GrappaConstants.WIDTH_ATTR);
      grappaKeys.add("colorDeterminer");
      grappaKeys.add("invisible");
    }
    names = new Vector();
    values = new Vector();
    Enumeration keys = node.getLocalAttributeKeys();
    Vector orderedKeys = new Vector();
    while (keys.hasMoreElements()) {
      orderedKeys.add(keys.nextElement());
    }
    Collections.sort(orderedKeys, new KeyComparator());
    for (int i = 0; i < orderedKeys.size(); i++) {
      String key = (String)orderedKeys.elementAt(i);
      if (!grappaKeys.contains(key)) {
        values.addElement(node.getLocalAttribute(key).getValue());
        int j = key.indexOf('-');
        if (j != -1)
          key = key.substring(j+1);
        names.addElement(key);
      }
    }
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
    if (col == 0) {
      return names.elementAt(row);
    } else if (col == 1)
      return values.elementAt(row);
    return "";
  }

  private void addAttribute(String name) {
    Attribute a = node.getLocalAttribute(name);
    if (a == null) {
      return;
    }
    names.addElement(name);
    values.addElement(a.getValue());
  }

  class KeyComparator implements Comparator {

// do alphabetic ordering    
//     public int compare(Object o1, Object o2) {
//       String s1 = (String)o1;
//       String s2 = (String)o2;
//       return s1.compareTo(s2);
//     }

    // do numerical prefix ordering
    public int compare(Object o1, Object o2) {
      String s1 = (String)o1;
      String s2 = (String)o2;
      int index1 = s1.indexOf('-');
      if (index1 == -1)
        return -1;
      int index2 = s2.indexOf('-');
      if (index2 == -1)
        return 1;
      s1 = s1.substring(0, index1);
      s2 = s2.substring(0, index2);
      try {
        int first = Integer.parseInt(s1);
        int second = Integer.parseInt(s2);
        if (first < second)
          return -1;
        else
          return 1;
      } catch (Exception e) {
        return 0;
      }
    }

    public boolean equals(Object o) {
      return this.equals(o);
    }
  }

  private void readObject(ObjectInputStream ois)
    throws IOException, ClassNotFoundException
  {
    ois.defaultReadObject();
    createLogger();
  }
    
}
