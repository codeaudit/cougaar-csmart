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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FileNotFoundException;
import java.io.RandomAccessFile;
import java.io.IOException;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;

public class ConsoleStyledDocument extends DefaultStyledDocument {
  // DefaultStyledDocument buffer size is 4096
  //  static int MAX_CHARACTERS = DefaultStyledDocument.BUFFER_SIZE_DEFAULT;
  //  static int MIN_REMOVE_CHARACTERS = 800;
  int bufferSize;
  int minRemoveSize;

  public ConsoleStyledDocument() {
    bufferSize = DefaultStyledDocument.BUFFER_SIZE_DEFAULT * 4;
    minRemoveSize = (int)(bufferSize * .2);
  }

  // works with non-random access file
//    public void fillFromLogFile(String logFileName) {
//      AttributeSet a = new javax.swing.text.SimpleAttributeSet();
//      try {
//        // clear document
//        remove(0, getLength());
//      } catch (Exception e) {
//        System.out.println(e);
//        e.printStackTrace();
//      }
//      // read log file contents into document
//      BufferedReader reader = null;
//      try {
//        reader = new BufferedReader(new FileReader(logFileName));
//      } catch (FileNotFoundException fnfe) {
//        System.out.println(fnfe);
//      }
//      try {
//        while (true) {
//          String s = reader.readLine();
//          if (s != null) {
//            System.out.println("Inserting: " + s + " at: " + getLength());
//            insertString(getLength(), s, a);
//          } else
//            break;
//        }
//      } catch (IOException ioe) {
//        System.out.println(ioe);
//      } catch (BadLocationException ble) {
//        System.out.println(ble);
//      }
//      bufferSize = -1; // don't trim document any more
//    }

  // for random access file
  public void fillFromLogFile(RandomAccessFile logFile) {
    AttributeSet a = new javax.swing.text.SimpleAttributeSet();
    try {
      // clear document
      remove(0, getLength());
    } catch (Exception e) {
      System.out.println(e);
      e.printStackTrace();
    }
    // read log file contents into document
    try {
      logFile.seek(0);
      while (true) {
        String s = logFile.readLine();
        if (s != null) {
          System.out.println("Inserting: " + s + " at: " + getLength());
          insertString(getLength(), s, a);
        } else
          break;
      }
    } catch (IOException ioe) {
      System.out.println(ioe);
    } catch (BadLocationException ble) {
      System.out.println(ble);
    }
    bufferSize = -1; // don't trim document any more
  }

  public void appendString(String s, AttributeSet a) {
    try {
      if (bufferSize == -1) { // display everything, no limit on buffer size
        super.insertString(getLength(), s, a);
        return;
      }
      int len = s.length();
      // special case, the string is larger than the buffer
      // just insert the end of the string
      if (len >= bufferSize) {
        remove(0, getLength());
        super.insertString(0, s.substring(len - bufferSize), a);
        return;
      }
      int bufferLength = getLength();
      int neededSpace = bufferLength + len;
      // if appending string will exceed buffer length
      // then remove at least the first 20% of buffer
      if (neededSpace > bufferSize) {
        int tmp = Math.max(neededSpace - bufferSize, minRemoveSize);
        // don't remove more characters than exist
        tmp = Math.min(tmp, bufferLength);
        remove(0, tmp);
      }
      super.insertString(getLength(), s, a);
    } catch (BadLocationException ble) {
      System.out.println("Bad location exception: " + ble +
                         " " + ble.offsetRequested());
      ble.printStackTrace();
    }
  }

  /**
   * Set the number of characters displayed.  A value of -1 means
   * display all characters (no limit).  Values of 0 and other negative
   * numbers are ignored.
   */

  public void setBufferSize(int bufferSize) {
    if (bufferSize == -1) 
      this.bufferSize = bufferSize;
    else if (bufferSize >= 1) {
      this.bufferSize = bufferSize;
      this.minRemoveSize = Math.min((int)(bufferSize * .2), 1);
    }
  }

  /**
   * Get the number of characters displayed.  A value of -1 means
   * display all characters (no limit). 
   */

  public int getBufferSize() {
    return bufferSize;
  }

  public static void main(String[] args) {
    ConsoleStyledDocument doc = new ConsoleStyledDocument();
    AttributeSet a = new javax.swing.text.SimpleAttributeSet();
    // with MAX_CHARACTER = 5 this prints:
    // abc, abcde, fghij, vwxyz
    doc.setBufferSize(5);
    try {
      //      BufferedWriter logFile = new BufferedWriter(new FileWriter("tmp"));
      RandomAccessFile logFile = 
        new RandomAccessFile("tmp-ra", "rw");
      logFile.writeChars("abc");
      doc.appendString("abc", a);
      System.out.println(doc.getText(0, doc.getLength()));
      logFile.writeChars("de");
      doc.appendString("de", a);
      System.out.println(doc.getText(0, doc.getLength()));
      logFile.writeChars("fghij");
      doc.appendString("fghij", a);
      System.out.println(doc.getText(0, doc.getLength()));
      logFile.writeChars("klmnopqrstuvwxyz");
      doc.appendString("klmnopqrstuvwxyz", a);
      System.out.println(doc.getText(0, doc.getLength()));
      // for non-random access file
      //      logFile.close();
      //      doc.fillFromLogFile("tmp");
      //      System.out.println(doc.getText(0, doc.getLength()));
      //      logFile = new BufferedWriter(new FileWriter("tmp", true));
      //      logFile.write("12345");
      //      doc.appendString("12345", a);

      doc.fillFromLogFile(logFile);
      System.out.println(doc.getText(0, doc.getLength()));
      System.out.println("Final document");
      logFile.seek(logFile.length());
      logFile.writeChars("12345");
      doc.appendString("12345", a);
      System.out.println(doc.getText(0, doc.getLength()));
      logFile.close();
    } catch (Exception e) {
      System.out.println(e);
      e.printStackTrace();
    }
  }
}
