/**
 * <copyright>
 *  Copyright 2002 BBNT Solutions, LLC
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
 *  </copyright>
 */
package org.cougaar.tools.csmart.society;

import org.cougaar.tools.csmart.core.property.ModifiableConfigurableComponent;
import org.cougaar.tools.csmart.society.AgentComponent;
import org.cougaar.tools.csmart.ui.viewer.CSMART;

/**
 * AgentBase.java
 *
 * Implements generic methods required by all agents.
 *
 * @author
 * @version
 */

// TODO: does this have to implement PropertiesListener as SocietyBase does
public class AgentBase extends ModifiableConfigurableComponent implements AgentComponent {

  public AgentBase(String name) {
    super(name);
    createLogger();
  }

  private void createLogger() {
    log = CSMART.createLogger(this.getClass().getName());
  }

  public void initProperties() {
    // TODO: what does this need to do?
  }
}
