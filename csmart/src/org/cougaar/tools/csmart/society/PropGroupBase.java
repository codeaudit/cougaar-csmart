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

import org.cougaar.tools.csmart.core.cdata.PGPropData;
import org.cougaar.tools.csmart.core.cdata.PGPropMultiVal;
import org.cougaar.tools.csmart.core.cdata.PropGroupData;
import org.cougaar.tools.csmart.core.property.ModifiableConfigurableComponent;
import org.cougaar.tools.csmart.core.property.Property;
import org.cougaar.tools.csmart.core.property.name.CompositeName;
import org.cougaar.tools.csmart.ui.viewer.CSMART;
import org.cougaar.util.log.Logger;

import java.util.Collection;
import java.util.Iterator;


/**
 * PropGroupBase
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

  public PropGroupBase(PropGroupData pgd){
    super(pgd.getName());
    this.pgd = pgd;
    createLogger();
  }
  
  public void initProperties() {
    Iterator iter = pgd.getPropertiesIterator();
    while(iter.hasNext()) {
      PGPropData data = (PGPropData)iter.next();
      // Compose Name of Name and Type. 
      String name;
      if(data.getSubType() == null || data.getSubType().equals("")) {
        name = data.getName() + "  (" + data.getType() + ")";
      } else {
        name = data.getName() + " ("+ data.getSubType() + ")";
      }
      
      if (data.isListType())
	addProperty(name, ((PGPropMultiVal)data.getValue()).getValuesStringArray());
      else
	addProperty(name, data.getValue());
    }
  }

  private void createLogger() {
    log = CSMART.createLogger(this.getClass().getName());
  }

  private String getName(Property prop) {
    String name = prop.getName().toString();
    int index = name.lastIndexOf('.');
    if (index != -1)
      name = name.substring(index+1);
      name = name.substring(0, ((name.indexOf("(") == -1) ? name.length() : name.indexOf("(")));
    return name.trim();
  }

  private String getType(Property prop) {
    String name = prop.getName().toString();
    int typeStart = name.indexOf("(");
    if (typeStart == -1)
      return new String("String");
    else
      return name.substring(name.indexOf("(")+1, name.indexOf(")"));
  }

  private PGPropData getProp(String name) {
    PGPropData[] props = pgd.getProperties();
    for(int i=0; i < props.length; i++) {
      if(props[i].getName().equals(name)) {
        return props[i];
      }
    }
    return null;
  }

  // For constructing component data
  public PropGroupData getPropGroupData() {
    PropGroupData pgData = new PropGroupData(this.getShortName());
    
    Iterator props = getSortedLocalPropertyNames();
    while (props.hasNext()) {
      Property prop;
      CompositeName pname = (CompositeName)props.next();
      if (pname != null)
	prop = getProperty(pname);
      else
	continue;
      PGPropData oldPG = getProp(getName(prop));
      PGPropData pg = new PGPropData();
      if(oldPG != null) {
	pg.setName(oldPG.getName());
	pg.setType(oldPG.getType());
	pg.setSubType(oldPG.getSubType());
      } else {
	pg.setName(getName(prop));
	String type = getType(prop);
	// if the value for this property is a list, then this type
	// is really the subtype
	// do a prop.getPropertyClass()?
	// This doesnt appear usually set, cant depend on it
	// So what, get the value?
	if ((prop.getValue() != null) && 
	    (prop.getValue().getClass().isArray() ||
	     Collection.class.isAssignableFrom(prop.getValue().getClass()))) {
	  pg.setSubType(type);
	  pg.setType("Collection");
	} else {
	  // otherwise, set this as the type
	  pg.setType(type);
	  // Note you'll get a warning about a null subtype
	  // when saving to DB
	}
      }
      
      if (pg.isListType()) {
	pg.setValue(new PGPropMultiVal(prop.getValue()));
      } else {
	pg.setValue(prop.getValue());
      }	    
      
      pgData.addProperty(pg);
    }
    
    return pgData;
  }
  
}
