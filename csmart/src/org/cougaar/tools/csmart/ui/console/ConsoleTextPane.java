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
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Highlighter;
import javax.swing.text.Position;

/**
 * A scrolled text pane that contains a ConsoleStyledDocument and supports
 * searching and highlighting that document.
 */

public class ConsoleTextPane extends JTextPane {
  ConsoleStyledDocument doc;
  Position startPosition;
  String searchString;
  Position searchPosition;
  String notifyCondition;
  Position notifyPosition;
  Highlighter highlighter;
  DefaultHighlighter.DefaultHighlightPainter searchHighlight;
  DefaultHighlighter.DefaultHighlightPainter notifyHighlight;
  Object searchHighlightReference;
  Object notifyHighlightReference;

  public ConsoleTextPane(ConsoleStyledDocument doc) {
    super(doc);
    this.doc = doc;
    startPosition = doc.getStartPosition();
    highlighter = getHighlighter();
    searchHighlight = 
      new DefaultHighlighter.DefaultHighlightPainter(Color.yellow);
    notifyHighlight =
      new DefaultHighlighter.DefaultHighlightPainter(Color.magenta);
    searchString = null;
  }

  private void highlightSearchString(int startOffset, int endOffset) {
    if (searchHighlightReference != null)
      highlighter.removeHighlight(searchHighlightReference);
    try {
      searchHighlightReference =
        highlighter.addHighlight(startOffset, endOffset, searchHighlight);
    } catch (BadLocationException ble) {
      System.out.println("Bad location exception: " + ble.offsetRequested() +
                         " " + ble);
    }
    displayHighlightedText(startOffset, endOffset);
  }

  private void highlightNotifyString(int startOffset, int endOffset) {
    if (notifyHighlightReference != null)
      highlighter.removeHighlight(notifyHighlightReference);
    try {
      notifyHighlightReference =
        highlighter.addHighlight(startOffset, endOffset, notifyHighlight);
    } catch (BadLocationException ble) {
      System.out.println("Bad location exception: " + ble.offsetRequested() +
                         " " + ble);
    }
    displayHighlightedText(startOffset, endOffset);
  }

  private void displayHighlightedText(int startOffset, int endOffset) {
    try {
      setCaretPosition(endOffset);
      Rectangle r = modelToView(startOffset);
      scrollRectToVisible(r);
    } catch (BadLocationException ble) {
      System.out.println("Bad location exception: " + ble.offsetRequested() +
                         " " + ble);
    }
  }

  /**
   * Search for a string starting at a given position.
   * If the string is found, set the position at the end of the
   * string so it can be used as the start of the next search.
   * If the string is not found, return false.
   * @param s string to search for
   * @param startPosition position in document to start search
   * @param search true for "search"; false for "notify"
   * @return boolean whether or not string was found
   * TODO: if the last text in the buffer is highlighted, then
   * the highlighting is automatically applied to any new text added,
   * how to avoid this?
   */

  private boolean worker(String s, Position startPosition, boolean search) {
    s = s.toLowerCase();
    int startOffset = startPosition.getOffset();
    try {
      String content = getText(startOffset, 
                               doc.getEndPosition().getOffset() - startOffset);
      content = content.toLowerCase();
      int index = content.indexOf(s);
      if (index == -1)
        return false;
      startOffset = startOffset + index;
      int endOffset = startOffset + s.length();
      if (search) {
        highlightSearchString(startOffset, endOffset);
        searchPosition = doc.createPosition(endOffset);
      } else {
        highlightNotifyString(startOffset, endOffset);
        notifyPosition = doc.createPosition(endOffset);
      }
    } catch (BadLocationException ble) {
      System.out.println("Bad location exception: " + ble.offsetRequested() +
                         " " + ble);
      return false;
    }
    return true;
  }

  /**
   * Search for notify strings in specified string.
   * If notify strings are not found, just append the string to the
   * styled document.  
   * If notify strings are found, but the caret is set (the user
   * searched the document or a previous notify string was found),
   * just append the string, otherwise, 
   * append the string, highlight the matching notify string, 
   * and set the caret at the end of the notify string.
   * @return boolean true if notify condition found, false otherwise
   */

  public boolean appendString(String s, AttributeSet a) {
    if (notifyCondition == null) {
      doc.appendString(s, a);
      return false;
    } else {
      int index = s.toLowerCase().indexOf(notifyCondition);
      if (index == -1) {
        doc.appendString(s, a);
        return false;
      }
      if (notifyPosition != null) {
        doc.appendString(s, a);
        return true;
      }
      int startOffset = doc.getEndPosition().getOffset() + index -1;
      int endOffset = startOffset + notifyCondition.length();
      doc.appendString(s, a); 
      highlightNotifyString(startOffset, endOffset);
      try {
        notifyPosition = doc.createPosition(endOffset);
      } catch (BadLocationException ble) {
        System.out.println("Bad location exception: " + ble.offsetRequested() +
                           " " + ble);
      }
      return true;
    }
  }

  public boolean notifyNext() {
    if (notifyCondition != null) 
      return worker(notifyCondition, notifyPosition, false);
    return false;
  }
        
  /**
   * Search for the string starting at the beginning of the document, and
   * highlight it if found. 
   * Search is case insensitive.
   * @param s string to search for
   * @return true if string found and false otherwise
   */

  public boolean search(String s) {
    searchString = s;
    return worker(s, startPosition, true);
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
      return worker(searchString, searchPosition, true);
    return false;
  }

  /**
   * Specify a string that this listener watches for; if the string
   * is detected in the standard output stream, then the node status
   * button color is set to blue and remains set until the user resets
   * it via the reset menu item on the status button pop-up menu.
   * @param s string to watch for in standard output
   */

  public void setNotifyCondition(String s) {
    if (s == null)
      notifyCondition = s;
    else
      notifyCondition = s.toLowerCase();
  }

  /**
   * Clear the notify highlighting and position.  Starts searching for
   * notify conditions with new text appended after this method is called.
   */

  public void clearNotify() {
    notifyPosition = null;
    if (notifyHighlightReference != null)
      highlighter.removeHighlight(notifyHighlightReference);
  }

  public static void main(String[] args) {
    ConsoleStyledDocument doc = new ConsoleStyledDocument();
    javax.swing.text.AttributeSet a = 
      new javax.swing.text.SimpleAttributeSet();
    ConsoleTextPane pane = new ConsoleTextPane(doc);
    pane.appendString("abcdefghijklmnopqrstuvw", a);
    pane.search("abc");
    pane.setNotifyCondition("xyz");
    pane.appendString("xyzabc", a);
    pane.appendString("xyz", a);
    pane.notifyNext();
    pane.searchNext();
    javax.swing.JFrame frame = new javax.swing.JFrame();
    frame.getContentPane().add(pane);
    frame.pack();
    frame.setSize(200, 200);
    frame.setVisible(true);
  }
  
}
