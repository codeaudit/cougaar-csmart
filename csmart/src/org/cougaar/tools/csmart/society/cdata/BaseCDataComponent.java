/**
 * <copyright>
 *  
 *  Copyright 2002-2004 BBNT Solutions, LLC
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
package org.cougaar.tools.csmart.society.cdata;

import org.cougaar.tools.csmart.core.cdata.ComponentData;
import org.cougaar.tools.csmart.core.property.BaseComponent;
import org.cougaar.tools.csmart.society.ComponentBase;


/**
 * A BaseComponent initialized from ComponentData. Used when copying Complex Recipes.
 */
public class BaseCDataComponent extends ComponentBase implements BaseComponent {
  
  private ComponentData cdata;

  public BaseCDataComponent(ComponentData cdata) {
    super(cdata.getName());
    this.cdata = cdata;
    this.classname = cdata.getClassName();
    this.priority = cdata.getPriority();
    this.type = cdata.getType();
  }

  /**
   * Initialize the Properties using the super class. Then copy over the
   * other slots: LIB_ID, ALIB_ID, and the Parameters.
   */
  public void initProperties() {
    super.initProperties();
    setLibID(cdata.getLibID());
    setAlibID(cdata.getAlibID());
    // for each parameter of the component, call addParameter
    for (int i = 0; i < cdata.parameterCount(); i++)
      addParameter(cdata.getParameter(i));
  }

}// BaseCDataComponent
