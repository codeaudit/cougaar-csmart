/*
 * <copyright>
 *  
 *  Copyright 2004 BBNT Solutions, LLC
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
package org.cougaar.tools.csmart.ui.util;

import javax.swing.filechooser.FileFilter;
import javax.swing.*;
import java.io.File;

/**
 * Created by IntelliJ IDEA.
 * User: bkrisler
 * Date: Apr 25, 2003
 * Time: 6:50:05 AM
 * To change this template use Options | File Templates.
 */
public class ChooserUtils {

  public FileFilter getFileFilter(String ext, String desc) {
    return new MyFileFilter(ext, desc);
  }

  public FileFilter getFileFilter(String[] exts, String desc) {
    return new MyFileFilter(exts, desc);
  }

  private class MyFileFilter extends FileFilter {
    String [] extensions;
    String description;

    public MyFileFilter(String ext, String desc) {
      this (new String[] {ext}, desc);
    }

    public MyFileFilter(String[] exts, String desc) {
      extensions = new String[exts.length];
      for(int i= exts.length -1; i >= 0; i--) {
        extensions[i] = exts[i].toLowerCase();
      }
      description = (desc == null ? exts[0] + " files" : desc);
    }

    public boolean accept(File f) {
      if(f.isDirectory()) { return true; }

      String name = f.getName().toLowerCase();
      for(int i = extensions.length-1; i >=0; i--) {
        if(name.endsWith(extensions[i])) {
          return true;
        }
      }
      return false;
    }

    public String getDescription() {
      return description;
    }
  }
}
