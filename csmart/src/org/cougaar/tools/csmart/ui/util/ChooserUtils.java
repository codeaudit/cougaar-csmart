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
