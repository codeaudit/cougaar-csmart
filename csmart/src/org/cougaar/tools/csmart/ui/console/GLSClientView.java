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
