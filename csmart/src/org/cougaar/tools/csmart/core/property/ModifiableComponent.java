/*
 * <copyright>
 *  
 *  Copyright 2000-2004 BBNT Solutions, LLC
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

package org.cougaar.tools.csmart.core.property;



/**
 * The interface for ConfigurableComponents that can be modified from
 * a society.
 **/
public interface ModifiableComponent extends BaseComponent {
  /**
   * Add a listener for non-property changes to the society. E.g.
   * adding a host.
   * @param l the ModificationListener
   **/
  void addModificationListener(ModificationListener l);

  /**
   * Remove a listener for non-property changes to the society.
   * @param l the ModificationListener
   **/
  void removeModificationListener(ModificationListener l);

  /**
   * Returns whether or not the society can be edited.
   * @return true if society can be edited and false otherwise
   */
  boolean isEditable();

  /**
   * Set whether or not the society can be edited.
   * @param editable true if society is editable and false otherwise
   */
  void setEditable(boolean editable);

  /**
   * Create a copy of the component
   * with the given name and return the copy.
   * @param name the name for the copied object
   * @return the copy
   */
  ModifiableComponent copy(String name);
}
