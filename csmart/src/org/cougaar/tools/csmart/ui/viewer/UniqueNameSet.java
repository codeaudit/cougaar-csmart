/*
 * <copyright>
 *  Copyright 2000-2002 BBNT Solutions, LLC
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
  protected transient Logger log;

  /**
   * Construct an object which can generate unique names
   * from the specified prefix.
   * @param prefix the prefix to use in the unique names
   */
  public UniqueNameSet(String prefix) {
    this.prefix = prefix;
    createLogger();
  }

  private void createLogger() {
    log = CSMART.createLogger(this.getClass().getName());
  }

  /**
   * Initialize the unique name set with a set of objects,
   * using the specified method to get the names of those objects.
   * @param things an array of objects used to initialize the unique name set
   * @param getNameMethod the <code>Method</code> to get the name of an object
   */
  public void init(Object[] things, Method getNameMethod) {
    Object[] noArgs = new Object[0];
    for (int i = 0; i < things.length; i++) {
      try {
        String name = (String) getNameMethod.invoke(things[i], noArgs);
        add(name);
      } catch (Exception e) {
        if(log.isErrorEnabled()) {
          log.error("Reading: " + things[i], e);
        }
      }
    }
  }

  /**
   * Get a unique name for an object.
   * @return the unique name
   */
  public String generateName() {
    return generateName(prefix);
  }

  /**
   * Generate a unique name from the given name by appending
   * a bracketed counter to it.
   * @param name the name to use as the base
   * @return a new unique name
   */
  public String generateName(String name) {
    if (contains(name)) {
      String base = name;
      int index = name.indexOf(" [");
      if (index != -1)
        base = name.substring(0, index);
      do {
        name = base + " [" + ++nameCounter + "]";
      } while (contains(name));
    }
    return name;
  }


}
