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
import java.awt.Point;
import java.awt.Rectangle;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Highlighter;
import javax.swing.text.Position;

/**
 * A scrolled text pane that contains a ConsoleStyledDocument and supports
 * searching and highlighting that document.
 */

public class ConsoleTextPane extends JScrollPane {
  JTextPane textPane;
  ConsoleStyledDocument doc;
  Position startPosition;
  String searchString;
  Position searchPosition;
  Highlighter highlighter;
  DefaultHighlighter.DefaultHighlightPainter highlight;
  Object previousHighlight;

  public ConsoleTextPane(ConsoleStyledDocument doc) {
    this.doc = doc;
    textPane = new JTextPane(doc);
    setViewportView(textPane);
    startPosition = doc.getStartPosition();
    highlighter = textPane.getHighlighter();
    highlight = new DefaultHighlighter.DefaultHighlightPainter(Color.yellow);
    searchString = null;
  }

  /**
   * Search for the string starting at the beginning of the document, and
   * highlight it if found. 
   * Search is case insensitive.
   * @param s string to search for
   * @return true if string found and false otherwise
   */

  public boolean search(String s) {
    return searchWorker(s, startPosition);
  }

  /**
   * Search for a string starting at a given position.
   * Remember the end of the string as the start of the next search.
   */

  private boolean searchWorker(String s, Position startSearchPosition) {
    searchString = s.toLowerCase();
    int startOffset = startSearchPosition.getOffset();
    try {
      String content = 
        textPane.getText(startOffset, 
                         doc.getEndPosition().getOffset() - startOffset);
      content = content.toLowerCase();
      int index = content.indexOf(searchString);
      if (index == -1)
        return false;
      int end = index + searchString.length();
      if (previousHighlight != null)
        highlighter.removeHighlight(previousHighlight);
      // translate content offsets into document offsets and highlight
      previousHighlight =
        highlighter.addHighlight(startOffset+index, startOffset+end, highlight);
      searchPosition = doc.createPosition(startOffset+end);
      textPane.setCaretPosition(startOffset+end);
      Rectangle r = textPane.modelToView(startOffset);
      // scroll so that highlighted text is visible
      getViewport().setViewPosition(new Point((int)r.getX(), (int)r.getY()));
    } catch (BadLocationException ble) {
      System.out.println("Bad location exception: " + ble.offsetRequested() +
                         " " + ble);
      return false;
    }
    return true;
  }

  /**
   * Search for the "current" string (i.e. the last string specified in
   * a call to the search method) from the "current" location (i.e. the
   * end of the last string found).
   * Search is case insensitive.
   * @return true if string found and false otherwise
   */

  public boolean searchNext() {
    if (searchString != null)
      return searchWorker(searchString, searchPosition);
    return false;
  }

  public static void main(String[] args) {
    ConsoleStyledDocument doc = new ConsoleStyledDocument();
    javax.swing.text.AttributeSet a = 
      new javax.swing.text.SimpleAttributeSet();
    doc.appendString("abcdefghijklmnopqrstuvwxyzdef", a);
    ConsoleTextPane pane = new ConsoleTextPane(doc);
    javax.swing.JFrame frame = new javax.swing.JFrame();
    frame.getContentPane().add(pane);
    frame.pack();
    frame.setSize(200, 200);
    pane.search("def");
    try {
      ((JTextPane)pane.getViewport().getView()).getDocument().remove(0, 5);
    } catch (BadLocationException ble) {
      System.out.println("Bad location exception: " + ble.offsetRequested() +
                         " " + ble);
    }
    pane.searchNext();
    frame.setVisible(true);
  }
  
}
