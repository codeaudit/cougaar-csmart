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

package org.cougaar.tools.csmart.ui.monitor.plan;

import att.grappa.*;
import org.cougaar.tools.csmart.ui.monitor.PropertyNames;
import org.cougaar.tools.csmart.ui.monitor.generic.CSMARTFrame;
import org.cougaar.tools.csmart.ui.monitor.generic.CSMARTGraph;
import org.cougaar.tools.csmart.ui.viewer.CSMART;
import org.cougaar.util.log.Logger;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

/**
 * Find plan object from a dialog in which the user can specify:
 * community name, agent name, plan object type, and for some
 * objects, additional object-specific information.
 */

public class ULPlanFinder extends JDialog {
  private static final String ALL = "ALL";
  private CSMARTGraph graph;
  private CSMARTFrame frame;
  private JPanel findPanel;
  private JComboBox communityCB;
  private JComboBox agentCB;
  private JComboBox planObjectTypeCB;
  private int selectedIndex;
  private int maxSelectedIndex;
  private JButton findNextButton;
  private Vector selectedObjects;
  private Hashtable communityToAgents;
  Box box = new Box(BoxLayout.Y_AXIS);
  private Vector loadedPanels = new Vector();
  private Vector planObjects;
  private Vector planElements;
  // Time panel
  private JPanel timePanel;
  private JLabel fromLabel;
  private JLabel toLabel;
  private boolean allTimeFilter;
  private double startTime;
  private double endTime;
  private JTextField startTimeField;
  private JTextField endTimeField;
  // TASK Panel 
  private JPanel taskPanel;
  private JComboBox verbCB;
  private JComboBox directObjUIDCB;
  private JComboBox prepPhraseCB;
  private JPanel taskRangePanel;
  private boolean allTaskFilter;
  private JTextField taskStartField;
  private JLabel taskStartLabel;
  private JTextField taskEndField;
  private JLabel taskEndLabel;
  private double taskStart;
  private double taskEnd;
  // ASSET Panel 
  private JPanel assetPanel;
  private JComboBox assetAgentCB;
  private JComboBox assetGroupNameCB;
  private JComboBox assetNameCB;
  // asset transfer panel
  private JPanel assetTransferPanel;
  private JComboBox assetTransUIDCB;
  private JComboBox assignUIDCB;
  private JComboBox assignorCB;
  // Happiness Panel 
  private JPanel happinessPanel;
  private JPanel happinessRangePanel;
  private JComboBox regardingCB;
  private JComboBox publisherCB;
  private JComboBox sourceCB;
  private JComboBox descriptionCB;
  private boolean allRatingFilter;
  private float ratingStart;
  private float ratingEnd;
  private JTextField ratingStartField;
  private JLabel ratingStartLabel;
  private JTextField ratingEndField;
  private JLabel ratingEndLabel;
  private boolean allDeltaFilter;
  private float deltaStart;
  private float deltaEnd;
  private JTextField deltaStartField;
  private JLabel deltaStartLabel;
  private JTextField deltaEndField;
  private JLabel deltaEndLabel;
  private boolean allCumulFilter;
  private double eventCumulStart;
  private double eventCumulEnd;
  private JLabel eventCumulStartLabel;
  private JTextField eventCumulStartField;
  private JLabel eventCumulEndLabel;
  private JTextField eventCumulEndField;
  private boolean allComplFilter;
  private long timeComplStart;
  private long timeComplEnd;
  private JLabel timeComplStartLabel;
  private JTextField timeComplStartField;
  private JLabel timeComplEndLabel;
  private JTextField timeComplEndField;
  // allocation panel
  private JPanel allocationPanel;
  private JComboBox allocToAgentCB;
  private JComboBox allocTaskUIDCB;
  private JComboBox allocAssetUIDCB;
  private JComboBox allocRemoteAgentUIDCB;
  private JComboBox allocLocalOrgUIDCB;
  private ActionListener disableFindNext;
  private JPanel successPanel;
  private JCheckBox successCBox;
  // status panel
  private JPanel statusPanel;
  private JRadioButton estimatedButton;
  private JRadioButton reportedButton;
  private JRadioButton observedButton;
  private ButtonGroup statusButtonGroup;
  private String status;
  private transient Logger log;

  public ULPlanFinder(CSMARTFrame frame, 
                      CSMARTGraph graph, 
                      Hashtable communityToAgents) {

    super(frame, "Finder");

    createLogger();
    this.communityToAgents = communityToAgents;
    this.graph = graph;
    this.frame = frame;
    findPanel = new JPanel(new BorderLayout());

    // community
    JPanel communityPanel = new JPanel();
    communityPanel.setLayout(new GridBagLayout());
    TitledBorder communityTitledBorder = new TitledBorder("Community");
    Font font = communityTitledBorder.getTitleFont();
    Font titleFont = font.deriveFont(Font.ITALIC);
    communityTitledBorder.setTitleFont(titleFont);
    communityPanel.setBorder(communityTitledBorder);
    JLabel communityLabel = new JLabel("Community: ");

    communityCB = new JComboBox();
    Enumeration keys = communityToAgents.keys();
    communityCB.addItem(ALL);
    while (keys.hasMoreElements()) {
      communityCB.addItem((String)keys.nextElement());
    }
    communityCB.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
	communityActionPerformed(e);
      }
    });
    communityPanel.add(communityLabel,
		   new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
					  GridBagConstraints.WEST,
					  GridBagConstraints.HORIZONTAL,
					  new Insets(0, 0, 0, 0), 0, 0));
    communityPanel.add(communityCB,
		   new GridBagConstraints(1, 0, 1, 1, 1.0, 0.0,
					  GridBagConstraints.WEST,
					  GridBagConstraints.HORIZONTAL,
					  new Insets(0, 0, 0, 0), 0, 0));
    box.add(communityPanel);

    // agent
    JPanel agentPanel = new JPanel();
    agentPanel.setLayout(new GridBagLayout());
    TitledBorder agentTitledBorder = new TitledBorder("Agent");
    agentTitledBorder.setTitleFont(titleFont);
    agentPanel.setBorder(agentTitledBorder);
    JLabel agentLabel = new JLabel("Agent: ");
    agentCB = new JComboBox();
    agentPanel.add(agentLabel,
		   new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
					  GridBagConstraints.WEST,
					  GridBagConstraints.HORIZONTAL,
					  new Insets(0, 0, 0, 0), 0, 0));
    agentPanel.add(agentCB,
		   new GridBagConstraints(1, 0, 1, 1, 1.0, 0.0,
					  GridBagConstraints.WEST,
					  GridBagConstraints.HORIZONTAL,
					  new Insets(0, 0, 0, 0), 0, 0));
    box.add(agentPanel);

    // planObject types
    JPanel planObjectPanel = new JPanel();
    planObjectPanel.setLayout(new GridBagLayout());
    TitledBorder planObjectTitledBorder = new TitledBorder("PlanObject Type");
    planObjectTitledBorder.setTitleFont(titleFont);
    planObjectPanel.setBorder(planObjectTitledBorder);
    JLabel planObjectTypeLabel = new JLabel("PlanObject Types: ");

    planObjectTypeCB = new JComboBox();
    planObjectTypeCB.addItem(ALL);
    planObjects = graph.getValuesOfAttribute(PropertyNames.OBJECT_TYPE);
    planElements = graph.getValuesOfAttribute(PropertyNames.PLAN_ELEMENT_TYPE);
    for (int i = 0; i < planObjects.size(); i++ ) {
      if (planObjects.elementAt(i).equals("Plan_Element")) {
        for (int j = 0; j < planElements.size(); j++ ) {
          planObjectTypeCB.addItem(planElements.elementAt(j));
        }
      } else {
        planObjectTypeCB.addItem(planObjects.elementAt(i));
      }
    }
    planObjectTypeCB.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
	planObjectTypeActionPerformed(e);
      }
    });

    planObjectPanel.add(planObjectTypeLabel,
		   new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
					  GridBagConstraints.WEST,
					  GridBagConstraints.HORIZONTAL,
					  new Insets(0, 0, 0, 0), 0, 0));
    planObjectPanel.add(planObjectTypeCB,
		   new GridBagConstraints(1, 0, 1, 1, 1.0, 0.0,
					  GridBagConstraints.WEST,
					  GridBagConstraints.HORIZONTAL,
					  new Insets(0, 0, 0, 0), 0, 0));
    box.add(planObjectPanel);

    findPanel.add(box, BorderLayout.CENTER);

    JPanel buttonPanel = new JPanel();
    final JButton findButton = new JButton("Find");
    findButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
	find();
	findButton.setEnabled(false);
      }
    });
    buttonPanel.add(findButton);
    findNextButton = new JButton("Find Next");
    // find next is disabled until user selects find
    findNextButton.setEnabled(false);
    findNextButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
	findNext();
      }
    });
    buttonPanel.add(findNextButton);
    JButton cancelButton = new JButton("Cancel");
    cancelButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
	cancel();
      }
    });
    buttonPanel.add(cancelButton);
    findPanel.add(buttonPanel, BorderLayout.SOUTH);
    getContentPane().add(findPanel);
    pack();

    // find next is disabled and find is enabled
    // when any other dialog change is made
    // so user must select "find" to process the values in the dialog
    // and determine the new set of selected objects
    disableFindNext = new ActionListener() {
      public void actionPerformed(ActionEvent e) {
	findNextButton.setEnabled(false);
	findButton.setEnabled(true);
      }
    };
    communityCB.addActionListener(disableFindNext);
    agentCB.addActionListener(disableFindNext);
    planObjectTypeCB.addActionListener(disableFindNext);
  }


  private void createLogger() {
    log = CSMART.createLogger(this.getClass().getName());
  }

  /**
   * User selected find button. Scroll to first selected object.
   */

  private void find() {
    selectedObjects = getSelectedObjects();
    if (selectedObjects.size() == 0)
      return;
    selectedIndex = 0;
    maxSelectedIndex = selectedObjects.size();
    findNext();
    findNextButton.setEnabled(true);
  }

  /**
   * User selected find next button. Scroll to next selected object.
   * If at last selected object, cycle around to the first one.
   */

  private void findNext() {
    graph.select((Element)selectedObjects.elementAt(selectedIndex++));
    if (selectedIndex == maxSelectedIndex)
      selectedIndex = 0;
    frame.scrollToSelected();
  }

  /**
   * User selected community; set agents in that community in the
   * agent combo box.
   */

  private void communityActionPerformed(ActionEvent e) {
    JComboBox cb = (JComboBox)e.getSource();
    if (agentCB.getItemCount() != 0)
      agentCB.removeAllItems();
    Object selection = cb.getSelectedItem();
    if (((String)selection).equals(ALL))
      return; // do nothing

    agentCB.addItem(ALL);
    Vector agentNames = ((Vector)communityToAgents.get(cb.getSelectedItem()));
      for (int i = 0; i < agentNames.size(); i++)
        agentCB.addItem(agentNames.elementAt(i));
  }

  /**
   * User plan object type; enable plan object type specific panels.
   */

  private void planObjectTypeActionPerformed(ActionEvent e) {
    JComboBox cb = (JComboBox)e.getSource();
    String s = (String)cb.getSelectedItem();
    if (s.equals(ALL)){
      if (!loadedPanels.isEmpty()) {
        for (int i=0; i<loadedPanels.size(); i++) 
          box.remove((JPanel)loadedPanels.elementAt(i));
        pack();
      }
      return; // do nothing
    }
    if (s.equals(PropertyNames.TASK_OBJECT)) {
    // handle TASK_OBJECT
      if (!loadedPanels.isEmpty()) {
        for (int i=0; i<loadedPanels.size(); i++) {
          box.remove((JPanel)loadedPanels.elementAt(i));
          pack();
        }
      }
      taskPanel = new JPanel();
      taskPanel.setLayout(new GridBagLayout());
      TitledBorder taskTitledBorder = new TitledBorder("Task");
      taskPanel.setBorder(taskTitledBorder);
      verbCB = new JComboBox();
      verbCB.addItem(ALL);
      Vector verbs = graph.getValuesOfAttribute(PropertyNames.TASK_VERB);
      for (int i = 0; i < verbs.size(); i++) 
        verbCB.addItem(verbs.elementAt(i));
      JLabel verbLabel = new JLabel ("Verb :");
      verbCB.addActionListener(disableFindNext);
      JLabel directObjUIDLabel = new JLabel ("Direct Object UID :");
      directObjUIDCB = new JComboBox();
      directObjUIDCB.addItem(ALL);
      Vector dirObjs = graph.getValuesOfAttribute(PropertyNames.TASK_DIRECT_OBJECT_UID);
      for (int i = 0; i < dirObjs.size(); i++ )
        directObjUIDCB.addItem(dirObjs.elementAt(i));
      directObjUIDCB.addActionListener(disableFindNext);
      JLabel prepPhraseLabel = new JLabel ("Prep Phrase :");
      prepPhraseCB = new JComboBox();
      prepPhraseCB.addItem(ALL);
      Vector prepPhrases = graph.getValuesOfAttribute(PropertyNames.TASK_PREP_PHRASE);
      for (int i=0; i<prepPhrases.size(); i++)
        prepPhraseCB.addItem(prepPhrases.elementAt(i));
      prepPhraseCB.addActionListener(disableFindNext);
      int x = 0;
      int y = 0;
      taskPanel.add(verbLabel,
                    new GridBagConstraints(x++, y, 1, 1, 1.0, 0.0,
                                           GridBagConstraints.WEST,
                                           GridBagConstraints.HORIZONTAL,
                                           new Insets(0, 0, 0, 0), 0, 0));
      taskPanel.add(verbCB,
                    new GridBagConstraints(x++, y, 1, 1, 1.0, 0.0,
                                           GridBagConstraints.WEST,
                                           GridBagConstraints.HORIZONTAL,
                                           new Insets(0, 0, 0, 0), 0, 0));
      y = 1;
      x = 0;
      taskPanel.add(directObjUIDLabel,
                    new GridBagConstraints(x++, y, 1, 1, 1.0, 0.0,
                                           GridBagConstraints.WEST,
                                           GridBagConstraints.HORIZONTAL,
                                           new Insets(0, 0, 0, 0), 0, 0));
      taskPanel.add(directObjUIDCB,
                    new GridBagConstraints(x++, y, 1, 1, 1.0, 0.0,
                                           GridBagConstraints.WEST,
                                           GridBagConstraints.HORIZONTAL,
                                           new Insets(0, 0, 0, 0), 0, 0));
      y = 2;
      x = 0;
      taskPanel.add(prepPhraseLabel,
                    new GridBagConstraints(x++, y, 1, 1, 1.0, 0.0,
                                           GridBagConstraints.WEST,
                                           GridBagConstraints.HORIZONTAL,
                                           new Insets(0, 0, 0, 0), 0, 0));
      taskPanel.add(prepPhraseCB,
                    new GridBagConstraints(x++, y, 1, 1, 1.0, 0.0,
                                           GridBagConstraints.WEST,
                                           GridBagConstraints.HORIZONTAL,
                                           new Insets(0, 0, 0, 0), 0, 0));

      box.add(taskPanel);
      // task end time panel
      taskRangePanel = new JPanel();
      taskRangePanel.setLayout(new GridBagLayout());
      taskStartLabel = new JLabel("Task Start :");
      taskStartLabel.setEnabled(false);
      taskStartField = new JTextField(10);
      taskStartField.setEnabled(false);
      taskEndLabel = new JLabel("Task End :");
      taskEndLabel.setEnabled(false);
      taskEndField = new JTextField(10);
      taskEndField.setEnabled(false);
      JRadioButton allTaskButton = new JRadioButton("All");
      allTaskFilter = true;
      allTaskButton.setFocusPainted(false);
      allTaskButton.setSelected(true);
      allTaskButton.addActionListener(disableFindNext);
      allTaskButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent evt) {
          allTaskFilter = true;
          taskStartLabel.setEnabled(false);
          taskStartField.setEnabled(false);
          taskEndLabel.setEnabled(false);
          taskEndField.setEnabled(false);
        }
      });
      JRadioButton specificTaskButton = new JRadioButton();
      specificTaskButton.setFocusPainted(false);
      specificTaskButton.addActionListener(disableFindNext);
      specificTaskButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent evt) {
          allTaskFilter = false;
          taskStartLabel.setEnabled(true);
          taskStartField.setEnabled(true);
          taskEndLabel.setEnabled(true);
          taskEndField.setEnabled(true);
        }
      });
      ButtonGroup taskButtonGroup = new ButtonGroup();
      taskButtonGroup.add(allTaskButton);
      taskButtonGroup.add(specificTaskButton);
      x=0;
      y=0;
      taskRangePanel.add(allTaskButton,
                    new GridBagConstraints(x++, y, 1, 1, 1.0, 0.0,
                                           GridBagConstraints.WEST,
                                           GridBagConstraints.HORIZONTAL,
                                           new Insets(0, 0, 0, 0), 0, 0));
      taskRangePanel.add(specificTaskButton,
                    new GridBagConstraints(x++, y, 1, 1, 1.0, 0.0,
                                           GridBagConstraints.WEST,
                                           GridBagConstraints.HORIZONTAL,
                                           new Insets(0, 0, 0, 0), 0, 0));
      taskRangePanel.add(taskStartLabel,
                     new GridBagConstraints(x++, y, 1, 1, 1.0, 0.0,
                                            GridBagConstraints.WEST,
                                            GridBagConstraints.HORIZONTAL,
                                            new Insets(0, 0, 0, 0), 0, 0));
      taskRangePanel.add(taskStartField,
                     new GridBagConstraints(x++, y, 1, 1, 1.0, 0.0,
                                            GridBagConstraints.WEST,
                                            GridBagConstraints.HORIZONTAL,
                                            new Insets(0, 0, 0, 0), 0, 0));
      taskRangePanel.add(taskEndLabel,
                     new GridBagConstraints(x++, y, 1, 1, 1.0, 0.0,
                                            GridBagConstraints.WEST,
                                            GridBagConstraints.HORIZONTAL,
                                            new Insets(0, 0, 0, 0), 0, 0));
      taskRangePanel.add(taskEndField,
                     new GridBagConstraints(x++, y, 1, 1, 1.0, 0.0,
                                            GridBagConstraints.WEST,
                                            GridBagConstraints.HORIZONTAL,
                                            new Insets(0, 0, 0, 0), 0, 0));

      box.add(taskRangePanel);
      pack();
      if (!loadedPanels.isEmpty())
        loadedPanels.clear();
      loadedPanels.addElement(taskPanel);
      loadedPanels.addElement(taskRangePanel);
    }else if (s.equals(PropertyNames.ASSET_OBJECT))  {
      // handle ASSET_OBJECT
      if (!loadedPanels.isEmpty()) {
        for (int i=0; i<loadedPanels.size(); i++) {
          box.remove((JPanel)loadedPanels.elementAt(i));
          pack();
        }
      }
      assetPanel = new JPanel();
      assetPanel.setLayout(new GridBagLayout());
      TitledBorder assetTitledBorder = new TitledBorder("Asset");
      assetPanel.setBorder(assetTitledBorder);
      JLabel assetAgentLabel = new JLabel("Asset Agent :");
      assetAgentCB = new JComboBox();
      assetAgentCB.addItem(ALL);
      Vector assetAgents = graph.getValuesOfAttribute(PropertyNames.ASSET_AGENT);
      for (int i=0; i<assetAgents.size(); i++) 
        assetAgentCB.addItem(assetAgents.elementAt(i));
      assetAgentCB.addActionListener(disableFindNext);

      JLabel assetGroupNameLabel = new JLabel("Asset Group Name :");
      assetGroupNameCB = new JComboBox();
      assetGroupNameCB.addItem(ALL);
      Vector assetGroupNames = graph.getValuesOfAttribute(PropertyNames.ASSET_GROUP_NAME);
      for (int i=0; i<assetGroupNames.size(); i++) 
        assetGroupNameCB.addItem(assetGroupNames.elementAt(i));
      assetGroupNameCB.addActionListener(disableFindNext);

      JLabel assetNameLabel = new JLabel("Name :");
      assetNameCB = new JComboBox();
      assetNameCB.addItem(ALL);
      Vector assetNames = graph.getValuesOfAttribute(PropertyNames.ASSET_NAME);
      for (int i=0; i<assetNames.size(); i++) 
        assetNameCB.addItem(assetNames.elementAt(i));
      assetNameCB.addActionListener(disableFindNext);

      int x = 0;
      int y = 0;
      assetPanel.add(assetAgentLabel,
                     new GridBagConstraints(x++, y, 1, 1, 1.0, 0.0,
                                            GridBagConstraints.WEST,
                                            GridBagConstraints.HORIZONTAL,
                                            new Insets(0, 0, 0, 0), 0, 0));
      assetPanel.add(assetAgentCB,
                     new GridBagConstraints(x++, y, 1, 1, 1.0, 0.0,
                                            GridBagConstraints.WEST,
                                            GridBagConstraints.HORIZONTAL,
                                            new Insets(0, 0, 0, 0), 0, 0));
      y = 1;
      x = 0;
      assetPanel.add(assetGroupNameLabel,
                     new GridBagConstraints(x++, y, 1, 1, 1.0, 0.0,
                                            GridBagConstraints.WEST,
                                            GridBagConstraints.HORIZONTAL,
                                            new Insets(0, 0, 0, 0), 0, 0));
      assetPanel.add(assetGroupNameCB,
                     new GridBagConstraints(x++, y, 1, 1, 1.0, 0.0,
                                            GridBagConstraints.WEST,
                                            GridBagConstraints.HORIZONTAL,
                                            new Insets(0, 0, 0, 0), 0, 0));
      y = 2;
      x = 0;
      assetPanel.add(assetNameLabel,
                     new GridBagConstraints(x++, y, 1, 1, 1.0, 0.0,
                                            GridBagConstraints.WEST,
                                            GridBagConstraints.HORIZONTAL,
                                            new Insets(0, 0, 0, 0), 0, 0));
      assetPanel.add(assetNameCB,
                     new GridBagConstraints(x++, y, 1, 1, 1.0, 0.0,
                                            GridBagConstraints.WEST,
                                            GridBagConstraints.HORIZONTAL,
                                            new Insets(0, 0, 0, 0), 0, 0));

      box.add(assetPanel);
      pack();
      if (!loadedPanels.isEmpty())
        loadedPanels.clear();
      loadedPanels.addElement(assetPanel);
    }else if (s.equals(PropertyNames.EVENT_HAPPINESS_CHANGE))  {
      if (!loadedPanels.isEmpty()) {
        for (int i=0; i<loadedPanels.size(); i++) {
          box.remove((JPanel)loadedPanels.elementAt(i));
          pack();
        }
      }
      happinessPanel = new JPanel();
      happinessPanel.setLayout(new GridBagLayout());
      TitledBorder happinessTitledBorder = new TitledBorder("Happiness");
      happinessPanel.setBorder(happinessTitledBorder);

      JLabel regardingLabel = new JLabel("Event Regarding :");
      Vector regards = graph.getValuesOfAttribute(PropertyNames.EVENT_REGARDING);
      regardingCB = new JComboBox();
      regardingCB.addItem(ALL);
      for (int i=0; i<regards.size(); i++) {
        regardingCB.addItem(regards.elementAt(i));
      }
      regardingCB.addActionListener(disableFindNext);
      JLabel publisherLabel = new JLabel("Publisher :");
      Vector publishers = graph.getValuesOfAttribute(PropertyNames.EVENT_PUBLISHER);
      publisherCB = new JComboBox();
      publisherCB.addItem(ALL);
      for (int i=0; i<publishers.size(); i++) {
        publisherCB.addItem(publishers.elementAt(i));
      }
      publisherCB.addActionListener(disableFindNext);
      JLabel sourceLabel = new JLabel("Source :");
      Vector sources = graph.getValuesOfAttribute(PropertyNames.EVENT_SOURCE);
      sourceCB = new JComboBox();
      sourceCB.addItem(ALL);
      for (int i=0; i<sources.size(); i++) {
        sourceCB.addItem(sources.elementAt(i));
      }
      sourceCB.addActionListener(disableFindNext);
      JLabel descriptionLabel = new JLabel("Description :");
      descriptionCB = new JComboBox();
      descriptionCB.addItem(ALL);
      Vector descriptions = graph.getValuesOfAttribute(PropertyNames.EVENT_DESCRIPTION);
      for (int i=0; i<descriptions.size(); i++)
        descriptionCB.addItem(descriptions.elementAt(i));
      descriptionCB.addActionListener(disableFindNext);
      int x = 0;
      int y = 0;

      y++;
      happinessPanel.add(regardingLabel,
                     new GridBagConstraints(x++, y, 1, 1, 1.0, 0.0,
                                            GridBagConstraints.WEST,
                                            GridBagConstraints.HORIZONTAL,
                                            new Insets(0, 0, 0, 0), 0, 0));
      happinessPanel.add(regardingCB,
                     new GridBagConstraints(x++, y, 1, 1, 1.0, 0.0,
                                            GridBagConstraints.WEST,
                                            GridBagConstraints.HORIZONTAL,
                                            new Insets(0, 0, 0, 0), 0, 0));
      y++;
      x = 0;
      happinessPanel.add(publisherLabel,
                     new GridBagConstraints(x++, y, 1, 1, 1.0, 0.0,
                                            GridBagConstraints.WEST,
                                            GridBagConstraints.HORIZONTAL,
                                            new Insets(0, 0, 0, 0), 0, 0));
      happinessPanel.add(publisherCB,
                     new GridBagConstraints(x++, y, 1, 1, 1.0, 0.0,
                                            GridBagConstraints.WEST,
                                            GridBagConstraints.HORIZONTAL,
                                            new Insets(0, 0, 0, 0), 0, 0));

      y++;
      x = 0;
      happinessPanel.add(sourceLabel,
                     new GridBagConstraints(x++, y, 1, 1, 1.0, 0.0,
                                            GridBagConstraints.WEST,
                                            GridBagConstraints.HORIZONTAL,
                                            new Insets(0, 0, 0, 0), 0, 0));
      happinessPanel.add(sourceCB,
                     new GridBagConstraints(x++, y, 1, 1, 1.0, 0.0,
                                            GridBagConstraints.WEST,
                                            GridBagConstraints.HORIZONTAL,
                                            new Insets(0, 0, 0, 0), 0, 0));
      y++;
      x = 0;
      happinessPanel.add(descriptionLabel,
                     new GridBagConstraints(x++, y, 1, 1, 1.0, 0.0,
                                            GridBagConstraints.WEST,
                                            GridBagConstraints.HORIZONTAL,
                                            new Insets(0, 0, 0, 0), 0, 0));
      happinessPanel.add(descriptionCB,
                     new GridBagConstraints(x++, y, 1, 1, 1.0, 0.0,
                                            GridBagConstraints.WEST,
                                            GridBagConstraints.HORIZONTAL,
                                            new Insets(0, 0, 0, 0), 0, 0));
      // Happiness range panel
      happinessRangePanel = new JPanel();
      happinessRangePanel.setLayout(new GridBagLayout());
      JRadioButton allRatingButton = new JRadioButton("All");
      allRatingFilter = true;
      allRatingButton.setFocusPainted(false);
      allRatingButton.setSelected(true);
      allRatingButton.addActionListener(disableFindNext);
      allRatingButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent evt) {
          allRatingFilter = true;
          ratingStartLabel.setEnabled(false);
          ratingStartField.setEnabled(false);
          ratingEndLabel.setEnabled(false);
          ratingEndField.setEnabled(false);
        }
      });
      JRadioButton specificRangeButton = new JRadioButton();
      specificRangeButton.setFocusPainted(false);
      specificRangeButton.addActionListener(disableFindNext);
      specificRangeButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent evt) {
          allRatingFilter = false;
          ratingStartLabel.setEnabled(true);
          ratingStartField.setEnabled(true);
          ratingEndLabel.setEnabled(true);
          ratingEndField.setEnabled(true);
        }
      });
      ButtonGroup rangeButtonGroup = new ButtonGroup();
      rangeButtonGroup.add(allRatingButton);
      rangeButtonGroup.add(specificRangeButton);
      ratingStartLabel = new JLabel("Rating Start :");
      ratingStartLabel.setEnabled(false);
      ratingStartField = new JTextField(10);
      ratingStartField.setEnabled(false);
      ratingEndLabel = new JLabel("Rating End :");
      ratingEndLabel.setEnabled(false);
      ratingEndField = new JTextField(10);
      ratingEndField.setEnabled(false);
      JRadioButton allDeltaButton = new JRadioButton("All");
      allDeltaFilter = true;
      allDeltaButton.setFocusPainted(false);
      allDeltaButton.setSelected(true);
      allDeltaButton.addActionListener(disableFindNext);
      allDeltaButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent evt) {
          allDeltaFilter = true;
          deltaStartLabel.setEnabled(false);
          deltaStartField.setEnabled(false);
          deltaEndLabel.setEnabled(false);
          deltaEndField.setEnabled(false);
        }
      });
      JRadioButton specificDeltaButton = new JRadioButton();
      specificDeltaButton.setFocusPainted(false);
      specificDeltaButton.addActionListener(disableFindNext);
      specificDeltaButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent evt) {
          allDeltaFilter = false;
          deltaStartLabel.setEnabled(true);
          deltaStartField.setEnabled(true);
          deltaEndLabel.setEnabled(true);
          deltaEndField.setEnabled(true);
        }
      });
      ButtonGroup deltaButtonGroup = new ButtonGroup();
      deltaButtonGroup.add(allDeltaButton);
      deltaButtonGroup.add(specificDeltaButton);
      deltaStartLabel = new JLabel("Delta Start :");
      deltaStartLabel.setEnabled(false);
      deltaStartField = new JTextField(10);
      deltaStartField.setEnabled(false);
      deltaEndLabel = new JLabel("Delta End :");
      deltaEndLabel.setEnabled(false);
      deltaEndField = new JTextField(10);
      deltaEndField.setEnabled(false);

      eventCumulStartLabel = new JLabel("Event Cumulative Start :");
      eventCumulStartLabel.setEnabled(false);
      eventCumulStartField = new JTextField(10);
      eventCumulEndLabel = new JLabel("Event Cumulative End :");
      eventCumulEndLabel.setEnabled(false);
      eventCumulEndField = new JTextField(10);
      JRadioButton allCumulButton = new JRadioButton("All");
      allCumulFilter = true;
      allCumulButton.setFocusPainted(false);
      allCumulButton.setSelected(true);
      allCumulButton.addActionListener(disableFindNext);
      allCumulButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent evt) {
          allCumulFilter = true;
          eventCumulStartLabel.setEnabled(false);
          eventCumulStartField.setEnabled(false);
          eventCumulEndLabel.setEnabled(false);
          eventCumulEndField.setEnabled(false);
        }
      });
      JRadioButton specificCumulButton = new JRadioButton();
      specificCumulButton.setFocusPainted(false);
      specificCumulButton.addActionListener(disableFindNext);
      specificCumulButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent evt) {
          allCumulFilter = false;
          eventCumulStartLabel.setEnabled(true);
          eventCumulStartField.setEnabled(true);
          eventCumulEndLabel.setEnabled(true);
          eventCumulEndField.setEnabled(true);
        }
      });
      ButtonGroup cumulButtonGroup = new ButtonGroup();
      cumulButtonGroup.add(allCumulButton);
      cumulButtonGroup.add(specificCumulButton);




      timeComplStartLabel = new JLabel("Time Completed Start :");
      timeComplStartField = new JTextField(10);
      timeComplStartLabel.setEnabled(false);
      timeComplStartField.setEnabled(false);
      timeComplEndLabel = new JLabel("Time Completed End :");
      timeComplEndField = new JTextField(10);
      timeComplEndLabel.setEnabled(false);
      timeComplEndField.setEnabled(false);
      JRadioButton allComplButton = new JRadioButton("All");
      allComplFilter = true;
      allComplButton.setFocusPainted(false);
      allComplButton.setSelected(true);
      allComplButton.addActionListener(disableFindNext);
      allComplButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent evt) {
          allComplFilter = true;
          timeComplStartLabel.setEnabled(false);
          timeComplStartField.setEnabled(false);
          timeComplEndLabel.setEnabled(false);
          timeComplEndField.setEnabled(false);
        }
      });
      JRadioButton specificComplButton = new JRadioButton();
      specificComplButton.setFocusPainted(false);
      specificComplButton.addActionListener(disableFindNext);
      specificComplButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent evt) {
          allComplFilter = false;
          timeComplStartLabel.setEnabled(true);
          timeComplStartField.setEnabled(true);
          timeComplEndLabel.setEnabled(true);
          timeComplEndField.setEnabled(true);
        }
      });
      ButtonGroup complButtonGroup = new ButtonGroup();
      complButtonGroup.add(allComplButton);
      complButtonGroup.add(specificComplButton);


      x=0;
      y=0;
      happinessRangePanel.add(allRatingButton,
                    new GridBagConstraints(x++, y, 1, 1, 1.0, 0.0,
                                           GridBagConstraints.WEST,
                                           GridBagConstraints.HORIZONTAL,
                                           new Insets(0, 0, 0, 0), 0, 0));
      happinessRangePanel.add(specificRangeButton,
                    new GridBagConstraints(x++, y, 1, 1, 1.0, 0.0,
                                           GridBagConstraints.WEST,
                                           GridBagConstraints.HORIZONTAL,
                                           new Insets(0, 0, 0, 0), 0, 0));
      happinessRangePanel.add(ratingStartLabel,
                     new GridBagConstraints(x++, y, 1, 1, 1.0, 0.0,
                                            GridBagConstraints.WEST,
                                            GridBagConstraints.HORIZONTAL,
                                            new Insets(0, 0, 0, 0), 0, 0));
      happinessRangePanel.add(ratingStartField,
                     new GridBagConstraints(x++, y, 1, 1, 1.0, 0.0,
                                            GridBagConstraints.WEST,
                                            GridBagConstraints.HORIZONTAL,
                                            new Insets(0, 0, 0, 0), 0, 0));
      happinessRangePanel.add(ratingEndLabel,
                     new GridBagConstraints(x++, y, 1, 1, 1.0, 0.0,
                                            GridBagConstraints.WEST,
                                            GridBagConstraints.HORIZONTAL,
                                            new Insets(0, 0, 0, 0), 0, 0));
      happinessRangePanel.add(ratingEndField,
                     new GridBagConstraints(x++, y, 1, 1, 1.0, 0.0,
                                            GridBagConstraints.WEST,
                                            GridBagConstraints.HORIZONTAL,
                                            new Insets(0, 0, 0, 0), 0, 0));
      x = 0;
      y++;
      happinessRangePanel.add(allDeltaButton,
                    new GridBagConstraints(x++, y, 1, 1, 1.0, 0.0,
                                           GridBagConstraints.WEST,
                                           GridBagConstraints.HORIZONTAL,
                                           new Insets(0, 0, 0, 0), 0, 0));
      happinessRangePanel.add(specificDeltaButton,
                    new GridBagConstraints(x++, y, 1, 1, 1.0, 0.0,
                                           GridBagConstraints.WEST,
                                           GridBagConstraints.HORIZONTAL,
                                           new Insets(0, 0, 0, 0), 0, 0));
      happinessRangePanel.add(deltaStartLabel,
                     new GridBagConstraints(x++, y, 1, 1, 1.0, 0.0,
                                            GridBagConstraints.WEST,
                                            GridBagConstraints.HORIZONTAL,
                                            new Insets(0, 0, 0, 0), 0, 0));
      happinessRangePanel.add(deltaStartField,
                     new GridBagConstraints(x++, y, 1, 1, 1.0, 0.0,
                                            GridBagConstraints.WEST,
                                            GridBagConstraints.HORIZONTAL,
                                            new Insets(0, 0, 0, 0), 0, 0));
      happinessRangePanel.add(deltaEndLabel,
                     new GridBagConstraints(x++, y, 1, 1, 1.0, 0.0,
                                            GridBagConstraints.WEST,
                                            GridBagConstraints.HORIZONTAL,
                                            new Insets(0, 0, 0, 0), 0, 0));
      happinessRangePanel.add(deltaEndField,
                     new GridBagConstraints(x++, y, 1, 1, 1.0, 0.0,
                                            GridBagConstraints.WEST,
                                            GridBagConstraints.HORIZONTAL,
                                            new Insets(0, 0, 0, 0), 0, 0));
      x = 0;
      y++;
      happinessRangePanel.add(allCumulButton,
                    new GridBagConstraints(x++, y, 1, 1, 1.0, 0.0,
                                           GridBagConstraints.WEST,
                                           GridBagConstraints.HORIZONTAL,
                                           new Insets(0, 0, 0, 0), 0, 0));
      happinessRangePanel.add(specificCumulButton,
                    new GridBagConstraints(x++, y, 1, 1, 1.0, 0.0,
                                           GridBagConstraints.WEST,
                                           GridBagConstraints.HORIZONTAL,
                                           new Insets(0, 0, 0, 0), 0, 0));
      happinessRangePanel.add(eventCumulStartLabel,
                     new GridBagConstraints(x++, y, 1, 1, 1.0, 0.0,
                                            GridBagConstraints.WEST,
                                            GridBagConstraints.HORIZONTAL,
                                            new Insets(0, 0, 0, 0), 0, 0));
      happinessRangePanel.add(eventCumulStartField,
                     new GridBagConstraints(x++, y, 1, 1, 1.0, 0.0,
                                            GridBagConstraints.WEST,
                                            GridBagConstraints.HORIZONTAL,
                                            new Insets(0, 0, 0, 0), 0, 0));
      happinessRangePanel.add(eventCumulEndLabel,
                     new GridBagConstraints(x++, y, 1, 1, 1.0, 0.0,
                                            GridBagConstraints.WEST,
                                            GridBagConstraints.HORIZONTAL,
                                            new Insets(0, 0, 0, 0), 0, 0));
      happinessRangePanel.add(eventCumulEndField,
                     new GridBagConstraints(x++, y, 1, 1, 1.0, 0.0,
                                            GridBagConstraints.WEST,
                                            GridBagConstraints.HORIZONTAL,
                                            new Insets(0, 0, 0, 0), 0, 0));
      x = 0;
      y++;
      happinessRangePanel.add(allComplButton,
                    new GridBagConstraints(x++, y, 1, 1, 1.0, 0.0,
                                           GridBagConstraints.WEST,
                                           GridBagConstraints.HORIZONTAL,
                                           new Insets(0, 0, 0, 0), 0, 0));
      happinessRangePanel.add(specificComplButton,
                    new GridBagConstraints(x++, y, 1, 1, 1.0, 0.0,
                                           GridBagConstraints.WEST,
                                           GridBagConstraints.HORIZONTAL,
                                           new Insets(0, 0, 0, 0), 0, 0));
      happinessRangePanel.add(timeComplStartLabel,
                     new GridBagConstraints(x++, y, 1, 1, 1.0, 0.0,
                                            GridBagConstraints.WEST,
                                            GridBagConstraints.HORIZONTAL,
                                            new Insets(0, 0, 0, 0), 0, 0));
      happinessRangePanel.add(timeComplStartField,
                     new GridBagConstraints(x++, y, 1, 1, 1.0, 0.0,
                                            GridBagConstraints.WEST,
                                            GridBagConstraints.HORIZONTAL,
                                            new Insets(0, 0, 0, 0), 0, 0));
      happinessRangePanel.add(timeComplEndLabel,
                     new GridBagConstraints(x++, y, 1, 1, 1.0, 0.0,
                                            GridBagConstraints.WEST,
                                            GridBagConstraints.HORIZONTAL,
                                            new Insets(0, 0, 0, 0), 0, 0));
      happinessRangePanel.add(timeComplEndField,
                     new GridBagConstraints(x++, y, 1, 1, 1.0, 0.0,
                                            GridBagConstraints.WEST,
                                            GridBagConstraints.HORIZONTAL,
                                            new Insets(0, 0, 0, 0), 0, 0));

      box.add(happinessPanel);
      box.add(happinessRangePanel);
      pack();
      if (!loadedPanels.isEmpty())
        loadedPanels.clear();
      loadedPanels.addElement(happinessPanel);
      loadedPanels.addElement(happinessRangePanel);
    }else if (planElements.contains(s)) {
      // handle plan elements
      if (!loadedPanels.isEmpty()) {
        for (int i=0; i<loadedPanels.size(); i++) {
          box.remove((JPanel)loadedPanels.elementAt(i));
          pack();
        }
      }
      // Status panel - all plan elements
      statusPanel = new JPanel();
      statusPanel.setLayout(new GridBagLayout());
      TitledBorder statusTitledBorder = new TitledBorder("Status");
      statusPanel.setBorder(statusTitledBorder);
      estimatedButton = new JRadioButton("Estimated");
      // default to estimated for time ranges
      estimatedButton.setSelected(true);
      estimatedButton.addActionListener(disableFindNext);

      reportedButton = new JRadioButton("Reported");
      reportedButton.addActionListener(disableFindNext);

      observedButton = new JRadioButton("Observed");
      observedButton.addActionListener(disableFindNext);

      statusButtonGroup = new ButtonGroup();
      statusButtonGroup.add(estimatedButton);
      statusButtonGroup.add(reportedButton);
      statusButtonGroup.add(observedButton);
      int x = 0;
      int y = 0;
      statusPanel.add(estimatedButton,
                    new GridBagConstraints(x++, y, 1, 1, 1.0, 0.0,
                                           GridBagConstraints.WEST,
                                           GridBagConstraints.HORIZONTAL,
                                           new Insets(0, 0, 0, 0), 0, 0));
      statusPanel.add(reportedButton,
                    new GridBagConstraints(x++, y, 1, 1, 1.0, 0.0,
                                           GridBagConstraints.WEST,
                                           GridBagConstraints.HORIZONTAL,
                                           new Insets(0, 0, 0, 0), 0, 0));
      statusPanel.add(observedButton,
                    new GridBagConstraints(x++, y, 1, 1, 1.0, 0.0,
                                           GridBagConstraints.WEST,
                                           GridBagConstraints.HORIZONTAL,
                                           new Insets(0, 0, 0, 0), 0, 0));
      box.add(statusPanel);
      loadedPanels.addElement(statusPanel);
      // Time panel - all plan elements
      timePanel = new JPanel();
      timePanel.setLayout(new GridBagLayout());
      TitledBorder timeTitledBorder = new TitledBorder("Time");
      timePanel.setBorder(timeTitledBorder);
      JRadioButton allTimeButton = new JRadioButton("All");
      allTimeFilter = true;
      allTimeButton.setFocusPainted(false);
      allTimeButton.setSelected(true);
      fromLabel = new JLabel("From: ");
      toLabel = new JLabel("To: ");
      allTimeButton.addActionListener(disableFindNext);
      allTimeButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent evt) {
          allTimeFilter = true;
          fromLabel.setEnabled(false);
          startTimeField.setEnabled(false);
          toLabel.setEnabled(false);
          endTimeField.setEnabled(false);
          estimatedButton.setSelected(false);
          if(log.isInfoEnabled()) {
            log.info("in all action listener");
          }
          reportedButton.setSelected(false);
          observedButton.setSelected(false);
        }
      });
      JRadioButton specificTimesButton = new JRadioButton();
      specificTimesButton.setFocusPainted(false);
      specificTimesButton.addActionListener(disableFindNext);
      specificTimesButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent evt) {
          allTimeFilter = false;
          fromLabel.setEnabled(true);
          startTimeField.setEnabled(true);
          toLabel.setEnabled(true);
          endTimeField.setEnabled(true);
        }
      });
      ButtonGroup timeButtonGroup = new ButtonGroup();
      timeButtonGroup.add(allTimeButton);
      timeButtonGroup.add(specificTimesButton);
      fromLabel.setEnabled(false); // initially set to "all time"
      startTimeField = new JTextField(10);
      startTimeField.setText("0");
      startTimeField.setEnabled(false);
      toLabel.setEnabled(false);
      endTimeField = new JTextField(10);
      endTimeField.setText("0");
      endTimeField.setEnabled(false);
      x = 0;
      y = 0;
      timePanel.add(allTimeButton,
                    new GridBagConstraints(x, y++, 1, 1, 1.0, 0.0,
                                           GridBagConstraints.WEST,
                                           GridBagConstraints.HORIZONTAL,
                                           new Insets(0, 0, 0, 0), 0, 0));
      timePanel.add(specificTimesButton,
                    new GridBagConstraints(x++, y, 1, 1, 1.0, 0.0,
                                           GridBagConstraints.WEST,
                                           GridBagConstraints.HORIZONTAL,
                                           new Insets(0, 0, 0, 0), 0, 0));
      timePanel.add(fromLabel,
                    new GridBagConstraints(x++, y, 1, 1, 1.0, 0.0,
                                           GridBagConstraints.WEST,
                                           GridBagConstraints.HORIZONTAL,
                                           new Insets(0, 0, 0, 0), 0, 0));
      timePanel.add(startTimeField,
                    new GridBagConstraints(x++, y, 1, 1, 1.0, 0.0,
                                           GridBagConstraints.WEST,
                                           GridBagConstraints.HORIZONTAL,
                                           new Insets(0, 0, 0, 0), 0, 0));
      timePanel.add(toLabel,
                    new GridBagConstraints(x++, y, 1, 1, 1.0, 0.0,
                                           GridBagConstraints.WEST,
                                           GridBagConstraints.HORIZONTAL,
                                           new Insets(0, 0, 0, 0), 0, 0));
      timePanel.add(endTimeField,
                    new GridBagConstraints(x++, y, 1, 1, 1.0, 0.0,
                                           GridBagConstraints.WEST,
                                           GridBagConstraints.HORIZONTAL,
                                           new Insets(0, 0, 0, 0), 0, 0));
      box.add(timePanel);
      loadedPanels.addElement(timePanel);
      // Success Panel - disposition only
      if (s.equals(PropertyNames.PLAN_ELEMENT_DISPOSITION)) {
        successPanel = new JPanel();
        successPanel.setLayout(new GridBagLayout());
        TitledBorder successTitledBorder = new TitledBorder("Success");
        successPanel.setBorder(successTitledBorder);
        successCBox = new JCheckBox("Success");
        successCBox.addActionListener(disableFindNext);
        x = 0;
        y = 0;
        successPanel.add(successCBox,
                      new GridBagConstraints(x, y, 1, 1, 1.0, 0.0,
                                             GridBagConstraints.WEST,
                                             GridBagConstraints.HORIZONTAL,
                                             new Insets(0, 0, 0, 0), 0, 0));
        box.add(successPanel);
        loadedPanels.addElement(successPanel);
        pack();
      }

      // AllocationPanel - allocation only
      if (s.equals(PropertyNames.PLAN_ELEMENT_ALLOCATION)) {
        allocationPanel = new JPanel();
        allocationPanel.setLayout(new GridBagLayout());
        TitledBorder allocTitledBorder = new TitledBorder("Allocation");
        allocationPanel.setBorder(allocTitledBorder);
        JLabel allocToAgentLabel = new JLabel("To Agent :");

        allocToAgentCB = new JComboBox();
        allocToAgentCB.addItem(ALL);
        Vector allocToAgents = graph.getValuesOfAttribute(PropertyNames.ALLOCATION_TO_AGENT);
        for (int i=0; i<allocToAgents.size(); i++) 
          allocToAgentCB.addItem(allocToAgents.elementAt(i));
        allocToAgentCB.addActionListener(disableFindNext);

        JLabel allocTaskUIDLabel = new JLabel("Task UID :");
        allocTaskUIDCB = new JComboBox();
        allocTaskUIDCB.addItem(ALL);
        Vector allocTaskUIDs = graph.getValuesOfAttribute(PropertyNames.ALLOCATION_TASK_UID);
        for (int i=0; i<allocTaskUIDs.size(); i++) 
          allocTaskUIDCB.addItem(allocTaskUIDs.elementAt(i));
        allocTaskUIDCB.addActionListener(disableFindNext);

        JLabel allocAssetUIDLabel = new JLabel("Asset UID :");
        allocAssetUIDCB = new JComboBox();
        allocAssetUIDCB.addItem(ALL);
        Vector allocAssetUIDs = graph.getValuesOfAttribute(PropertyNames.ALLOCATION_ASSET_UID);
        for (int i=0; i<allocAssetUIDs.size(); i++) 
          allocAssetUIDCB.addItem(allocAssetUIDs.elementAt(i));
        allocAssetUIDCB.addActionListener(disableFindNext);

        JLabel allocRemoteAgentUIDLabel = new JLabel("Remote Agent UID :");
        allocRemoteAgentUIDCB = new JComboBox();
        allocRemoteAgentUIDCB.addItem(ALL);
        Vector allocRemoteAgentUIDs = graph.getValuesOfAttribute(PropertyNames.ALLOCATION_REMOTE_AGENT_UID);
        for (int i=0; i<allocRemoteAgentUIDs.size(); i++) 
          allocRemoteAgentUIDCB.addItem(allocRemoteAgentUIDs.elementAt(i));
        allocRemoteAgentUIDCB.addActionListener(disableFindNext);

        JLabel allocLocalOrgUIDLabel = new JLabel("Local Org UID :");
        allocLocalOrgUIDCB = new JComboBox();
        allocLocalOrgUIDCB.addItem(ALL);
        Vector allocLocalOrgUIDs = graph.getValuesOfAttribute(PropertyNames.ALLOCATION_LOCAL_ORG_UID);
        for (int i=0; i<allocLocalOrgUIDs.size(); i++) 
          allocLocalOrgUIDCB.addItem(allocLocalOrgUIDs.elementAt(i));
        allocLocalOrgUIDCB.addActionListener(disableFindNext);
        x = 0;
        y = 0;
        allocationPanel.add(allocToAgentLabel,
                      new GridBagConstraints(x++, y, 1, 1, 1.0, 0.0,
                                             GridBagConstraints.WEST,
                                             GridBagConstraints.HORIZONTAL,
                                             new Insets(0, 0, 0, 0), 0, 0));
        allocationPanel.add(allocToAgentCB,
                      new GridBagConstraints(x++, y, 1, 1, 1.0, 0.0,
                                             GridBagConstraints.WEST,
                                             GridBagConstraints.HORIZONTAL,
                                             new Insets(0, 0, 0, 0), 0, 0));
        y++;
        x = 0;
        allocationPanel.add(allocTaskUIDLabel,
                      new GridBagConstraints(x++, y, 1, 1, 1.0, 0.0,
                                             GridBagConstraints.WEST,
                                             GridBagConstraints.HORIZONTAL,
                                             new Insets(0, 0, 0, 0), 0, 0));
        allocationPanel.add(allocTaskUIDCB,
                      new GridBagConstraints(x++, y, 1, 1, 1.0, 0.0,
                                             GridBagConstraints.WEST,
                                             GridBagConstraints.HORIZONTAL,
                                             new Insets(0, 0, 0, 0), 0, 0));
        y++;
        x = 0;
        allocationPanel.add(allocAssetUIDLabel,
                      new GridBagConstraints(x++, y, 1, 1, 1.0, 0.0,
                                             GridBagConstraints.WEST,
                                             GridBagConstraints.HORIZONTAL,
                                             new Insets(0, 0, 0, 0), 0, 0));
        allocationPanel.add(allocAssetUIDCB,
                      new GridBagConstraints(x++, y, 1, 1, 1.0, 0.0,
                                             GridBagConstraints.WEST,
                                             GridBagConstraints.HORIZONTAL,
                                             new Insets(0, 0, 0, 0), 0, 0));
        y++;
        x = 0;
        allocationPanel.add(allocRemoteAgentUIDLabel,
                      new GridBagConstraints(x++, y, 1, 1, 1.0, 0.0,
                                             GridBagConstraints.WEST,
                                             GridBagConstraints.HORIZONTAL,
                                             new Insets(0, 0, 0, 0), 0, 0));
        allocationPanel.add(allocRemoteAgentUIDCB,
                      new GridBagConstraints(x++, y, 1, 1, 1.0, 0.0,
                                             GridBagConstraints.WEST,
                                             GridBagConstraints.HORIZONTAL,
                                             new Insets(0, 0, 0, 0), 0, 0));
        y++;
        x = 0;
        allocationPanel.add(allocLocalOrgUIDLabel,
                      new GridBagConstraints(x++, y, 1, 1, 1.0, 0.0,
                                             GridBagConstraints.WEST,
                                             GridBagConstraints.HORIZONTAL,
                                             new Insets(0, 0, 0, 0), 0, 0));
        allocationPanel.add(allocLocalOrgUIDCB,
                      new GridBagConstraints(x++, y, 1, 1, 1.0, 0.0,
                                             GridBagConstraints.WEST,
                                             GridBagConstraints.HORIZONTAL,
                                             new Insets(0, 0, 0, 0), 0, 0));
        y++;
        x = 0;
        box.add(allocationPanel);
        loadedPanels.addElement(allocationPanel);
        pack();
      }
      if (s.equals(PropertyNames.PLAN_ELEMENT_ASSET_TRANSFER)) {
        // asset transfer panel - asset transfer only
        assetTransferPanel = new JPanel();
        assetTransferPanel.setLayout(new GridBagLayout());
        TitledBorder assetTransferTitledBorder = new TitledBorder("Asset Transfer");
        assetTransferPanel.setBorder(assetTransferTitledBorder);

        JLabel assetTransAssetUIDLabel = new JLabel("Asset UID :");
        assetTransUIDCB = new JComboBox();
        assetTransUIDCB.addItem(ALL);
        Vector assetTransUIDs = graph.getValuesOfAttribute(PropertyNames.ASSET_TRANSFER_ASSET_UID);
        for (int i=0; i<assetTransUIDs.size(); i++) 
          assetTransUIDCB.addItem(assetTransUIDs.elementAt(i));
        assetTransUIDCB.addActionListener(disableFindNext);

        JLabel assignUIDLabel = new JLabel("Assignee UID :");
        assignUIDCB = new JComboBox();
        assignUIDCB.addItem(ALL);
        Vector assignUIDs = graph.getValuesOfAttribute(PropertyNames.ASSET_TRANSFER_ASSIGNEE_UID);
        for (int i=0; i<assignUIDs.size(); i++) 
          assignUIDCB.addItem(assignUIDs.elementAt(i));
        assignUIDCB.addActionListener(disableFindNext);

        JLabel assignorLabel = new JLabel("Assignor :");
        assignorCB = new JComboBox();
        assignorCB.addItem(ALL);
        Vector assignors = graph.getValuesOfAttribute(PropertyNames.ASSET_TRANSFER_ASSIGNOR);
        for (int i=0; i<assignors.size(); i++) 
          assignorCB.addItem(assignors.elementAt(i));
        assignorCB.addActionListener(disableFindNext);


        x = 0;
        y = 0;
        assetTransferPanel.add(assetTransAssetUIDLabel,
                      new GridBagConstraints(x++, y, 1, 1, 1.0, 0.0,
                                             GridBagConstraints.WEST,
                                             GridBagConstraints.HORIZONTAL,
                                             new Insets(0, 0, 0, 0), 0, 0));
        assetTransferPanel.add(assetTransUIDCB,
                      new GridBagConstraints(x++, y, 1, 1, 1.0, 0.0,
                                             GridBagConstraints.WEST,
                                             GridBagConstraints.HORIZONTAL,
                                             new Insets(0, 0, 0, 0), 0, 0));
        y++;
        x = 0;
        assetTransferPanel.add(assignUIDLabel,
                      new GridBagConstraints(x++, y, 1, 1, 1.0, 0.0,
                                             GridBagConstraints.WEST,
                                             GridBagConstraints.HORIZONTAL,
                                             new Insets(0, 0, 0, 0), 0, 0));
        assetTransferPanel.add(assignUIDCB,
                      new GridBagConstraints(x++, y, 1, 1, 1.0, 0.0,
                                             GridBagConstraints.WEST,
                                             GridBagConstraints.HORIZONTAL,
                                             new Insets(0, 0, 0, 0), 0, 0));
        y++;
        x = 0;
        assetTransferPanel.add(assignorLabel,
                      new GridBagConstraints(x++, y, 1, 1, 1.0, 0.0,
                                             GridBagConstraints.WEST,
                                             GridBagConstraints.HORIZONTAL,
                                             new Insets(0, 0, 0, 0), 0, 0));
        assetTransferPanel.add(assignorCB,
                      new GridBagConstraints(x++, y, 1, 1, 1.0, 0.0,
                                             GridBagConstraints.WEST,
                                             GridBagConstraints.HORIZONTAL,
                                             new Insets(0, 0, 0, 0), 0, 0));
        box.add(assetTransferPanel);
        loadedPanels.addElement(assetTransferPanel);
        pack();
      }
    } else {
      if (!loadedPanels.isEmpty()) {
        for (int i=0; i<loadedPanels.size(); i++) {
          box.remove((JPanel)loadedPanels.elementAt(i));
          pack();
        }
      }
      pack();
    }
  }

  /**
   * User selected cancel button; hide dialog.
   */

  private void cancel() {
    setVisible(false);
  }

  /**
   * Display dialog.  
   */

  public void displayFinder() {
    setVisible(true);
  }

  /**
   * Determine what objects were selected from the dialog.
   * Returns vector of grappa Node.
   */

  private Vector getSelectedObjects() {
    status = null;
    String communitySelected = (String)communityCB.getSelectedItem();
    String agentSelected = (String)agentCB.getSelectedItem();
    String planObjectTypeSelected = (String)planObjectTypeCB.getSelectedItem();
 
    Vector selectedNodes = graph.vectorOfElements(GrappaConstants.NODE);
    if (!communitySelected.equals(ALL)) 
      selectedNodes = getNodesWithStringAttribute(selectedNodes,
                                                  PropertyNames.PLAN_OBJECT_COMMUNITY_NAME,
                                                  communitySelected);
    if (agentCB.getItemCount() > 0 ) {
      if (!agentSelected.equals(ALL)) 
        selectedNodes = getNodesWithStringAttribute(selectedNodes,
                                                    PropertyNames.AGENT_ATTR,
                                                    agentSelected);
      }
    // first select by object name
    String objectName = null;
    // if object selected is a plan element, then get plan objects first
    if (!planObjectTypeSelected.equals(ALL)) {
      if (planElements.contains(planObjectTypeSelected)) 
        objectName = PropertyNames.PLAN_ELEMENT_OBJECT;
      else
        objectName = planObjectTypeSelected;
      selectedNodes = getNodesWithStringAttribute(selectedNodes, 
                                                  PropertyNames.OBJECT_TYPE,
                                                  objectName);
      // get specific plan element objects
      if (planElements.contains(planObjectTypeSelected))
        selectedNodes = getNodesWithStringAttribute(selectedNodes,
                                                    PropertyNames.PLAN_ELEMENT_TYPE,
                                                    planObjectTypeSelected);

      // now refine the selected nodes based on what type of object we have
      // TASK
      if (planObjectTypeSelected.equals(PropertyNames.TASK_OBJECT)) {
        String verbSelected = (String)verbCB.getSelectedItem();
        if (!verbSelected.equals(ALL))
            selectedNodes = getNodesWithStringAttribute(selectedNodes,
                                                        PropertyNames.TASK_VERB,
                                                        verbSelected);
        String dirObjUIDSelected = (String)directObjUIDCB.getSelectedItem();
        if (!dirObjUIDSelected.equals(ALL))
          selectedNodes = getNodesWithStringAttribute(selectedNodes,
                                                      PropertyNames.TASK_DIRECT_OBJECT_UID,
                                                      dirObjUIDSelected);

        String prepPhraseSelected = (String)prepPhraseCB.getSelectedItem();
        if (!prepPhraseSelected.equals(ALL)) 
          selectedNodes = getNodesWithStringAttribute(selectedNodes,
                                                      PropertyNames.TASK_PREP_PHRASE,
                                                      prepPhraseSelected);
        if (!allTaskFilter) {
          String s = taskStartField.getText();
          taskStart = getDoubleValue(s,
                                   "Invalid start time: " + s + " using 0.",
                                   0.0);
          s = taskEndField.getText();
          taskEnd = getDoubleValue(s,
                                 "Invalid end time: " + s + " using max value.",
                                 Double.MAX_VALUE);
          selectedNodes = getNodesInDoubleRange(selectedNodes,
                                              PropertyNames.TASK_END_TIME,
                                              taskStart,
                                              taskEnd);
        }
      } else if (planObjectTypeSelected.equals(PropertyNames.EVENT_HAPPINESS_CHANGE)) {
        // HAPPINESS CHANGE EVENT
        if (!allRatingFilter) {
          String s = ratingStartField.getText();
          ratingStart = getFloatValue(s,
                                   "Invalid start time: " + s + " using 0.",
                                   0F);
          s = ratingEndField.getText();
          ratingEnd = getFloatValue(s,
                                 "Invalid end time: " + s + " using max value.",
                                 Float.MAX_VALUE);

          selectedNodes = getNodesInFloatRange(selectedNodes, 
                                              PropertyNames.EVENT_RATING,
                                              ratingStart,
                                              ratingEnd);      
        }  
        if (!allDeltaFilter) {
          String s = deltaStartField.getText();
          deltaStart = getFloatValue(s,
                                   "Invalid start time: " + s + " using 0.",
                                   0F);
          s = deltaEndField.getText();
          deltaEnd = getFloatValue(s,
                                 "Invalid end time: " + s + " using max value.",
                                 Float.MAX_VALUE);

          selectedNodes = getNodesInFloatRange(selectedNodes, 
                                              PropertyNames.EVENT_DELTA,
                                              deltaStart,
                                              deltaEnd);      
        }  
        String regardingSelected = (String)regardingCB.getSelectedItem();
        if (!regardingSelected.equals(ALL))
            selectedNodes = getNodesWithStringAttribute(selectedNodes,
                                                        PropertyNames.EVENT_REGARDING,
                                                        regardingSelected);
        String publisherSelected = (String)publisherCB.getSelectedItem();
        if (!publisherSelected.equals(ALL))
          selectedNodes = getNodesWithStringAttribute(selectedNodes,
                                                      PropertyNames.EVENT_PUBLISHER,
                                                      publisherSelected);

        String sourceSelected = (String)sourceCB.getSelectedItem();
        if (!sourceSelected.equals(ALL)) 
          selectedNodes = getNodesWithStringAttribute(selectedNodes,
                                                      PropertyNames.EVENT_SOURCE,
                                                      sourceSelected);
        String descriptionSelected = (String)descriptionCB.getSelectedItem();
        if (!descriptionSelected.equals(ALL)) 
          selectedNodes = getNodesWithStringAttribute(selectedNodes,
                                                      PropertyNames.EVENT_DESCRIPTION,
                                                      descriptionSelected);
        if (!allCumulFilter) {
          String s = eventCumulStartField.getText();
          eventCumulStart = getDoubleValue(s,
                                   "Invalid start time: " + s + " using 0.",
                                   0D);
          s = eventCumulEndField.getText();
          eventCumulEnd = getDoubleValue(s,
                                 "Invalid end time: " + s + " using max value.",
                                 Double.MAX_VALUE);

          selectedNodes = getNodesInDoubleRange(selectedNodes, 
                                              PropertyNames.EVENT_CUMULATIVE,
                                              eventCumulStart,
                                              eventCumulEnd);      
        }  
        if (!allComplFilter) {
          String s = timeComplStartField.getText();
          timeComplStart = getLongValue(s,
                                   "Invalid start time: " + s + " using 0.",
                                   0L);
          s = timeComplEndField.getText();
          timeComplEnd = getLongValue(s,
                                 "Invalid end time: " + s + " using max value.",
                                 Long.MAX_VALUE);

          selectedNodes = getNodesInLongRange(selectedNodes, 
                                              PropertyNames.EVENT_TIME_COMPLETED,
                                              timeComplStart,
                                              timeComplEnd);      
        }  
        
      } else if (planObjectTypeSelected.equals(PropertyNames.ASSET_OBJECT)) {
        // ASSET
        String assetAgentSelected = (String)assetAgentCB.getSelectedItem();
        if (!assetAgentSelected.equals(ALL))
          selectedNodes = getNodesWithStringAttribute(selectedNodes,
                                                      PropertyNames.ASSET_AGENT,
                                                      assetAgentSelected);

        String groupSelected = (String)assetGroupNameCB.getSelectedItem();
        if (!groupSelected.equals(ALL))
          selectedNodes = getNodesWithStringAttribute(selectedNodes,
                                                      PropertyNames.ASSET_GROUP_NAME,
                                                      groupSelected);
        String nameSelected = (String)assetNameCB.getSelectedItem();
        if (!nameSelected.equals(ALL))
          selectedNodes = getNodesWithStringAttribute(selectedNodes,
                                                      PropertyNames.ASSET_NAME,
                                                      nameSelected);

      } else if (planElements.contains(planObjectTypeSelected)) {
        // get info from time panels or any controls that are common to plan elements
        if (!allTimeFilter) {
          String s = startTimeField.getText();
          startTime = getDoubleValue(s,
                                   "Invalid start time: " + s + " using 0.",
                                   0D);
          s = endTimeField.getText();
          endTime = getDoubleValue(s,
                                 "Invalid end time: " + s + " using max value.",
                                 Double.MAX_VALUE);

          if (estimatedButton.isSelected()) {
            status = PropertyNames.ESTIMATED_ALLOCATION_RESULT_END_TIME;
          }
          if (reportedButton.isSelected()) {
            status = PropertyNames.REPORTED_ALLOCATION_RESULT_END_TIME;
          }
          if (observedButton.isSelected()) {
            status = PropertyNames.OBSERVED_ALLOCATION_RESULT_END_TIME;
          }
          selectedNodes = getNodesInDoubleRange(selectedNodes, 
                                              status,
                                              startTime,
                                              endTime);
        } 
        if (planObjectTypeSelected.equals(PropertyNames.PLAN_ELEMENT_DISPOSITION)) {
          if (successCBox.isSelected()) {
            selectedNodes = getNodesWithStringAttribute(selectedNodes,
                                                        PropertyNames.DISPOSITION_SUCCESS,
                                                        "true");
          }
        } else if (planObjectTypeSelected.equals(PropertyNames.PLAN_ELEMENT_ASSET_TRANSFER)) {
                  String assetTransUIDSelected = (String)assetTransUIDCB.getSelectedItem();
                  if (!assetTransUIDSelected.equals(ALL))
                    selectedNodes = getNodesWithStringAttribute(selectedNodes,
                                                                PropertyNames.ASSET_TRANSFER_ASSET_UID,
                                                                assetTransUIDSelected);
                  String assignUIDSelected = (String)assignUIDCB.getSelectedItem();
                  if (!assignUIDSelected.equals(ALL))
                    selectedNodes = getNodesWithStringAttribute(selectedNodes,
                                                                PropertyNames.ASSET_TRANSFER_ASSIGNEE_UID,
                                                                assignUIDSelected);

                  String assignorSelected = (String)assignorCB.getSelectedItem();
                  if (!assignorSelected.equals(ALL)) 
                    selectedNodes = getNodesWithStringAttribute(selectedNodes,
                                                                PropertyNames.ASSET_TRANSFER_ASSIGNOR,
                                                                assignorSelected);
        } else if (planObjectTypeSelected.equals(PropertyNames.PLAN_ELEMENT_ALLOCATION)) {
                  String allocAgentSelected = (String)allocToAgentCB.getSelectedItem();
                  if (!allocAgentSelected.equals(ALL))
                    selectedNodes = getNodesWithStringAttribute(selectedNodes,
                                                                PropertyNames.ALLOCATION_TO_AGENT,
                                                                allocAgentSelected);
                  String allocTaskUIDSelected = (String)allocTaskUIDCB.getSelectedItem();
                  if (!allocTaskUIDSelected.equals(ALL))
                    selectedNodes = getNodesWithStringAttribute(selectedNodes,
                                                                PropertyNames.ALLOCATION_TASK_UID,
                                                                allocTaskUIDSelected);

                  String allocAssetUIDSelected = (String)allocAssetUIDCB.getSelectedItem();
                  if (!allocAssetUIDSelected.equals(ALL)) 
                    selectedNodes = getNodesWithStringAttribute(selectedNodes,
                                                                PropertyNames.ALLOCATION_ASSET_UID,
                                                                allocAssetUIDSelected);
                  String allocRemoteAgentUIDSelected = (String)allocRemoteAgentUIDCB.getSelectedItem();
                  if (!allocRemoteAgentUIDSelected.equals(ALL))
                    selectedNodes = getNodesWithStringAttribute(selectedNodes,
                                                                PropertyNames.ALLOCATION_REMOTE_AGENT_UID,
                                                                allocRemoteAgentUIDSelected);

                  String allocLocalOrgUIDSelected = (String)allocLocalOrgUIDCB.getSelectedItem();
                  if (!allocLocalOrgUIDSelected.equals(ALL)) 
                    selectedNodes = getNodesWithStringAttribute(selectedNodes,
                                                                PropertyNames.ALLOCATION_LOCAL_ORG_UID,
                                                                allocLocalOrgUIDSelected);
        }
      }
    }
    return selectedNodes;
  }




  /**
   * Get value from a text field.
   */

  private float getFloatValue(String s, String errorMessage, 
			      float defaultValue) {
    float value = defaultValue;
    try {
      value = Float.parseFloat(s);
    } catch (NumberFormatException nfe) {
      JOptionPane.showMessageDialog(null, errorMessage);
    }
    return value;
  }

  private long getLongValue(String s, String errorMessage, long defaultValue) {
    long value = defaultValue;
    try {
      value = Long.parseLong(s);
    } catch (NumberFormatException nfe) {
      JOptionPane.showMessageDialog(null, errorMessage);
    }
    return value;
  }

  private double getDoubleValue(String s, String errorMessage, double defaultValue) {
    double value = defaultValue;
    try {
       value = Double.parseDouble(s);
    } catch (NumberFormatException nfe) {
      JOptionPane.showMessageDialog(null, errorMessage);
    }
    return value;
  }

  private Vector getNodesInFloatRange(Vector nodes,
				      String attributeName, 
				      float startTime, float endTime) {
    Vector selectedNodes = new Vector(nodes.size());
    float nodeValue;
    for (int i = 0; i < nodes.size(); i++) {
      Node node = (Node)nodes.elementAt(i);
      String s = (String)node.getAttributeValue(attributeName);
      if (s == null)
	continue;
      try {
	nodeValue = Float.parseFloat(s);
      } catch (NumberFormatException e) {
        if(log.isErrorEnabled()) {
          log.error("ULPlanFinder: ", e);
        }
	continue;
      }
      if (nodeValue >= startTime && nodeValue <= endTime)
	selectedNodes.addElement(node);
    }
    return selectedNodes;
  }

  private Vector getNodesInLongRange(Vector nodes,
				     String attributeName, 
				     long startTime, long endTime) {
    Vector selectedNodes = new Vector(nodes.size());
    long nodeValue;
    for (int i = 0; i < nodes.size(); i++) {
      Node node = (Node)nodes.elementAt(i);
      String s = (String)node.getAttributeValue(attributeName);
      if (s == null)
	continue;
      try {
	nodeValue = Long.parseLong(s);
      } catch (NumberFormatException e) {
        if(log.isErrorEnabled()) {
          log.error("ULPlanFinder: ", e);
        }
	continue;
      }
      if (nodeValue >= startTime && nodeValue <= endTime)
	selectedNodes.addElement(node);
    }
    return selectedNodes;
  }

  private Vector getNodesInDoubleRange(Vector nodes,
				     String attributeName, 
				     double startTime, double endTime) {
    Vector selectedNodes = new Vector(nodes.size());
    double nodeValue;
    for (int i = 0; i < nodes.size(); i++) {
      Node node = (Node)nodes.elementAt(i);
      String s = (String)node.getAttributeValue(attributeName);
      if (s == null)
	continue;
      try {
	nodeValue = Double.parseDouble(s);
      } catch (NumberFormatException e) {
        if(log.isErrorEnabled()) {
          log.error("ULPlanFinder: ", e);
        }
	continue;
      }
      if (nodeValue >= startTime && nodeValue <= endTime)
	selectedNodes.addElement(node);
    }
    return selectedNodes;
  }

  private Vector getNodesWithFloatAttribute(Vector nodes, 
					    String attributeName,
					    float value) {
    Vector selectedNodes = new Vector(nodes.size());
    float nodeValue;
    for (int i = 0; i < nodes.size(); i++) {
      Node node = (Node)nodes.elementAt(i);
      String s = (String)node.getAttributeValue(attributeName);
      if (s == null)
	continue;
      try {
	nodeValue = Float.parseFloat(s);
      } catch (NumberFormatException e) {
        if(log.isErrorEnabled()) {
          log.error("ULPlanFinder: ", e);
        }
	continue;
      }
      if (nodeValue == value)
	selectedNodes.addElement(node);
    }
    return selectedNodes;
  }

  private Vector getNodesWithLongAttribute(Vector nodes, 
					   String attributeName,
					   long value) {
    Vector selectedNodes = new Vector(nodes.size());
    long nodeValue;
    for (int i = 0; i < nodes.size(); i++) {
      Node node = (Node)nodes.elementAt(i);
      String s = (String)node.getAttributeValue(attributeName);
      if (s == null)
	continue;
      try {
	nodeValue = Long.parseLong(s);
      } catch (NumberFormatException e) {
        if(log.isErrorEnabled()) {
          log.error("ULPlanFinder: ", e);
        }
	continue;
      }
      if (nodeValue == value)
	selectedNodes.addElement(node);
    }
    return selectedNodes;
  }


  private Vector getNodesWithStringAttribute(Vector nodes,
					     String attributeName,
					     String attributeValue) {
    Vector selectedNodes = new Vector(nodes.size());
    for (int i = 0; i < nodes.size(); i++) {
      Node node = (Node)nodes.elementAt(i);
      Attribute a = node.getAttribute(attributeName);
      if (a != null) 
	if (a.getStringValue().equals(attributeValue))
	  selectedNodes.addElement(node);
    }
    return selectedNodes;
  }

  /**
   * Return subset of nodes that are in the specified community.
   */

  private Vector getNodesInChoiceCommunity(Vector nodes,
					   String commName) {
    Vector selectedNodes = new Vector(nodes.size());
    for (int i = 0; i < nodes.size(); i++) {
      Node node = (Node)nodes.elementAt(i);
      // If source is selected community, OK
      Attribute oComm = 
	node.getAttribute(PropertyNames.PLAN_OBJECT_COMMUNITY_NAME);
      if (oComm != null) 
	if (oComm.getStringValue().equals(commName))
	  selectedNodes.addElement(node);
    }
    return selectedNodes;
  }

  // Return subset of Nodes for which EITHER of the given
  // attributes has the value specified
  private Vector getNodesWithEitherAttribute(Vector nodes,
					     String attributeName,
					     String otherAttName,
					     String attributeValue) {
    Vector selectedNodes = new Vector(nodes.size());
    for (int i = 0; i < nodes.size(); i++) {
      Node node = (Node)nodes.elementAt(i);
      Attribute a = node.getAttribute(attributeName);
      if (a != null) 
	if (a.getStringValue().equals(attributeValue)) 
	  selectedNodes.addElement(node);
      a = node.getAttribute(otherAttName);
      if (a != null) {
	//
      if(log.isDebugEnabled()) {
        log.debug("NodesWEA looking for " + attributeValue + " and have " + a.getStringValue());
      }
	if (a.getStringValue().equals(attributeValue)) {
          if(log.isDebugEnabled()) {
           log.debug("NodesWEA found match for atValue " + attributeValue + " on attribute " + otherAttName);
          }
	  selectedNodes.addElement(node);
	}
      }
    }
    return selectedNodes;
  }

//    public static void main(String[] args) {
//      Hashtable communityToAgents = new Hashtable();
//      String[] communityNames = { "Community One", "Community Two", "Community Three" };
//      String[] agentNames = { "Customer", "Provider" };
//      for (int i = 0; i < communityNames.length; i++) {
//        String[] myAgents = new String[agentNames.length];
//        for (int j = 0; j < agentNames.length; j++) 
//  	myAgents[j] = agentNames[j] + i;
//        communityToAgents.put(communityNames[i], myAgents);
//      }
//      String[] planObjectTypes = { "Allocation", "Aggregation", "Disposition", "Expansion", "Asset", "Task", "Workflow" };
//      //mek
    
//      ULPlanFinder finder =
//        new ULPlanFinder(communityNames, communityToAgents, planObjectTypes);
//      finder.displayFinder();
//    }
  
  /**
   * Used to order selected nodes according to position.
   */

  class NodeComparator implements Comparator {

    /** Return negative, zero or positive as o1's position is less than,
     * equal to, or greater than o2, where position is considered
     * top-to-bottom and left-to-right.
     */

    public int compare(Object o1, Object o2) {
      Node n1 = (Node)o1;
      Node n2 = (Node)o2;
      GrappaPoint gp1 =
	(GrappaPoint)n1.getAttributeValue(GrappaConstants.POS_ATTR);
      GrappaPoint gp2 = 
	(GrappaPoint)n2.getAttributeValue(GrappaConstants.POS_ATTR);
      if (gp1 == null || gp2 == null)
	return 0; // treat them as equal
      double y1 = gp1.getY();
      double y2 = gp2.getY();
      if (y1 < y2)
	return -1;
      else if (y1 == y2) {
	double x1 = gp1.getX();
	double x2 = gp2.getX();
	if (x1 < x2)
	  return -1;
	else if (x1 > x2)
	  return 1;
	else 
	  return 0;
      } else 
	return 1;
    }

    public boolean equals(Object o) {
      return this.equals(o);
    }

  }

  private void readObject(ObjectInputStream ois)
    throws IOException, ClassNotFoundException
  {
    ois.defaultReadObject();
    createLogger();
  }

}
