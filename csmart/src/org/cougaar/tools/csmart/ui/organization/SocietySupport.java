package org.cougaar.tools.csmart.ui.organization;

import org.w3c.dom.Document;

import javax.swing.*;
import java.io.File;

/**
 * Created by IntelliJ IDEA.
 * User: travers
 * Date: Apr 8, 2003
 * Time: 1:46:06 PM
 * To change this template use Options | File Templates.
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
