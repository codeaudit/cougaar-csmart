/* 
 * <copyright>
 *  Copyright 2001-2002 BBNT Solutions, LLC
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
package org.cougaar.tools.csmart.society.scalability;

import java.util.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.cougaar.util.log.Logger;
import org.cougaar.tools.csmart.ui.viewer.CSMART;

public class Test {
    public static void test1() {
      Logger log = CSMART.createLogger("org.cougaar.tools.csmart.society.scalability.Test");
        ScalabilityXSociety society = new ScalabilityXSociety("test1");
        society.getProperty(ScalabilityXSociety.PROP_LEVELCOUNT).setValue(new Integer(3));
        society.getProperty(ScalabilityXSociety.PROP_AGENTCOUNT)
            .setValue(new int[] {1, 2, 3});
        Collection agents = society.getDescendentsOfClass(ScalabilityXAgent.class);
        if(log.isDebugEnabled()) {
          log.debug("Agents:");
        }
        for (Iterator i = agents.iterator(); i.hasNext(); ) {
            ScalabilityXAgent c = (ScalabilityXAgent) i.next();
            if(log.isDebugEnabled()) {
              log.debug("Agent " + c.getFullName());
            }
            c.printLocalProperties(System.out);
        }
        Collection plugins = society.getDescendentsOfClass(ScalabilityXPlugin.class);
        if(log.isDebugEnabled()) {
          log.debug("Plugins:");
        }
        for (Iterator i = plugins.iterator(); i.hasNext(); ) {
            ScalabilityXAgent c = (ScalabilityXAgent) i.next();
            if(log.isDebugEnabled()) {
              log.debug("Plugin " + c.getFullName());
            }
            c.printLocalProperties(System.out);
        }
    }

    public static void main(String args[]) {
        test1();
    }
}
