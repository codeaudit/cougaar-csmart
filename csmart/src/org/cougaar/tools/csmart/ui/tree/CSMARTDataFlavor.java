/* 
 * <copyright>
 *  
 *  Copyright 2001-2004 BBNT Solutions, LLC
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
