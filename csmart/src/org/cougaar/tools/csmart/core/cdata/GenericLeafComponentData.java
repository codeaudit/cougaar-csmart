/* 
 * <copyright>
 *  
 *  Copyright 2001-2004 BBNT Solutions, LLC
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

package org.cougaar.tools.csmart.core.cdata;

import java.io.Serializable;

/**
 * Generic implementation of LeafComponentData.
 *
 * @see LeafComponentData
 */
public class GenericLeafComponentData implements LeafComponentData, Serializable {

  private String type = null;
  private String name = null;
  private Object value = null;

  /**
   * Creates a new <code>GenericLeafComponentData</code> instance.
   *
   */
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
   * @param type of component 
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
   * @param name of the component.
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
   * @param val (Object) of the component
   */
  public void setValue(Object val) {
    this.value = val;
  }

  /**
   * Determines if two GenericLeafComponents are equal.
   *
   * @param o an GenericLeafComponent
   * @return a <code>boolean</code> value
   */
  public boolean equals(Object o) {
    if (o instanceof GenericLeafComponentData) {
      GenericLeafComponentData that = (GenericLeafComponentData)o;
      if (this.getName().equals(that.getName()) && 
          this.getType().equals(that.getType()) &&
          this.getValue().equals(that.getValue())) {
	return true;
      }
    }
    return false;
  }
}
