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

