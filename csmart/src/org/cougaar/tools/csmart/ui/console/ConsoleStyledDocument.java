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

package org.cougaar.tools.csmart.ui.console;

import org.cougaar.tools.csmart.ui.viewer.CSMART;
import org.cougaar.util.log.Logger;

import javax.swing.event.DocumentListener;
import javax.swing.event.UndoableEditListener;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import java.io.BufferedWriter;
import java.io.FileWriter;

/**
 * This is the document that contains the content of the Node output.
 * By extending <code>DefaultStyledDocument</code>, we get access to
 * search and highlight support. Note however that this is relatively
 * heavyweight. It includes support for undo listeners, all sorts of
 * SGML type strutures, etc. Also note that it uses a <code>GapContent</code>
 * for underlying storage - which automatically grows at twice the required
 * speed, and never shrinks. So this extension goes to some pains to
 * remove text from the document before adding additional content,
 * to avoid growing the document uncontrollably.
 */
public class ConsoleStyledDocument extends DefaultStyledDocument {
  // DefaultStyledDocument buffer size is 4096
  int bufferSize;
  int minRemoveSize; // remove characters in this increment size

  private transient Logger log;

  public ConsoleStyledDocument() {
    bufferSize = DefaultStyledDocument.BUFFER_SIZE_DEFAULT * 4;
    minRemoveSize = (int)(bufferSize * .2);
    createLogger();
    // We dont need undo support
    UndoableEditListener[] uelists = getUndoableEditListeners();
    for (int i = 0; i < uelists.length; i++)
      removeUndoableEditListener(uelists[i]);
  }

  private void createLogger() {
    log = CSMART.createLogger(this.getClass().getName());
  }

  /**
   * Append a string to the display buffer.
   * If we are limiting the buffer size, trim the buffer as necessary
   * to keep under the limit, possibly including some of the addition
   * being added here. This is in part necessary because the underlying
   * storage, a <code>GapContent</code>, never shrinks in size --
   * and when it increases in size, it does so by double the necessary
   * amount (to maintain a gap).<br>
   * Note that this means that notifications / etc may miss things
   * if we were unable to add the entire new String.
   * @param s string to append to the styled document
   * @param a attribute set to use in the styled document
   */
  public void appendString(String s, AttributeSet a) {
    if (s == null || s.length() == 0)
      return;
    try {
      // If the buffer size is not -1, we must trim it
      if (bufferSize > 0) {
        int slen = s.length();
        int blen = getLength();
        int nremove = blen + slen - bufferSize; // amt to remove from doc
        int nskip = 0; // amount in new string to skip
        if (nremove > blen) {
          nskip = nremove - blen;
          nremove = blen;
        } else if (nremove > 0) {
	  // remove at least the min chunk
          nremove = Math.max(nremove, minRemoveSize);
        } else {
          nremove = 0;
        }
	// OK, remove from the document
        if (nremove > 0) {
          remove(0, nremove);
        }
	// and trim from the input string so it will fit
        if (nskip > 0) {
          s = s.substring(nskip);    // And keep only what fits
        }
      }
      // Now insert the new amount into the buffer
      super.insertString(getLength(), s, a);
    } catch (BadLocationException ble) {
      if(log.isErrorEnabled()) {
        log.error("appendString: Bad location exception: " + ble +
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

  /**
   * When completely done with this document, get rid of all listeners,
   * and set the internal buffer variable to null
   **/
  public void cleanUp() {
//     if (log.isDebugEnabled())
//       log.debug("ConsoleDoc.cleanUp about to remove listeners");
    // remove listeners
    DocumentListener[] lists = getDocumentListeners();
    for (int i = 0; i < lists.length; i++)
      removeDocumentListener(lists[i]);
    UndoableEditListener[] uelists = getUndoableEditListeners();
    for (int i = 0; i < uelists.length; i++)
      removeUndoableEditListener(uelists[i]);

    // clear buffer (underlying storage)
    buffer = null;
  }

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

} // ConsoleStyledDocument
