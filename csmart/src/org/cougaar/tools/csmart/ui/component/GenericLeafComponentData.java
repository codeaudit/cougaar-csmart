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
 * Generic Leaf Component Implementation.
 *
 * @see LeafComponentData
 */
public class GenericLeafComponentData {

  private String type = null;
  private String name = null;
  private Object value = null;

  public GenericLeafComponentData() {
  }

  /** 
   * Gets the type of the leaf component.
   * Convenience types are defined in the interface.
   * 
   * @return Component type
   **/
  public String getType() {
    return this.type;
  }

  /**
   * Sets the type of the leaf component.
   *
   * @param String component type.
   */
  public void setType(String type) {
    this.type = type;
  }

  /** 
   * Name of Component.  For example,
   * if the leaf is a file, the name
   * is the name of the file.
   *
   * @return name of the component.
   **/
  public String getName() {
    return this.name;
  }
  
  /**
   * Sets the name of the component.
   *
   * @param String name of the component.
   */
  public void setName(String name) {
    this.name = name;
  }

  /** 
   * Value of Component.  For a file,
   * this is the file contents.
   *
   * @return component value
   **/
  public Object getValue() {
    return this.value;
  }

  /**
   * Sets the value of the component.
   *
   * @param Object value of the component
   */
  public void setValue(Object val) {
    this.value = val;
  }
}
