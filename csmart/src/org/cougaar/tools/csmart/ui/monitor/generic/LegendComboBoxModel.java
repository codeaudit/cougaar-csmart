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

package org.cougaar.tools.csmart.ui.monitor.generic;

import javax.swing.*;
import java.awt.*;
import java.util.Collections;
import java.util.Comparator;
import java.util.Vector;

/**
 * Used as the model for JComboBoxes in legends.
 */

public class LegendComboBoxModel extends AbstractListModel implements ComboBoxModel {
    Object currentValue;
    Vector values; // vector of JLabel
    JLabel blankLabel;
    int nElements; // number of elements in the list

  /** Called with a vector which has alternating entries:
   * a String (for example, agent name)
   * a Color (java.awt.Color) for that String.
   * @param nodeColors strings and colors
   */

  public LegendComboBoxModel(Vector nodeColors) {
    nElements = nodeColors.size()/2;
    values = new Vector(nElements);
    for (int i = 0; i < nElements; i++) {
      JLabel label = new JLabel((String)nodeColors.elementAt(i*2),
				new ColoredSquare((Color)nodeColors.elementAt(i*2+1), 100, 12),
				SwingConstants.LEFT);
      label.setForeground(Color.black);
      values.addElement(label);
    }
    // sort labels alphabetically
    Collections.sort(values, new MyComparator());
    blankLabel = new JLabel();
    blankLabel.setText("");
    blankLabel.setIcon(null);
  }
  
  public void setSelectedItem(Object anObject) {
    currentValue = anObject;
    fireContentsChanged(this,-1,-1);
  }
      
  public Object getSelectedItem() {
    return currentValue;
  }

  public int getSize() {
    return nElements;
  }

  public Object getElementAt(int index) {
    if (index >= 0 && index < nElements)
      return values.elementAt(index);
    else 
      return blankLabel;
  }

  class MyComparator implements Comparator {

    public int compare(Object o1, Object o2) {
      String s1 = ((JLabel)o1).getText();
      return s1.compareTo(((JLabel)o2).getText());
    }

    public boolean equals(Object o) {
      return this.equals(o);
    }

  }
}

