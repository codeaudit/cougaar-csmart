/* 
 * <copyright>
 *  Copyright 2001-2003 BBNT Solutions, LLC
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

package org.cougaar.tools.csmart.core.cdata;

/**
 * Leaf Component Interface.
 *
 * This is used to contain auxiliary component data for a 
 * specific component.  This with mostly be files that
 * are needed by a component.
 */
public interface LeafComponentData {

  /** Leaf Component Types **/
  String FILE = "File";

  /** 
   * Gets the type of the leaf component.
   * Convenience types are defined in the interface.
   * 
   * @return Component type
   **/
  String getType();

  /**
   * Sets the type of the leaf component.
   *
   * @param String component type.
   */
  void setType(String type);

  /** 
   * Name of Component.  For example,
   * if the leaf is a file, the name
   * is the name of the file.
   *
   * @return name of the component.
   **/
  String getName();
  
  /**
   * Sets the name of the component.
   *
   * @param String name of the component.
   */
  void setName(String name);

  /** 
   * Value of Component.  For a file,
   * this is the file contents.
   *
   * @return component value
   **/
  Object getValue();

  /**
   * Sets the value of the component.
   *
   * @param Object value of the component
   */
  void setValue(Object val);
}
