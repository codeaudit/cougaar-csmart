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
   * @param type (String) of component
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
   * @param name (String) of the component.
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
   * @param val value of the component
   */
  void setValue(Object val);
}
