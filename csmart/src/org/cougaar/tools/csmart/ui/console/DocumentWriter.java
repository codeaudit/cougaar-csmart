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
