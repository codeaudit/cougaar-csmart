/*
 * <copyright>
 * This software is to be used only in accordance with the COUGAAR license
 * agreement. The license agreement and other information can be found at
 * http://www.cougaar.org
 *
 * © Copyright 2000, 2001 BBNT Solutions LLC
 * </copyright>
 */

package org.cougaar.tools.csmart.ui.console;

import java.io.*;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyledDocument;
import javax.swing.SwingUtilities;

public class DocumentWriter extends Writer {
  private StyledDocument text;
  private SimpleAttributeSet att;

  DocumentWriter(StyledDocument text, SimpleAttributeSet att) {
    this.text = text;
    this.att = att;
  }

  private void ensureOpen() throws IOException {
    if (text == null) {
      throw new IOException("Writer closed");
    }
  }

  public synchronized void write(char[] buf, int off, int len) throws IOException {
    ensureOpen();
    final String insertion = new String(buf, off, len);
    try {
      // must use swing "invokeLater" to be thread-safe
      SwingUtilities.invokeLater(new Runnable() {
	public void run() {
	  try {
	    text.insertString(text.getLength(), insertion, att);
	  } catch (Exception e) {
	  }
	}
      });
    } catch (RuntimeException e) {
      throw new IOException(e.getMessage());
    }
  }

  public synchronized void flush() throws IOException {
    ensureOpen();
  }

  public synchronized void close() throws IOException {
    ensureOpen();
    text = null;
  }
}
