/* 
 * <copyright>
 * This software is to be used only in accordance with the COUGAAR license
 * agreement. The license agreement and other information can be found at
 * http://www.cougaar.org
 * 
 *       © Copyright 2001 by BBNT Solutions LLC.
 * </copyright>
 */
package org.cougaar.tools.csmart.ui.console;
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
