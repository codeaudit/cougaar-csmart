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

import java.io.Serializable;
import java.util.Iterator;
import org.cougaar.tools.csmart.core.cdata.PGPropData;
import org.cougaar.tools.csmart.core.cdata.PropGroupData;
import org.cougaar.tools.csmart.core.property.ModifiableConfigurableComponent;
import org.cougaar.tools.csmart.core.property.Property;
import org.cougaar.tools.csmart.ui.viewer.CSMART;
import org.cougaar.util.log.Logger;


/**
 * INIPropGroup.java
 *
 *
 * Created: Thu Feb 21 15:57:03 2002
 *
 * @author <a href="mailto:bkrisler@bbn.com">Brian Krisler</a>
 * @version 1.0
 */

public class PropGroupBase 
  extends ModifiableConfigurableComponent 
  implements PropGroupComponent {
  
  private PropGroupData pgd;
  private transient Logger log;

  private Property[] propProperties = null;

  public PropGroupBase(PropGroupData pgd){
    super(pgd.getName());
    this.pgd = pgd;
    createLogger();
  }
  
  public void initProperties() {
    propProperties = new Property[pgd.getPropertyCount()];
    Iterator iter = pgd.getPropertiesIterator();
    int i=0;
    while(iter.hasNext()) {
      PGPropData data = (PGPropData)iter.next();
      // Compose Name of Name and Type. 
      String name = data.getName() + "  (" + data.getType() + ")";
      propProperties[i++] = addProperty(name, data.getValue());
    }
  }

  private void createLogger() {
    log = CSMART.createLogger(this.getClass().getName());
  }


  private String getName(Property prop) {
    String name = prop.getName().toString();
    return name.substring(0, name.indexOf("("));
  }

  private String getType(Property prop) {
    String name = prop.getName().toString();
    return name.substring(name.indexOf("(")+1, name.indexOf(")"));
  }

  public PropGroupData getPropGroupData() {
        PropGroupData pgData = new PropGroupData(this.getShortName());
    
        for(int i=0; i < propProperties.length; i++) {
          Property prop = propProperties[i];
          PGPropData pg = new PGPropData();
          pg.setName(getName(prop));
          pg.setType(getType(prop));
          pg.setValue((String)prop.getValue());
          pgData.addProperty(pg);
        }
    
    return pgData;
  }

}
