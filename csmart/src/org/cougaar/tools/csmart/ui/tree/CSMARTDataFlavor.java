/* 
 * <copyright>
 *  Copyright 2001-2003 BBNT Solutions, LLC
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

package org.cougaar.tools.csmart.ui.tree;

import java.awt.datatransfer.DataFlavor;

public class CSMARTDataFlavor extends DataFlavor {
    /** The name of the DataFlavor parameter describing the userObject */
    public static final String userObjectClassParameter = "userObjectclass";
    /** The name of the DataFlavor parameter describing the source */
    public static final String sourceClassParameter = "sourceClass";

    public CSMARTDataFlavor(Class representationClass,
                            Class userObjectClass,
                            Class sourceClass,
                            String humanPresentableName)
    {
        super(composeMimeString(representationClass, userObjectClass, sourceClass),
              humanPresentableName);
    }

    private static String composeMimeString(Class representationClass,
                                            Class userObjectClass,
                                            Class sourceClass)
    {
        StringBuffer buf = new StringBuffer();
        buf.append(DataFlavor.javaJVMLocalObjectMimeType);
        buf.append(";class=");
        buf.append(representationClass.getName());
        if (userObjectClass!= null) {
            buf.append(";");
            buf.append(userObjectClassParameter);
            buf.append("=");
            buf.append(userObjectClass.getName());
        }
        if (sourceClass != null) {
            buf.append(";");
            buf.append(sourceClassParameter);
            buf.append("=");
            buf.append(sourceClass.getName());
        }
        return buf.substring(0);
    }

    public static String getUserObjectClassName(DataFlavor flavor) {
        return flavor.getParameter(userObjectClassParameter);
    }
    public static String getSourceClassName(DataFlavor flavor) {
        return flavor.getParameter(sourceClassParameter);
    }

    public String getUserObjectClassName() {
        return getParameter(userObjectClassParameter);
    }
    public String getSourceClassName() {
        return getParameter(sourceClassParameter);
    }
}
