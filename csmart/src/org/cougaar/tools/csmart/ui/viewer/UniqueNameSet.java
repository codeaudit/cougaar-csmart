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

package org.cougaar.tools.csmart.ui.viewer;

import org.cougaar.util.log.Logger;

import java.lang.reflect.Method;
import java.util.HashSet;

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
