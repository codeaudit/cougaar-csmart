package org.cougaar.tools.csmart.ui.organization;

import org.w3c.dom.Document;

import javax.swing.*;
import java.io.File;

/**
 * Interface to be implemented to support societies from
 * different sources (i.e. different file types).
 */
public interface SocietySupport {
  public JTree readFile(String filename);
  public String getSocietyName();
  public int getType();
  public String getFileExtension();
  public String getFileTitle();
  public int updateAgentCount();
  public Document getDocument();
  public boolean saveFile(File file);
}
