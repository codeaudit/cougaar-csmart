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

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.SimpleAttributeSet;

public class ConsoleStyledDocument extends DefaultStyledDocument {
  static int MAX_CHARACTERS = 1000;

  public void appendString(String s, AttributeSet a) {
    try {
      int len = s.length();
      // special case, the string is larger than the document
      // just insert the end of the string
      if (len >= MAX_CHARACTERS) {
        remove(0, MAX_CHARACTERS);
        super.insertString(0, s.substring(len - MAX_CHARACTERS), a);
        return;
      }
      int neededSpace = getLength() + len;
      if (neededSpace > MAX_CHARACTERS) 
        remove(0, neededSpace - MAX_CHARACTERS);
      super.insertString(getLength(), s, a);
    } catch (BadLocationException ble) {
      System.out.println("Bad location exception: " + ble);
    }
  }

  public static void main(String[] args) {
    ConsoleStyledDocument doc = new ConsoleStyledDocument();
    AttributeSet a = new SimpleAttributeSet();
    // with MAX_CHARACTER = 5 this prints:
    // abc, abcde, fghij, vwxyz
    try {
      doc.appendString("abc", a);
      System.out.println(doc.getText(0, doc.getLength()));
      doc.appendString("de", a);
      System.out.println(doc.getText(0, doc.getLength()));
      doc.appendString("fghij", a);
      System.out.println(doc.getText(0, doc.getLength()));
      doc.appendString("klmnopqrstuvwxyz", a);
      System.out.println(doc.getText(0, doc.getLength()));
    } catch (BadLocationException ble) {
      System.out.println("Bad location exception: " + ble);
    }
  }
}
