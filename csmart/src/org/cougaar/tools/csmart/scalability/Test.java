/* 
 * <copyright>
 * This software is to be used only in accordance with the COUGAAR license
 * agreement. The license agreement and other information can be found at
 * http://www.cougaar.org
 * 
 *       © Copyright 2001 by BBNT Solutions LLC.
 * </copyright>
 */
package org.cougaar.tools.csmart.scalability;

import org.cougaar.tools.csmart.ui.component.*;
import java.util.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Test {
    public static void test1() {
        ScalabilityXSociety society = new ScalabilityXSociety("test1");
        society.getProperty(ScalabilityXSociety.PROP_LEVELCOUNT).setValue(new Integer(3));
        society.getProperty(ScalabilityXSociety.PROP_AGENTCOUNT)
            .setValue(new int[] {1, 2, 3});
        Collection agents = society.getDescendentsOfClass(ScalabilityXAgent.class);
        System.out.println("Agents:");
        for (Iterator i = agents.iterator(); i.hasNext(); ) {
            ScalabilityXAgent c = (ScalabilityXAgent) i.next();
            System.out.println("Agent " + c.getName());
            c.printLocalProperties(System.out);
        }
        Collection plugins = society.getDescendentsOfClass(ScalabilityXPlugIn.class);
        System.out.println("Plugins:");
        for (Iterator i = plugins.iterator(); i.hasNext(); ) {
            ScalabilityXAgent c = (ScalabilityXAgent) i.next();
            System.out.println("Plugin " + c.getName());
            c.printLocalProperties(System.out);
        }
    }

    public static void main(String args[]) {
        test1();
    }
}
