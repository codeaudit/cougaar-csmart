package org.cougaar.tools.csmart.ui.console;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Highlighter;
import javax.swing.text.SimpleAttributeSet;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestResult;
import junit.framework.TestSuite;

public class Test_ConsoleTextPane extends TestCase {
  ConsoleTextPane pane;
  ConsoleStyledDocument doc;
  AttributeSet attributeSet;

  public Test_ConsoleTextPane(String name) {
    super(name);
  }

  protected void setUp() {
    doc = new ConsoleStyledDocument();
    attributeSet = new SimpleAttributeSet();
    pane = new ConsoleTextPane(doc, 
                               new NodeStatusButton((javax.swing.Icon)null));
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
    assertTrue("Incorrect number of highlights",
               highlights.length == 1);
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
    return new TestSuite(Test_ConsoleTextPane.class);
  }

  public static void main(String[] args) {
    junit.textui.TestRunner.run(suite());
  }
    //    Test test = new Test_ConsoleTextPane("Test ConsoleTextPane") {
    //      public void runTest() {
    //        testNotifyHighlight();
    //      }
    //    };
  //    Test test = new Test_ConsoleTextPane("testNotifyHighlight");
  //    test.run(new TestResult());
  //  }
}
