/*
 * <copyright>
 *  Copyright 2000-2003 BBNT Solutions, LLC
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

package org.cougaar.tools.csmart.ui.console;

import org.cougaar.mlm.ui.glsinit.GLSClient;

import javax.swing.*;

public class GLSClientView extends JComponent {

  /**
   * Create an internal frame that contains the GLSClient gui.
   * @param glsContactInfo Array of 4 strings: protocol, host, port, and agent 
   *        on which to contact the GLS Init Servlet.
   */
  public static JInternalFrame getGLSClientView(String[] glsContactInfo) {
    if (glsContactInfo == null || glsContactInfo.length < 4) {
      glsContactInfo = new String[4];
      glsContactInfo[0] = "http";
      glsContactInfo[1] = "localhost";
      glsContactInfo[2] = "8800";
      glsContactInfo[3] = "NCA";
    }

    // resizable, not closeable, maximizable, iconifiable
    JInternalFrame jif =
        new JInternalFrame("GLS", true, false, true, true);
    jif.getContentPane().add(new GLSClient(glsContactInfo[0], glsContactInfo[1], glsContactInfo[2], glsContactInfo[3]));
    jif.setSize(350, 400);
    jif.setLocation(0, 0);
    jif.setVisible(true);
    return jif;
  }
}
