/*
 * <copyright>
 *  Copyright 2000-2002 BBNT Solutions, LLC
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
import org.cougaar.tools.csmart.ui.viewer.CSMART;
import org.cougaar.util.log.Logger;

public class ConsoleStyledDocument extends DefaultStyledDocument {
  // DefaultStyledDocument buffer size is 4096
  int bufferSize;
  int minRemoveSize;

  private transient Logger log;

  public ConsoleStyledDocument() {
    bufferSize = DefaultStyledDocument.BUFFER_SIZE_DEFAULT * 4;
    minRemoveSize = (int)(bufferSize * .2);
    createLogger();
  }

  private void createLogger() {
    log = CSMART.createLogger(this.getClass().getName());
  }

  /**
   * Fill document from log file.  Reads log file contents into buffer.
   * Caller must close log file prior to calling this method
   * and re-open log file for appending.
   * Sets screen buffer size to display all output (i.e. no limit).
   * @param logFileName name of the log file from which to fill document
   */

  public void fillFromLogFile(String logFileName) {
    AttributeSet a = new javax.swing.text.SimpleAttributeSet();
    try {
      // clear document
      remove(0, getLength());
    } catch (Exception e) {
      if(log.isErrorEnabled()) {
        log.error("Exception clearing output window", e);
      }
    }
    // read log file contents into document
    BufferedReader reader = null;
    try {
      reader = new BufferedReader(new FileReader(logFileName));
    } catch (FileNotFoundException fnfe) {
      if(log.isErrorEnabled()) {
        log.error("Exception, Cannot find logfile", fnfe);
      }
    }
    try {
      while (true) {
        String s = reader.readLine();
        if (s != null) {
          insertString(getLength(), s, a);
        } else
          break;
      }
    } catch (IOException ioe) {
      if(log.isErrorEnabled()) {
        log.error("Exception", ioe);
      }
    } catch (BadLocationException ble) {
      if(log.isErrorEnabled()) {
        log.error("Exception", ble);
      }
    }
    bufferSize = -1; // don't trim document any more
  }

  /**
   * Append a string to the display buffer.  This appends the entire
   * string, and then trims the buffer to the size specified
   * in the setBufferSize method if necessary.  The entire string is
   * appended so that listeners can "see" the new string and act on
   * it (for example, highlight it).
   * @param s string to insert in the styled document
   * @param a attribute set to use in the styled document
   */

  public void appendString(String s, AttributeSet a) {
    if (s == null || s.length() == 0)
      return;
    try {
      super.insertString(getLength(), s, a);
      if (bufferSize == -1) // display everything, no limit on buffer size
        return;
      int len = s.length();
      // special case, the string is larger than the buffer
      // just insert the end of the string
      if (len >= bufferSize) {
        int tmp = len-bufferSize;
        remove(0, getLength() - bufferSize);
        return;
      }
      // if appending string exceeded buffer length
      // then remove at least the first 20% of the buffer
      if (getLength() > bufferSize) {
        int tmp = Math.max(getLength() - bufferSize, minRemoveSize);
        // don't remove more characters than exist
        tmp = Math.min(tmp, getLength());
        remove(0, tmp);
      }
    } catch (BadLocationException ble) {
      if(log.isErrorEnabled()) {
        log.error("Bad location exception: " + ble +
                           " " + ble.offsetRequested(), ble);
      }
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

  // for random access file
//    public void fillFromLogFile(RandomAccessFile logFile) {
//      AttributeSet a = new javax.swing.text.SimpleAttributeSet();
//      try {
//        // clear document
//        remove(0, getLength());
//      } catch (Exception e) {
//       if(log.isErrorEnabled()) {
//         log.error(e.toString());
//         e.printStackTrace();
//       }
//      }
//      // read log file contents into document
//      try {
//        RandomAccessFile logFile = new RandomAccessFile(logFileName, "r");
//        logFile.seek(0);
//        while (true) {
//          String s = logFile.readLine();
//          if (s != null) {
//            if(log.isDebugEnabled()) {
//              log.debug("Inserting: " + s + " at: " + getLength());
//            }
//            insertString(getLength(), s, a);
//          } else {
//            logFile.close();
//            break;
//          }
//        }
//      } catch (FileNotFoundException fnfe) {
//        if(log.isErrorEnabled()) {
//          log.error(fnfe.toString());
//        }
//      } catch (IOException ioe) {
//           if(log.isErrorEnabled()) {
//             log.error(ioe.toString());
//           }
//      } catch (BadLocationException ble) {
//        if(log.isErrorEnabled()) {
//           log.error(ble.toString());
//         }
//      }
//      bufferSize = -1; // don't trim document any more
//    }

  public static void main(String[] args) {
    ConsoleStyledDocument doc = new ConsoleStyledDocument();
    AttributeSet a = new javax.swing.text.SimpleAttributeSet();
    Logger log = CSMART.createLogger("org.cougaar.tools.csmart.ui.console.ConsoleStyledDocument");
    // with MAX_CHARACTER = 5 this prints:
    // abc, abcde, fghij, vwxyz
    doc.setBufferSize(5);
    try {
      BufferedWriter logFile = new BufferedWriter(new FileWriter("tmp"));
      logFile.write("abc");
      doc.appendString("abc", a);
      if(log.isDebugEnabled()) {
        log.debug(doc.getText(0, doc.getLength()));
      }
      logFile.write("de");
      doc.appendString("de", a);
      if(log.isDebugEnabled()) {
        log.debug(doc.getText(0, doc.getLength()));
      }
      logFile.write("fghij");
      doc.appendString("fghij", a);
      if(log.isDebugEnabled()) {
        log.debug(doc.getText(0, doc.getLength()));
      }
      logFile.write("klmnopqrstuvwxyz");
      doc.appendString("klmnopqrstuvwxyz", a);
      if(log.isDebugEnabled()) {
        log.debug(doc.getText(0, doc.getLength()));
      }
      logFile.close();
      doc.fillFromLogFile("tmp");
      if(log.isDebugEnabled()) {
        log.debug(doc.getText(0, doc.getLength()));
      }
      logFile = new BufferedWriter(new FileWriter("tmp", true));
      logFile.write("12345");
      doc.appendString("12345", a);
      if(log.isDebugEnabled()) {
        log.debug("Final document");
        log.debug(doc.getText(0, doc.getLength()));
      }
      logFile.close();
    } catch (Exception e) {
      if(log.isErrorEnabled()) {
        log.error("Exception", e);
      }
    }
  }

}
