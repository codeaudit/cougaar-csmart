/*
 * <copyright>
 * Copyright 1997-2001 Defense Advanced Research Projects
 * Agency (DARPA) and ALPINE (a BBN Technologies (BBN) and
 * Raytheon Systems Company (RSC) Consortium).
 * This software to be used only in accordance with the
 * COUGAAR licence agreement.
 * </copyright>
 */
package org.cougaar.tools.csmart.scalability;

import java.net.URL;
import java.net.MalformedURLException;

public class ScalabilityHelp {
    public static URL getURL(String anchor) {
        try {
            URL url = ScalabilityHelp.class.getResource("help.html");
            if (url != null) {
                return new URL(url.toExternalForm() + "#" + anchor);
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return null;
    }
}
