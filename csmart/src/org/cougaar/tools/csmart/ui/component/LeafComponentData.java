/* 
 * <copyright>
 * This software is to be used only in accordance with the COUGAAR license
 * agreement. The license agreement and other information can be found at
 * http://www.cougaar.org
 * 
 *       © Copyright 2001 by BBNT Solutions LLC.
 * </copyright>
 */

package org.cougaar.tools.csmart.ui.component;

/**
 * Leaf Component Interface.
 *
 * This is used to contain aux. compoent data for a 
 * specific component.  This with mostly be files that
 * are needed by a component.
 */
public interface LeafComponentData {

  /** Leaf Component Types **/
  public static final String FILE = "File";

  /** 
   * Gets the type of the leaf component.
   * Convenience types are defined in the interface.
   * 
   * @return Component type
   **/
  public String getType();

  /**
   * Sets the type of the leaf component.
   *
   * @param String component type.
   */
  public void setType(String type);

  /** 
   * Name of Component.  For example,
   * if the leaf is a file, the name
   * is the name of the file.
   *
   * @return name of the component.
   **/
  public String getName();
  
  /**
   * Sets the name of the component.
   *
   * @param String name of the component.
   */
  public void setName(String name);

  /** 
   * Value of Component.  For a file,
   * this is the file contents.
   *
   * @return component value
   **/
  public Object getValue();

  /**
   * Sets the value of the component.
   *
   * @param Object value of the component
   */
  public void setValue(Object val);
}
