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

package org.cougaar.tools.csmart.ui.configbuilder;

import org.cougaar.tools.csmart.core.property.ModifiableComponent;
import org.cougaar.tools.csmart.experiment.DBExperiment;
import org.cougaar.tools.csmart.experiment.Experiment;
import org.cougaar.tools.csmart.recipe.RecipeComponent;
import org.cougaar.tools.csmart.society.SocietyComponent;
import org.cougaar.tools.csmart.ui.Browser;
import org.cougaar.tools.csmart.ui.util.NamedFrame;
import org.cougaar.tools.csmart.ui.viewer.CSMART;
import org.cougaar.tools.csmart.ui.viewer.GUIUtils;
import org.cougaar.util.log.Logger;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.net.URL;

/**
 * User interface that supports building a configurable component.
 */
public class PropertyBuilder extends JFrame implements ActionListener {
  private PropertyEditorPanel propertyEditor;
  private ModifiableComponent configComponent;
  private static final String FILE_MENU = "File";
  private static final String EXIT_MENU_ITEM = "Exit";
  private static final String SAVE_DB_MENU_ITEM = "Save To Database";

  private static final String HELP_MENU = "Help";

  protected static final String HELP_DOC = "help.html";
  protected static final String ABOUT_CSMART_ITEM = "About CSMART";
  protected static final String ABOUT_DOC = "/org/cougaar/tools/csmart/ui/help/about-csmart.html";
  protected static final String HELP_MENU_ITEM = "Help";

  private String[] helpMenuItems = {
    HELP_MENU_ITEM, ABOUT_CSMART_ITEM
  };

  private JMenuItem saveMenuItem;
  private CSMART csmart;
  private transient Logger log;
  private Experiment experiment;
  private ModifiableComponent originalComponent;
  private boolean componentWasSaved = false;

  public PropertyBuilder(CSMART csmart, ModifiableComponent mc,
                         ModifiableComponent originalComponent,
                         Experiment experiment) {
    log = CSMART.createLogger(this.getClass().getName());
    this.csmart = csmart;
    this.originalComponent = originalComponent;
    this.experiment = experiment;

    // initialize menus and gui panels
    JMenuBar menuBar = new JMenuBar();
    getRootPane().setJMenuBar(menuBar);
    JMenu fileMenu = new JMenu(FILE_MENU);

    saveMenuItem = new JMenuItem(SAVE_DB_MENU_ITEM);
    saveMenuItem.addActionListener(this);
    fileMenu.add(saveMenuItem);
    fileMenu.addSeparator();

    JMenuItem exitMenuItem = new JMenuItem(EXIT_MENU_ITEM);
    exitMenuItem.addActionListener(this);
    fileMenu.add(exitMenuItem);

    menuBar.add(fileMenu);

    JMenu helpMenu = new JMenu(HELP_MENU);
    for (int i = 0; i < helpMenuItems.length; i++) {
      JMenuItem mItem = new JMenuItem(helpMenuItems[i]);
      mItem.addActionListener(this);
      helpMenu.add(mItem);
    }
    menuBar.add(helpMenu);

    setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
    addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent e) {
        if(!PropertyBuilder.this.getGlassPane().isVisible()) {
          exit();
        }
      }
    });

    //    setConfigComponent(mc);
    configComponent = mc;
    propertyEditor = new PropertyEditorPanel(configComponent, true);
    getContentPane().setLayout(new BorderLayout());
    getContentPane().add(propertyEditor, BorderLayout.CENTER);

    setSize(600,500);
    setVisible(true);
  }

  // if we modified a component in an experiment,
  // update the experiment and update the workspace view
  // and save the component in the database
  // if we modified a component not in the database,
  // save the component
  private void exit() {
    configComponent.setEditable(true);
    if (originalComponent != null)
      originalComponent.setEditable(true);
    propertyEditor.stopEditing(); // accept any edit in progress
    propertyEditor.exit();
    // if component is modified, or was modified and was saved
    // update it in the experiment
    if (isModified() || componentWasSaved) {
      if (experiment != null) {
        if (configComponent instanceof SocietyComponent) {
          SocietyComponent society = (SocietyComponent)configComponent;
          experiment.removeSocietyComponent();
          experiment.addSocietyComponent(society);
          CSMART.getOrganizer().replaceComponent(experiment, originalComponent, society);
        } else if (configComponent instanceof RecipeComponent) {
          RecipeComponent recipe = (RecipeComponent)configComponent;
          experiment.removeRecipeComponent((RecipeComponent)originalComponent);
          experiment.addRecipeComponent(recipe);
          CSMART.getOrganizer().replaceComponent(experiment, originalComponent, recipe);
        }
      }
    }
    if (isModified())
      saveToDatabase();
  }

  private boolean isModified() {
    return ((configComponent instanceof SocietyComponent &&
             ((SocietyComponent)configComponent).isModified()) ||
            (configComponent instanceof RecipeComponent &&
             ((RecipeComponent)configComponent).isModified()));
  }

  // user selected save from menu
  private void save() {
    if (!isModified()) {
      String[] msg = {
        "No modifications were made.",
        "Do you want to save anyway?"
      };
      int answer =
        JOptionPane.showConfirmDialog(this, msg,
                                      "No Modifications",
                                      JOptionPane.YES_NO_OPTION,
                                      JOptionPane.WARNING_MESSAGE);
      if (answer != JOptionPane.YES_OPTION) return;
    }
    componentWasSaved = true;
    saveToDatabase(); // force save
  }

  public void actionPerformed(ActionEvent e) {
    Object source = e.getSource();
    String s = ((AbstractButton)source).getActionCommand();
    if (s.equals(SAVE_DB_MENU_ITEM)) {
      save();
    } else if (s.equals(EXIT_MENU_ITEM)) {
      exit();
      // notify top-level viewer that user quit the builder
      NamedFrame.getNamedFrame().removeFrame(this);
      dispose();
    } else if (s.equals(HELP_MENU_ITEM)) {
      URL help = (URL)getClass().getResource(HELP_DOC);
      if (help != null)
	Browser.setPage(help);
    } else if (s.equals(ABOUT_CSMART_ITEM)) {
      URL about = (URL)getClass().getResource(ABOUT_DOC);
      if (about != null)
	Browser.setPage(about);
    }
  }

  /**
   * Save society and recipes to database.
   */

  private void saveToDatabase() {
    if (configComponent instanceof SocietyComponent) {
      final PropertyBuilder propertyBuilder = this;
      GUIUtils.timeConsumingTaskStart(csmart);
      GUIUtils.timeConsumingTaskStart(this);
      try {
        new Thread("SaveSociety") {
	    public void run() {
	      boolean success = ((SocietyComponent)configComponent).saveToDatabase();
	      GUIUtils.timeConsumingTaskEnd(csmart);
	      GUIUtils.timeConsumingTaskEnd(propertyBuilder);
	      if (!success && propertyBuilder.log.isWarnEnabled()) {
		propertyBuilder.log.warn("Failed to save society " + configComponent.getShortName());
	      } else if (propertyBuilder.log.isDebugEnabled()) {
		propertyBuilder.log.debug("Saved society " + configComponent.getShortName());
	      }
	    }
	  }.start();
      } catch (RuntimeException re) {
        if(log.isErrorEnabled()) {
          log.error("Runtime exception saving society", re);
        }
        GUIUtils.timeConsumingTaskEnd(csmart);
        GUIUtils.timeConsumingTaskEnd(propertyBuilder);
      }
    } else if (configComponent instanceof RecipeComponent) {
      ((RecipeComponent)configComponent).saveToDatabase();
    }
  }

//    public void reinit(ModifiableComponent newModifiableComponent) {
//      setConfigComponent(newModifiableComponent);
//      propertyEditor.reinit(configComponent);
//    }

//    private void setConfigComponent(ModifiableComponent newConfigComponent) {
//      configComponent = newConfigComponent;
//    }

}
