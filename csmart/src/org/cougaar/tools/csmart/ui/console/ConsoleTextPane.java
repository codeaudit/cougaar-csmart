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
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Document;
import javax.swing.text.Highlighter;
import javax.swing.text.Position;

/**
 * A text pane that contains a ConsoleStyledDocument and supports
 * searching and highlighting that document.
 */

public class ConsoleTextPane extends JTextPane {
  ConsoleStyledDocument doc;
  String searchString;
  Position searchPosition;
  String notifyCondition;
  Position notifyPosition;
  Highlighter highlighter;
  DefaultHighlighter.DefaultHighlightPainter searchHighlight;
  DefaultHighlighter.DefaultHighlightPainter notifyHighlight;
  Object searchHighlightReference;
  Object notifyHighlightReference;
  NodeStatusButton statusButton;

  public ConsoleTextPane(ConsoleStyledDocument doc, 
                         NodeStatusButton statusButton) {
    super(doc);
    this.doc = doc;
    this.statusButton = statusButton;
    highlighter = getHighlighter();
    searchHighlight = 
      new DefaultHighlighter.DefaultHighlightPainter(Color.yellow);
    notifyHighlight =
      new DefaultHighlighter.DefaultHighlightPainter(Color.magenta);
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
      displayHighlightedText(startOffset, endOffset);
    } catch (BadLocationException ble) {
      System.out.println("Bad location exception: " + ble.offsetRequested() +
                         " " + ble);
      return false;
    }
    return true;
  }

  /**
   * Specify a "notify" string. If this string
   * is detected in the output of the node, then the node status
   * button color is set to blue and remains set until the user resets it;
   * the first instance of the "notify" string is highlighted.
   * Search is case insensitive.
   * @param s string to watch for in node output
   */

  public void setNotifyCondition(String s) {
    if (s == null)
      notifyCondition = s;
    else {
      notifyCondition = s.toLowerCase();
      doc.addDocumentListener(new MyDocumentListener());
    }
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

  /**
   * Search for and highlight the next instance of the notify string,
   * starting at the end of the last notify string found (or at the
   * beginning of the screen buffer, if the previous notify string 
   * was removed from the buffer).
   * Search is case insensitive.
   * @return true if notify string is found and false otherwise
   */

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
    return worker(s, doc.getStartPosition(), true);
  }

  /**
   * Search for the "current" string (i.e. the last string specified in
   * a call to the search method) starting at the end of the last 
   * search string found (or at the beginning of the screen buffer, 
   * if the previous search string was removed from the buffer).
   * Search is case insensitive.
   * @return true if string found and false otherwise
   */

  public boolean searchNext() {
    if (searchString != null) 
      return worker(searchString, searchPosition, true);
    return false;
  }

  public static void main(String[] args) {
    ConsoleStyledDocument doc = new ConsoleStyledDocument();
    javax.swing.text.AttributeSet a = 
      new javax.swing.text.SimpleAttributeSet();
    ConsoleTextPane pane = 
      new ConsoleTextPane(doc, new NodeStatusButton((javax.swing.Icon)null));
    doc.setBufferSize(20);
    doc.appendString("abcdefghijklmnopqrstuvw", a);
    pane.setNotifyCondition("now is the time for all good men");
    doc.appendString("now is the time for all good men", a);
    javax.swing.JFrame frame = new javax.swing.JFrame();
    frame.getContentPane().add(pane);
    frame.pack();
    frame.setSize(200, 200);
    frame.setVisible(true);
  }

  /**
   * Listen for text added to the displayed document, and highlight
   * any text matching the notify condition.
   */

  class MyDocumentListener implements DocumentListener {

    public void insertUpdate(DocumentEvent e) {
      // if there's already a highlighted notify condition, 
      // then don't highlight a new one
      if (notifyPosition != null)
        return;
      Document doc = e.getDocument();
      try {
        String newContent = doc.getText(e.getOffset(), e.getLength());
        newContent = newContent.toLowerCase();
        int index = newContent.indexOf(notifyCondition);
        if (index != -1) {
          int startOffset = doc.getEndPosition().getOffset() -
            newContent.length() + index - 1;
          int endOffset = startOffset + notifyCondition.length();
          highlightNotifyString(startOffset, endOffset);
          notifyPosition = doc.createPosition(endOffset);
          statusButton.setStatus(NodeStatusButton.STATUS_NOTIFY);
        }
      } catch (BadLocationException ble) {
        System.out.println(ble);
        ble.printStackTrace();
      }
    }

    public void changedUpdate(DocumentEvent e) {
    }

    public void removeUpdate(DocumentEvent e) {
    }

  }
}
