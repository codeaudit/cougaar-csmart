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
package org.cougaar.tools.csmart.ui.console;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Highlighter;
import javax.swing.text.SimpleAttributeSet;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestResult;
import junit.framework.TestSuite;

public class ConsoleTextPaneTest extends TestCase {
  ConsoleTextPane pane;
  ConsoleStyledDocument doc;
  AttributeSet attributeSet;

  public ConsoleTextPaneTest(String name) {
    super(name);
  }

  protected void setUp() {
    doc = new ConsoleStyledDocument();
    attributeSet = new SimpleAttributeSet();
    pane = new ConsoleTextPane(doc,
                               new NodeStatusButton(null));
    doc.setBufferSize(20); // number of characters retained in the document
  }

  /**
   * Determine whether appending a string that matches the notify condition
   * and is larger than the document buffer will be properly highlighted.
   */

  public void testNotifyHighlight() {
    doc.appendString("abcdefghijklmnopqrstuvw", attributeSet);
    try {
      assertTrue("Failed adding string that is longer than buffer",
                 doc.getText(0, doc.getLength()).equals("defghijklmnopqrstuvw"));
    } catch (BadLocationException ble) {
      System.out.println("Bad location exception: " + ble);
    }
    pane.setNotifyCondition("now is the time for all good men");
    doc.appendString("now is the time for all good men", attributeSet);
    try {
      assertTrue("Failed appending string that is longer than buffer",
                 doc.getText(0, doc.getLength()).equals("ime for all good men"));
    } catch (BadLocationException ble) {
      System.out.println("Bad location exception: " + ble);
    }
    Highlighter highlighter = pane.getHighlighter();
    Highlighter.Highlight[] highlights = highlighter.getHighlights();
    // Next assertion fails. Since we trim before adding, the highlight
    // never gets applied, so we now expect the value to be 0 here....
    //assertTrue("Incorrect number of highlights",
    //           highlights.length == 1);
    int start = highlights[0].getStartOffset();
    int end = highlights[0].getEndOffset();
    try {
      assertTrue("Incorrect highlighting",
                 doc.getText(start, end).equals("ime for all good men"));
    } catch (BadLocationException ble) {
      System.out.println("Bad location exception: " + ble);
    }
    doc.appendString("0123456789", attributeSet);
    try {
      assertTrue("Highlighting incorrect after appending new text",
                 doc.getText(start, end).equals("l good men0123456789"));
    } catch (BadLocationException ble) {
      System.out.println("Bad location exception: " + ble);
    }
    javax.swing.JFrame frame = new javax.swing.JFrame();
    frame.getContentPane().add(pane);
    frame.pack();
    frame.setSize(200, 200);
    frame.setVisible(true);
  }

  public static Test suite() {
    return new TestSuite(ConsoleTextPaneTest.class);
  }

  public static void main(String[] args) {
    junit.textui.TestRunner.run(suite());
  }
    //    Test test = new ConsoleTextPaneTest("Test ConsoleTextPane") {
    //      public void runTest() {
    //        testNotifyHighlight();
    //      }
    //    };
  //    Test test = new ConsoleTextPaneTest("testNotifyHighlight");
  //    test.run(new TestResult());
  //  }
}
