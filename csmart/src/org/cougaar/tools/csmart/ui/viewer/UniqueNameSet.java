/*
 * <copyright>
 *  Copyright 2000-2001 BBNT Solutions, LLC
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

package org.cougaar.tools.csmart.ui.viewer;

import java.lang.reflect.Method;
import java.util.HashSet;
import org.cougaar.util.log.Logger;
import org.cougaar.tools.csmart.ui.viewer.CSMART;

public class UniqueNameSet extends HashSet {
  private String prefix;
  private int nameCounter = 0;
  private transient Logger log;

  public UniqueNameSet(String prefix) {
    this.prefix = prefix;
    createLogger();
  }

  private void createLogger() {
    log = CSMART.createLogger(this.getClass().getName());
  }

  public void init(Object[] things, Method getNameMethod) {
    Object[] noArgs = new Object[0];
    for (int i = 0; i < things.length; i++) {
      try {
        String name = (String) getNameMethod.invoke(things[i], noArgs);
        add(name);
      } catch (Exception e) {
        if(log.isDebugEnabled()) {
          log.error("Reading: " + things[i]);
          e.printStackTrace();
        }
      }
    }
  }
    
  public String generateName() {
    return generateName(prefix);
  }

  public String generateName(String name) {
    if (contains(name)) {
      String base = name;
      do {
        name = base + ++nameCounter;
      } while (contains(name));
    }
    return name;
  }


}
