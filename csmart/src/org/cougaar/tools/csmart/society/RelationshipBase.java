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

import org.cougaar.tools.csmart.core.cdata.RelationshipData;
import org.cougaar.tools.csmart.core.property.ModifiableConfigurableComponent;
import org.cougaar.tools.csmart.core.property.Property;
import org.cougaar.tools.csmart.ui.viewer.CSMART;
import org.cougaar.util.log.Logger;

/**
 * RelationshipBase.java
 *
 *
 * Created: Fri Feb 22 12:32:59 2002
 *
 * @author <a href="mailto:bkrisler@bbn.com">Brian Krisler</a>
 * @version 1.0
 */

public class RelationshipBase 
  extends ModifiableConfigurableComponent
  implements RelationshipComponent {
  private transient Logger log;

  public static final String PROP_TYPE = "Type";
  public static final String PROP_TYPE_DESC = "Type of Relationship";

  public static final String PROP_ROLE = "Role";
  public static final String PROP_ROLE_DESC = "Roles performed by this relationship";

  public static final String PROP_ITEM = "Item";
  public static final String PROP_ITEM_DESC = "Item";

  private Property propType;
  private Property propRole;
  private Property propItem;
  private RelationshipData relationship;

  public RelationshipBase (RelationshipData relationship, int index){
    super(relationship.getItem());
    createLogger();
    this.relationship = relationship;
  }

  private void createLogger() {
    log = CSMART.createLogger(this.getClass().getName());
  }

  public void initProperties() {
    propType = addProperty(PROP_TYPE, relationship.getType());
    propType.setToolTip(PROP_TYPE_DESC);
    propRole = addProperty(PROP_ROLE, relationship.getRole());
    propRole.setToolTip(PROP_ROLE_DESC);
    propItem = addProperty(PROP_ITEM, relationship.getItem());
    propItem.setToolTip(PROP_ITEM_DESC);
  }

}// RelationshipBase
