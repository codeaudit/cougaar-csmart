/*
 * <copyright>
 *  Copyright 1997-2002 BBNT Solutions, LLC
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

import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

import javax.swing.border.Border;
import javax.swing.*;

import java.util.ArrayList;
import java.net.URL;
import java.net.URLConnection;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class GLSClient extends JInternalFrame {
  String agentURL; // of the form http://host:port/$agent
  /** for feedback to user on whether root GLS was successful **/
  private JLabel initLabel = new JLabel ("0 GLS Tasks published ");
  private JLabel oplanLabel = new JLabel("No Oplan published");

  /** A button to push to kick things off **/
  private JButton initButton = new JButton("Send GLS root");
  private JButton oplanButton = new JButton("Publish Oplan");
  private JButton updateOplanButton = new JButton("Update Oplan");
  private JButton rescindButton = new JButton("Rescind GLS root");
  private JButton connectButton = new JButton("Connect");

  /** A combo box for selecting the Oplan **/
  private JComboBox oplanCombo = new JComboBox();

  /** A panel to hold the GUI **/
  private JPanel outerPanel = new JPanel((LayoutManager) null);

  /** number of GLS tasks published **/
  private int numGLS = 0;

  private ButtonListener buttonListener = new ButtonListener();

  private static final String GLS_INIT_SERVLET = "glsinit";
  private static final String GLS_REPLY_SERVLET = "glsreply";
  private static final String CONNECT_COMMAND = "connect";
  private static final String PUBLISH_COMMAND = "publishgls";
  private static final String RESCIND_COMMAND = "rescindgls";
  private static final String OPLAN_PARAM_NAME = "&oplanID=";

  public GLSClient(String agentURL) {
    // not closable, because it can't be recreated
    super("GLS", true, false, true, true);

    this.agentURL = agentURL;
    outerPanel.setLayout(new BoxLayout(outerPanel, BoxLayout.Y_AXIS));
    createConnectionPanel();
    createOplanPanel();
    createInitPanel();

    getRootPane().setDefaultButton(connectButton); // hitting return sends GLS
    getContentPane().add(outerPanel);
    setContentPane(outerPanel);
    setSize(350, 350);
    setLocation(0, 0);
    setVisible(true);
  }

  /** creates an x_axis oriented panel containing two components */
  private JPanel createXPanel(JComponent comp1, JComponent comp2) {
    JPanel panel = new JPanel((LayoutManager) null);
    panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
    
    panel.add(comp1);
    panel.add(comp2);
    return panel;
  }

  private JPanel createYPanel(JComponent comp1, JComponent comp2) {
    JPanel panel = new JPanel((LayoutManager) null);
    panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
    
    panel.add(comp1);
    panel.add(comp2);
    return panel;
  }

  private void createConnectionPanel() {
    JPanel connectPanel = new JPanel();
    connectPanel.setBorder(BorderFactory.createTitledBorder("Connection"));
    connectButton.addActionListener(new ConnectionButtonListener());
    connectPanel.add(connectButton);
    outerPanel.add(connectPanel);
  }

  private void createOplanPanel() {
    JPanel oplanPanel = new JPanel();
    oplanPanel.setLayout(new BoxLayout(oplanPanel, BoxLayout.Y_AXIS));
    oplanPanel.setBorder(BorderFactory.createTitledBorder("Oplan"));
    oplanButton.addActionListener(buttonListener);
    oplanButton.setActionCommand("sendoplan");
    updateOplanButton.addActionListener(buttonListener);
    updateOplanButton.setActionCommand("updateoplan");
    
    // turn this off until connection is made (even though it'll work)
    oplanButton.setEnabled(false); 
    // turn this off until an oplan shows up
    updateOplanButton.setEnabled(false); 

    JPanel buttons = new JPanel(new GridBagLayout());
    int x = 0;
    int y = 0;
    buttons.add(oplanButton,
                new GridBagConstraints(x, y++, 1, 1, 0.0, 0.0,
                                       GridBagConstraints.WEST,
                                       GridBagConstraints.HORIZONTAL,
                                       new Insets(5, 5, 5, 5),
                                       0, 0));
    buttons.add(updateOplanButton,
                new GridBagConstraints(x, y, 1, 1, 0.0, 0.0,
                                       GridBagConstraints.WEST,
                                       GridBagConstraints.HORIZONTAL,
                                       new Insets(0, 5, 5, 5),
                                       0, 0));
    oplanPanel.add(createXPanel(buttons, oplanLabel));
    outerPanel.add(oplanPanel);
  }

  private void createInitPanel() {
    initButton.setEnabled(false); // Leave this disabled until we have oplans
    rescindButton.setEnabled(false); // Leave this disabled until we have oplans
    initButton.addActionListener(buttonListener);
    initButton.setActionCommand("publishgls");
    rescindButton.addActionListener(buttonListener);
    rescindButton.setActionCommand("rescindgls");

    JLabel oplanComboLabel = new JLabel("Select Oplan");
    JPanel buttonPanel = createYPanel(initLabel, initButton);
    buttonPanel.add(rescindButton);
    
    JPanel glsButtons = new JPanel(new GridBagLayout());
    int x = 0;
    int y = 0;
    glsButtons.add(initLabel,
                   new GridBagConstraints(x, y++, 1, 1, 0.0, 0.0,
                                          GridBagConstraints.WEST,
                                          GridBagConstraints.HORIZONTAL,
                                          new Insets(5, 5, 5, 5),
                                          0, 0));
    glsButtons.add(initButton,
                   new GridBagConstraints(x, y++, 1, 1, 0.0, 0.0,
                                          GridBagConstraints.WEST,
                                          GridBagConstraints.HORIZONTAL,
                                          new Insets(0, 5, 5, 5),
                                          0, 0));
    glsButtons.add(rescindButton,
                   new GridBagConstraints(x, y, 1, 1, 0.0, 0.0,
                                          GridBagConstraints.WEST,
                                          GridBagConstraints.HORIZONTAL,
                                          new Insets(0, 5, 5, 5),
                                          0, 0));
    JPanel comboPanel = new JPanel(new GridBagLayout());
    x = 0;
    y = 0;
    comboPanel.add(oplanComboLabel,
                   new GridBagConstraints(x, y++, 1, 1, 0.0, 0.0,
                                          GridBagConstraints.CENTER,
                                          GridBagConstraints.NONE,
                                          new Insets(5, 5, 5, 5),
                                          0, 0));
    comboPanel.add(oplanCombo,
                   new GridBagConstraints(x, y, 1, 1, 1.0, 0.0,
                                          GridBagConstraints.CENTER,
                                          GridBagConstraints.HORIZONTAL,
                                          new Insets(5, 5, 5, 5),
                                          0, 0));
    JPanel glsPanel = createXPanel(glsButtons, comboPanel);
    glsPanel.setBorder(BorderFactory.createTitledBorder("GLS"));
    outerPanel.add(glsPanel);
  }

  private boolean comboContains(String id) {
    for (int i = 0, n = oplanCombo.getItemCount(); i < n; i++) {
      OplanInfo current = (OplanInfo) oplanCombo.getItemAt(i);
      if (current.getOplanId().equals(id)) {
	return true;
      }
    }
    return false;
  }

  /**
   * Creates url of the form:
   * http://localhost:8800/$NCA/?command=glsinit&oplanID=doit
   */

  private String getURLString(String servlet, String command, String oplanId) {
    StringBuffer urlString = new StringBuffer(agentURL);
    urlString.append('/');
    urlString.append(servlet);
    urlString.append("?command=");
    urlString.append(command);
    if (oplanId != null) {
      urlString.append(OPLAN_PARAM_NAME);
      urlString.append(oplanId);
    }
    return urlString.toString();
  }

  /**
   * Invoked when the user selects buttons:
   * Publish Oplan, Update Oplan, Send GLS Root, Rescind GLS Root
   * Sends appropriate command to agent.
   */

  private class ButtonListener implements ActionListener {
    public void actionPerformed(ActionEvent ae) {
      String command = ae.getActionCommand();
      String oplanId = null;
      if (command.equals(PUBLISH_COMMAND) || command.equals(RESCIND_COMMAND))
        oplanId = ((OplanInfo)oplanCombo.getSelectedItem()).getOplanId();
      String urlString = getURLString(GLS_INIT_SERVLET, command, oplanId);
      URL url;
      URLConnection urlConnection;
      try {
	url = new URL(urlString.toString());
        if (url == null)
          return;
        urlConnection = url.openConnection();
        if (urlConnection == null)
          return;
  	BufferedReader in = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
  	String inputLine;
  	while ((inputLine = in.readLine()) != null) {
          // read and discard any feedback
	}
  	in.close();
      } catch (java.net.MalformedURLException me) {
	JOptionPane.showMessageDialog(outerPanel, 
                                      "Invalid Host, Port, Agent or Servlet.",
                                      "Bad URL", 
                                      JOptionPane.ERROR_MESSAGE);
        me.printStackTrace();
        return;
      } catch (java.io.IOException ioe) {
	JOptionPane.showMessageDialog(outerPanel, 
                                      "Could not connect to Host, Port, or Agent.\nThe Servlet may not be running there.",
                                      "No Servlet", 
                                      JOptionPane.ERROR_MESSAGE);
	ioe.printStackTrace();
      }
    }
  } // end of ButtonListener class

  /**
   * Invoked when user selects "Connect" button.
   * Attempts to connect to the NCA agent in the society running
   * in the current experiment.
   */

  private class ConnectionButtonListener implements ActionListener {
    public void actionPerformed(ActionEvent ae) {
      String urlString = getURLString(GLS_REPLY_SERVLET, 
                                      CONNECT_COMMAND, null);
      URLConnection urlConnection = null;
      try {
	URL url = new URL(urlString);
        urlConnection = url.openConnection();
      } catch (java.net.MalformedURLException me) {
	JOptionPane.showMessageDialog(outerPanel, 
                                      "Invalid Host, Port, Agent or Servlet.",
                                      "Bad URL", 
                                      JOptionPane.ERROR_MESSAGE);
        me.printStackTrace();
	return;
      } catch (java.io.IOException ioe) {
	JOptionPane.showMessageDialog(outerPanel, 
                                      "Could not connect to Host, Port, or Agent.\nThe Servlet may not be running there.",
                                      "No Servlet", 
                                      JOptionPane.ERROR_MESSAGE);
	ioe.printStackTrace();
	return;
      }
      // read from open connection
      if (urlConnection != null) 
        (new LineReader(urlConnection)).start();
    }
  } // end ConnectionButtonListener

  /**
   * Reads information from an open URL connection.
   */

  private class LineReader extends SwingWorker {
    private URLConnection urlConnection;
    public LineReader(URLConnection urlConnection) {
      super();
      this.urlConnection = urlConnection;
    }
    
    public Object construct() {
      connectButton.setEnabled(false);
      oplanButton.setEnabled(true);
      getRootPane().setDefaultButton(oplanButton); 
      try {
  	BufferedReader in = 
          new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
  	String inputLine;
  	while ((inputLine = in.readLine()) != null) {
  	  //System.out.println(inputLine);
	  if (inputLine.indexOf("oplan") > 0)
            addOplan(inputLine);
	  if (inputLine.indexOf("GLS") > 0)
            updateGLSTasks(inputLine);
	}
  	in.close();
      } catch (java.io.IOException e) {
	connectButton.setEnabled(true);
	JOptionPane.showMessageDialog(outerPanel, 
                                      "Could not read from servlet.",
                                      "Bad Connection",
                                      JOptionPane.ERROR_MESSAGE);
	return null;
      } catch (Exception e) {
	e.printStackTrace();
	return null;
      }

      if (oplanCombo.getItemCount() != 0) {
        oplanButton.setEnabled(false);
        oplanLabel.setText("Oplan Published");
        updateOplanButton.setEnabled(true);
        initButton.setEnabled(true);
        getRootPane().setDefaultButton(initButton); 
      }
      return null;
    }

    /**
     * Add OPlan read from the agent to the list of available oplans.
     */

    private void addOplan(String s) {
      int nameIndex = s.indexOf("name=") + 5;
      int idIndex = s.indexOf("id=");
      String name = s.substring(nameIndex, idIndex).trim();
      String id = s.substring(idIndex +3, s.length()-1);
      if (!comboContains(id)) {
        oplanCombo.addItem(new OplanInfo(name, id));
      }
    }

    /**
     * Update information about GLS tasks from information
     * read from the agent.
     */

    private void updateGLSTasks(String s) {
      int numGLS = Integer.parseInt(s.substring(s.indexOf("GLS")+4, 
                                                s.length()-1));
      initLabel.setText(numGLS + " GLS Tasks published");
      if (numGLS > 0) 
        rescindButton.setEnabled(true);
      else
        rescindButton.setEnabled(false);
    }

  } // end LineReader class

  private class OplanInfo {
    private String oplanName;
    private String oplanID;

    public OplanInfo(String name, String id) {
      oplanName = name;
      oplanID = id;
    }
    public String toString() {
      return oplanName;
    }
    public String getOplanId() {
      return oplanID;
    }
  }

}
