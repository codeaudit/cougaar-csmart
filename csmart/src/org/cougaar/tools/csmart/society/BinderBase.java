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
package org.cougaar.tools.csmart.society;

import org.cougaar.core.component.ComponentDescription;
import org.cougaar.tools.csmart.core.cdata.ComponentData;
import org.cougaar.tools.csmart.ui.viewer.CSMART;

/**
 * BinderBase is the basic implementation for editing & configuring
 * binders. It is particularly suited to Binders of Plugins (that sit
 * inside Agents). However, it has a slot to allow specicying the type.
 */
public class BinderBase
  extends ComponentBase
  implements BinderComponent {

  /** Binder Classname Property Definition **/
  public static final String PROP_CLASSNAME = "Binder Class Name";
  public static final String PROP_CLASSNAME_DESC = "Name of the Binder Class";

  protected String folderLabel = "Binders";

  public static final String PROP_TYPE = "Binder Type";
  public static final String PROP_TYPE_DESC = "Type of Binder (Agent, Node)";
  public static final String PROP_PARAM = "param-";

  public BinderBase(String name) {
    super(name, "", ComponentDescription.priorityToString(ComponentDescription.PRIORITY_BINDER), ComponentData.AGENTBINDER);
    createLogger();
  }

  public BinderBase(String name, String classname, String priority) {
    super(name, classname, priority, ComponentData.AGENTBINDER);
    if(priority == null || priority.trim().equals("")) {
      this.priority = 
        ComponentDescription.priorityToString(ComponentDescription.PRIORITY_BINDER);
    }
    createLogger();
  }

  public BinderBase(String name, String classname, String priority, String type) {
    super(name, classname, priority, type);
    if(priority == null || priority.trim().equals("")) {
      this.priority = 
        ComponentDescription.priorityToString(ComponentDescription.PRIORITY_BINDER);
    }
    if (type == null || type.trim().equals(""))
      this.type = ComponentData.AGENTBINDER;
    createLogger();
  }

  private void createLogger() {
    log = CSMART.createLogger(this.getClass().getName());
  }

} // End of BinderBase
