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

public class CompoundWriter extends Writer {
  private Writer w1,w2;

  public CompoundWriter(Writer w1, Writer w2) {
    this.w1 = w1;
    this.w2 = w2;
  }

  public void write(char[] buf, int off, int len) throws IOException {
    w1.write(buf,off,len);
    w1.flush();
    w2.write(buf,off,len);
    w2.flush();
  }

  public void flush() throws IOException {
    w1.flush();
    w2.flush();
  }

  public synchronized void close() throws IOException {
    w1.close();
    w2.close();
  }
}

