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
package org.cougaar.tools.csmart.experiment;

import org.cougaar.tools.csmart.core.cdata.ComponentData;
import org.cougaar.tools.csmart.core.db.CMT;
import org.cougaar.tools.csmart.core.db.CommunityDbUtils;
import org.cougaar.tools.csmart.core.db.DBConflictHandler;
import org.cougaar.tools.csmart.core.db.DBUtils;
import org.cougaar.tools.csmart.core.db.ExperimentDB;
import org.cougaar.tools.csmart.core.db.PopulateDb;
import org.cougaar.tools.csmart.core.property.BaseComponent;
import org.cougaar.tools.csmart.core.property.ModifiableComponent;
import org.cougaar.tools.csmart.recipe.RecipeComponent;
import org.cougaar.tools.csmart.society.AgentComponent;
import org.cougaar.tools.csmart.society.SocietyComponent;
import org.cougaar.tools.csmart.ui.console.CSMARTConsoleModel;
import org.cougaar.tools.csmart.util.ReadOnlyProperties;
import org.cougaar.util.Parameters;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

/**
 * A CSMART Experiment. Holds the components being run, and the configuration of host/node/agents.<br>
 * @property org.cougaar.tools.csmart.allowComplexRecipeQueries
 *    (default false): If true, save changes after every recipe
 *    application, to permit recipe target queries to see the effect
 *    of the previous recipe.
 */
public final class DBExperiment extends ExperimentBase {

  // Define some Node Args
  private static final String CONFIG_DATABASE = "org.cougaar.configuration.database";
  private static final String CONFIG_USER = "org.cougaar.configuration.user";
  private static final String CONFIG_PASSWD = "org.cougaar.configuration.password";


  private static final String DBINIT_PROP = "DB"; // Use the CSMART database

  private Trial theTrial; // Experiments currently have a single trial



  ////////////////////////////////////////////
  // Constructors
  ////////////////////////////////////////////

  // Organizer uses this to create a new experiment based on a society/recipe
  // or from the UI
  public DBExperiment(final String name, final SocietyComponent societyComponent,
                    final RecipeComponent[] recipes) {
    this(name);
    init();
    setSocietyComponent(societyComponent);
    if (recipes != null)
      setRecipeComponents(recipes);

    // Give it a new empty Comm ASB:
    PopulateDb pdb = null;
    try {
      pdb = new PopulateDb(null, null);
      setCommAsbID(pdb.getNewCommAssembly());
    } catch (SQLException sqle) {
      log.error("constructor failed to get new comm ASB", sqle);
    } catch (IOException ie) {
      log.error("constructor failed to get new comm ASB", ie);
    } finally {
      try {
        if (pdb != null)
          pdb.close();
      } catch (SQLException se) {
      }
    }
  }

  // Used in copy when not in DB mode
  public DBExperiment(final String name) {
    super(name);
    init();
  }

  // Used in copy when in DB mode or on load from DB
  public DBExperiment(final String name, final String expID, final String trialID) {
    super(name, expID, trialID);
    init();
  }

  ////////////////////////////////////////////
  // Private Operations.
  ////////////////////////////////////////////

  private void init() {
    createLogger();
    setDefaultNodeArguments();
    if (getTrialID() != null)
      theTrial = new Trial(getTrialID());
    else
      theTrial = new Trial("Trial 1");
    theTrial.initProperties();
  }

  protected void setDefaultNodeArguments() {
    defaultNodeArguments =
        new ReadOnlyProperties(Collections.singleton(EXPERIMENT_ID));
    createObserver();
    defaultNodeArguments.put(PERSISTENCE_ENABLE, PERSISTENCE_DFLT);
    // By default we clear any existing persistence deltas when we
    // start a run.
    defaultNodeArguments.put(PERSIST_CLEAR, PERSIST_CLEAR_DFLT);
    defaultNodeArguments.put(TIMEZONE, TIMEZONE_DFLT);
    defaultNodeArguments.put(AGENT_STARTTIME, AGENT_STARTTIME_DFLT);
    defaultNodeArguments.put(COMPLAININGLP_LEVEL, COMPLAININGLP_LEVEL_DFLT);
    defaultNodeArguments.put(CONTROL_PORT, Integer.toString(APP_SERVER_DEFAULT_PORT));

    // By default, we tell the AppServer to ignore connection errors
    // if CSMART dies, so that the society does _not_ die.
    defaultNodeArguments.put(AS_SWALLOW_ERRORS, AS_SWALLOW_ERRORS_DFLT);

    // Class of Node to run. This is the first argument to the BOOTSTRAP_CLASS
    // below.
    defaultNodeArguments.put(CSMARTConsoleModel.COMMAND_ARGUMENTS, DEFAULT_NODE_CLASS);

    // Class of bootstrapper to use. The actual class being executed
    defaultNodeArguments.put(BOOTSTRAP_CLASS, DEFAULT_BOOTSTRAP_CLASS);

    if (DBUtils.dbMode) {
      defaultNodeArguments.put(CONFIG_DATABASE,
                               Parameters.findParameter(DBUtils.DATABASE));
      defaultNodeArguments.put(CONFIG_USER,
                               Parameters.findParameter(DBUtils.USER));
      defaultNodeArguments.put(CONFIG_PASSWD,
                               Parameters.findParameter(DBUtils.PASSWORD));
      // Initializer components from the CSMART database
      defaultNodeArguments.put(INITIALIZER_PROP, DBINIT_PROP);
      if (getTrialID() != null)
        defaultNodeArguments.setReadOnlyProperty(EXPERIMENT_ID, getTrialID());
    }
    try {
      defaultNodeArguments.put(ENV_DISPLAY,
                               InetAddress.getLocalHost().getHostName() + ":0.0");
    } catch (UnknownHostException uhe) {
      if (log.isErrorEnabled()) {
        log.error("UnknownHost Exception", uhe);
      }
    }
  }


  /**
   * Return the trial defined for this experiment.
   */
  private Trial getTrial() {
    return theTrial;
  }

  ////////////////////////////////////////////
  // Basic Operations.
  ////////////////////////////////////////////


  /**
   * Sets the Name of this Component.
   *
   * @param newName - New Component Name
   */
  public void setName(final String newName) {
    // If name is same, don't fire modification
    if (getExperimentName().equals(newName) || newName == null || newName.equals(""))
      return;

    if (log.isDebugEnabled()) {
      log.debug("Change expt name from " + getExperimentName() + " to " + newName);
    }
    super.setName(newName);

    // If the experiment isnt otherwise modified, and we can save
    // to the DB, then do so, and dont mark the experiment
    // as modified.
    if (!modified && getExperimentID() != null) {
      try {
        PopulateDb.changeExptName(getExperimentID(), newName);
      } catch (Exception e) {
        if (log.isErrorEnabled()) {
          log.error("setName: error setting name in DB", e);
        }
      } finally {
        return;
      }
    }

    fireModification();
  }


  /**
   * Return a deep copy of the experiment.
   * Called when an experiment is selected in the organizer.
   * Add experiments and society to workspace.
   * @return the copy of the experiment.
   */
  public ModifiableComponent copy(final String uniqueName) {
    if (log.isDebugEnabled()) {
      log.debug("Experiment copying " + getExperimentName() + " into new name " + uniqueName);
    }
    DBExperiment experimentCopy;
    if (DBUtils.dbMode)
      experimentCopy = new DBExperiment(uniqueName, getExperimentID(), getTrialID());
    else
      experimentCopy = new DBExperiment(uniqueName);

    // FIXME: Unset the experiment and trial IDs? I think so...
    experimentCopy.setExperimentID(null);
    experimentCopy.setTrialID(null);

    final Properties newProps = experimentCopy.getDefaultNodeArguments();
    newProps.clear();
    newProps.putAll(getDefaultNodeArguments());

//     // Copy the society component
//     ModifiableComponent society = getSocietyComponent();
//     if (society != null) {
//       ModifiableComponent copiedSocietyComponent = null;

//       // FIXME: USe a name based on the original society!!
//       //      copiedSocietyComponent = society.copy("Society for " + uniqueName);
//       copiedSocietyComponent = society.copy(society.getShortName() + " copy");
//       if (copiedSocietyComponent != null) {
//         // FIXME: Must save the copied society to the DB? I think the
// 	// society does it for me...
//         experimentCopy.addComponent(copiedSocietyComponent);
//       }
//     }

    // Don't copy the society.
    experimentCopy.addComponent(getSocietyComponent());

    // Copy the recipe components
    for (int i = 0; i < getRecipeComponentCount(); i++) {
      final ModifiableComponent mc = getRecipeComponent(i);
//       ModifiableComponent copiedComponent = null;
//       // FIXME: USe a name based on the original recipe!!
//       //      copiedComponent = mc.copy("Recipe for " + uniqueName);
//       copiedComponent = mc.copy(mc.getShortName() + " copy");
//       if (copiedComponent != null) {
//         experimentCopy.addComponent(copiedComponent);
//       }

      // Don't copy recipes either
      experimentCopy.addComponent(mc);
    }

    // copy hosts
    // Use special version of it that doesnt do the
    // Node/Agent reconciliation, since we dont
    // care for this purpose
    final HostComponent[] hosts = getHostComponentsNoReconcile();
    final HostComponent[] nhosts = new HostComponent[hosts.length];
    for (int i = 0; i < hosts.length; i++) {
      // FIXME: instead to nhosts[i] = hosts[i].copy();
      // and then don't need the following 2 set calls hopefully
      nhosts[i] = experimentCopy.addHost(hosts[i].getShortName().toString());
      nhosts[i].setServerPort(hosts[i].getServerPort());
      nhosts[i].setMonitoringPort(hosts[i].getMonitoringPort());
      //      hosts[i].copy(nhosts[i]);
      // FIXME: What about other Host fields? OS, etc?
      // -- answer - see above fixme
    }

    // copy nodes
    // Note that this will update the NameServer host, and do exactly
    // one Node/Agent reconciliation
    final NodeComponent[] nodes = getNodeComponents();
    final NodeComponent[] nnodes = new NodeComponent[nodes.length];
    for (int i = 0; i < nodes.length; i++) {
      nnodes[i] = ((ExperimentNode) nodes[i]).copy(experimentCopy);
      experimentCopy.addNodeComponent((ExperimentNode) nnodes[i]);

      // FIXME: What about other Node fields?
      // answer -- see comment in ExperimentNode.copy
    }

    // reconcile hosts-nodes-agents
    final AgentComponent[] nagents = experimentCopy.getAgents();
    NodeComponent nnode = null;
    final ArrayList nNodesCopied = new ArrayList();
    for (int i = 0; i < hosts.length; i++) {
      final NodeComponent[] onodes = hosts[i].getNodes();
      // for each of the old nodes, find the corresponding new node
      for (int j = 0; j < onodes.length; j++) {
        for (int k = 0; k < nnodes.length; k++) {
          if (nnodes[k].getShortName().equals(onodes[j].getShortName())) {
            nnode = nnodes[k];
            // add new node to new host
            nhosts[i].addNode(nnode);
            break;
          }
        }
        // add new agents to new node
        final AgentComponent[] oagents = onodes[j].getAgents();
        for (int x = 0; x < oagents.length; x++)
          for (int y = 0; y < nagents.length; y++)
            if (nagents[y].getShortName().equals(oagents[x].getShortName()))
              if(nagents[y] != null) {
                nnode.addAgent(nagents[y]);
              }

        // Record which Nodes we've (potentially) copied the Agents for
        nNodesCopied.add(nnode);
      } // loop over old old nodes on original Host
    } // loop over original hosts

    // FIXME: What about if orig Experiment had Agents assigned to a Node
    // that was not assigned to a host - is that mapping lost?
    // I think so. So we need to maybe keep track of which Nodes we updated just now
    for (int i = 0; i < nnodes.length; i++) {
      if (!nNodesCopied.contains(nnodes[i])) {
        // We may need to copy the old node-agent mapping onto this new node
        final AgentComponent[] oagents = nodes[i].getAgents();
// 	if (log.isDebugEnabled() && oagents.length > 0) {
// 	  log.debug("copy: Found node whose Agent mapping may not have been copied: " + nnodes[i] + " and old Node " + nodes[i]);
// 	}
        for (int j = 0; j < oagents.length; j++) {
// 	  if (log.isDebugEnabled()) {
// 	    log.debug("Looking at Agent " + oagents[j].getShortName() + " on old Node");
// 	  }
          for (int k = 0; k < nagents.length; k++) {
            if (nagents[k].getShortName().equals(oagents[j].getShortName())) {
              if (log.isDebugEnabled()) {
                log.debug("copy: Putting new Agent " + nagents[k].getShortName() + " on new Node " + nnodes[i].getShortName());
              }
              nnodes[i].addAgent(nagents[k]);
            } // Found Agent match
          } // loop over new agents
        } // loop over old agents on old Node
      } // block to deal with uncopied Node
    } // loop over new nodes
    // copy the results directory; results from the copied experiment
    // will be stored in this directory under the copied experiment name
    experimentCopy.setResultDirectory(getResultDirectory());

    // FIXME: Maybe copy now needs a Node/Agent reconciliation?
    //experimentCopy.getNodeComponents();

    // Fixme: Grab comm info from orig expt & resave under new asbID
    // and attach that new ID to this Expt
    PopulateDb pdb = null;
    String newAsbid = null;
    try {
      pdb = new PopulateDb(getExperimentID(), getTrialID());
      newAsbid = pdb.getNewCommAsbFromExpt(getExperimentID(), getTrialID());
    } catch (SQLException sqle) {
      log.error("Expt Copy error copying community info", sqle);
    } catch (IOException ioe) {
      log.error("Expt Copy error copying community info", ioe);
    } finally {
      try {
        if (pdb != null)
          pdb.close();
      } catch (SQLException se) {
      }
    }
    if (log.isDebugEnabled()) {
      log.debug("copy: new Expt gets Comm ASB " + newAsbid);
    }
    experimentCopy.setCommAsbID(newAsbid);

    // FIXME: Must duplicate the expt_trial_thread information
    // Else must recognize & fix problem on load in Organizer / CMTDialog

    // FIXME: Only do this sometimes maybe?
    experimentCopy.storeSelectedThreads(getSelectedThreads());

    return experimentCopy;
  }

  // Retrieve from the DB the threads selected in this experiment.
  // Do not store on the local experiment, but instead return a list
  // which will be stored on the copy
  private List getSelectedThreads() {
    if (getTrialID() == null || getSocietyComponent() == null)
      return null;
    final List threads = new ArrayList();

    for (int i = 0; i < CMT.ULDBThreads.length; i++) {
      if (ExperimentDB.isULThreadSelected(getTrialID(), CMT.ULDBThreads[i]))
        threads.add(CMT.ULDBThreads[i]);
    }
    if (threads.isEmpty())
      return null;
    else
      return threads;
  }

  // List of the CMT style threads selected for this Experiment
  // This should be non-null only when we copied the experiment
  // and have not yet saved it.
  private List selThreads = null;

  // Stash away the list of CMT style threads seleced in this experiment
  private void storeSelectedThreads(final List threads) {
    this.selThreads = threads;
  }

  // If there are any locally storead Threads, save them in DB
  // if we can.
  private void saveSelectedThreads() {
    if (selThreads == null || selThreads.isEmpty() || getExperimentID() == null || getExperimentID().equals("") || getTrialID() == null || getTrialID().equals(""))
      return;

    for (int i = 0; i < CMT.ULDBThreads.length; i++)
      ExperimentDB.setULThreadSelected(getTrialID(), CMT.ULDBThreads[i], selThreads.contains(CMT.ULDBThreads[i]));
    selThreads = null;
  }

  /**
   * Get component data for the society in the experiment.
   * If the experiment is not in the database, then it
   * constructs the society component data here.
   * @return ComponentData the component data for the society
   */
  public ComponentData getSocietyComponentData() {
    if (modified)
      saveToDb(); // if modified, update component data and save to database

    // If the exp/trialID is null and we have recipes, we must do a save
    // Otherwise recipes wont correctly add their data
    // do this within generateCompleteSociety

    if (completeSociety == null)
      generateCompleteSociety();

    return completeSociety;
  }

  // Private Methods

  ////////////////////////////////////////////
  // Host Component Operations.
  ////////////////////////////////////////////

  // Public Methods


  // Private Methods

  ////////////////////////////////////////////
  // Node Component Operations.
  ////////////////////////////////////////////

  // Public


  ////////////////////////////////////////////
  // Agent Component Operations.
  ////////////////////////////////////////////

  // Public

  // Private


  ////////////////////////////////////////////
  // Database Specific Operations
  ////////////////////////////////////////////

  /**
   * Save the experiment to the database, regardless of
   * whether or not it's been modified.
   * To avoid useless saves, the caller should check the modified
   * flag with isModified.  This method specifically does not check
   * the modified flag to allow the user to force saving the experiment.
   * If the experiment has been modified, save it to the database.
   * This creates the full ComponentData tree and then uses
   * <code>PopulateDb</code> to save it to the database
   *
   * @param ch a <code>DBConflictHandler</code> to graphically handle conflicts
   * @see org.cougaar.tools.csmart.core.db.PopulateDb
   */
  public void save(final DBConflictHandler ch) {
    PopulateDb pdb = null;

    // To debug slow saves, bug 2002.
    // Added per bug 2015
    final long saveStartTime = Calendar.getInstance().getTimeInMillis();
    if (log.isShoutEnabled())
      log.shout("save starting save");

    // Make sure we have the Community Assembly stored locally
    if (getCommAsbID() == null) {
      if (log.isDebugEnabled()) {
        log.debug("save had no CommAsb for expt " + getExperimentID());
      }
      try {
        // retrieve from db
        pdb = new PopulateDb(getExperimentID(), getTrialID());
        // set it locally
        setCommAsbID(pdb.getCommAsbForExpt(getExperimentID(), getTrialID()));
        if (getCommAsbID() == null) {
          if (log.isDebugEnabled()) {
            log.debug("save found no comm asb in DB either");
          }
          setCommAsbID(pdb.getNewCommAssembly());
        }
      } catch (SQLException sqle) {
        log.error("save error getting Comm assembly", sqle);
      } catch (IOException ioe) {
        log.error("save error getting Comm assembly", ioe);
      } finally {
        try {
          if (pdb != null)
            pdb.close();
        } catch (SQLException se) {
        }
        pdb = null;
      }
    }

    if (log.isDebugEnabled()) {
      log.debug("save has commAsb " + getCommAsbID() + " for expt " + getExperimentID());
    }

    try {
      if (log.isInfoEnabled()) {
        log.info("Saving experiment " + getExperimentName() + " to database");
      }

      // Don't do this here -- it gets done by generateHNACData
      //      updateNameServerHostName(); // Be sure this is up-to-date
      final List components = getComponents();

      boolean componentWasRemoved = false;

      final SocietyComponent sc = getSocietyComponent();
      if (sc == null) {
        if (log.isErrorEnabled())
          log.error("Experiment has no society; cannot save it to database.");
        return;
      }

      if (sc.getAssemblyId() == null) {
        // This should never happen!
        if (log.isErrorEnabled()) {
          log.error("Saving experiment " + getExperimentName() + " with exptID " + getExperimentID() +
                    " containing society " + sc.getSocietyName() +
                    " but society has now Assembly! Caller trace: ", new Throwable());
        }

        // Try to save the society that somehow
        // was not previously saved
        if (!sc.saveToDatabase()) {
          if (log.isErrorEnabled()) {
            log.error("Failed to save society " + sc.getSocietyName() + " so going to give up saving the experiment.");
          }
          return;
        }
      } else if (sc.isModified()) {
        // Save the society before trying to save the Experiment
        if (!sc.saveToDatabase()) {
          if (log.isErrorEnabled()) {
            log.error("Failed to save society " + sc.getSocietyName() + " so going to give up saving the experiment.");
          }
          return;
        }
      }

      // Check all recipe components to see if modified, if so, save them
      // Note that below the recipes are added to the experiment's
      // table
      // and this must happen after start the expt save
      final RecipeComponent[] rComponents = getRecipeComponents();
      for (int i = 0; i < rComponents.length; i++) {
        final RecipeComponent rc = rComponents[i];
        if (rc.isModified()) {
          rc.saveToDatabase();
        }
      }

      ///////////
      // OK: Now go ahead and save the experiment.
      pdb =
          new PopulateDb("CMT", "CSHNA", "CSMI", getExperimentName(),
                         getExperimentID(), getTrialID(), ch, sc.getAssemblyId());

      setExperimentID(pdb.getExperimentId());
      setTrialID(pdb.getTrialId()); // sets trial id and -D argument

      // Save back to DB the selected threads, if necc
      saveSelectedThreads();

      // store in the DB the community assembly ID being used
      pdb.setCommAssemblyId(getCommAsbID());

      // Some components will want access to the complete set of Nodes
      // in the society, etc. To get that, they must get back to the
      // root soc object, and do a getOwner and go from there. Ugly.


      // So far we've just created the pdb (which
      // sets up the Experimet & Trial IDs, and puts the CMT assembly
      // in the right place if necc, and clears out the other
      // assemblies (CSMI, CSHNA)

      // Gather the HostNodeAgent stuff
      // This will call updateNameServerHostName,
      // and take care of reconciling the Node/Agent mapping
      generateHNACDATA();
      if (log.isErrorEnabled() && completeSociety == null) {
        log.error("save: Society Data is null!");
        // FIXME: Throw an exception to pop out at this point?
      }

      // Problem: Societies from files store _tons_ of properties
      // on the Agents. This is most of the society definition, in fact.
      // It appears that these end up changing (somehow),
      // between saves. So this can throw that IllegalArgumentException
      // FIXME!!!!
      // Save what we have so far in the CSHNA assembly
      if (log.isDebugEnabled()) {
        log.debug("About to save HNA data");
      }
      pdb.populateHNA(completeSociety);

      // Now let components add in their pieces
      componentWasRemoved = askComponentsToAddCDATA();

      // Recipes do their thing in modifyComponentData
      // Assuming that is the case (big assumption),
      // we clear the modified flag here, so that _only_ the
      // recipe deltas will show up as modifications
      // when we go to save the CSMI assembly later
      // This breaks AgentInsertionRecipes and CompleteAgentRecipes,
      // and perhaps all Complex recipes!!
      // So instead, add resetModified() call to SocietyBase.addComponentData...
      //completeSociety.resetModified();


      // If any component removed something, we need
      // to recreate the CMT assembly (under the original ID)
      // So this, currently destroys the orig CMT
      // Note also it does not effect Agent asset data
      // or relationships or OPLAN stuff
      if (componentWasRemoved) {
        if (log.isDebugEnabled()) {
          log.debug("save: After adding CDATA, got a removed.");
        }
        pdb.populateCSA(completeSociety);
      } else {
        if (log.isDebugEnabled()) {
          log.debug("save: Done adding CDATA, ready to modify.");
        }
      }


      // Are we allowing complex Recipe target queries that depend on the previous
      // actions of earlier recipes on Plugins, Binders, etc?
      final boolean doComplex = (System.getProperty("org.cougaar.tools.csmart.allowComplexRecipeQueries", "false").equalsIgnoreCase("true"));

      if (!doComplex) {
        // Should this be turned down to INFO level? Or perhaps only after people have gotten used
        // to it?
        if (log.isWarnEnabled())
          log.warn("save: Not allowing complex recipe target queries. Some queries using Agent contents or relationships may not work. Queries based on Agent, Node, or Host names and assignments, and Community information will work. If your recipe fails to apply correctly, try un-commenting the appropriate line in the CSMART startup script.");
      }

      // then give everyone a chance to modify what they've collectively produced
      for (int i = 0, n = components.size(); i < n; i++) {
        final BaseComponent soc = (BaseComponent) components.get(i);

        if (log.isDebugEnabled())
          log.debug("save: About to apply mods by " + soc.getShortName());
        soc.modifyComponentData(completeSociety, pdb);

        if (soc.componentWasRemoved()) {
          if (log.isDebugEnabled()) {
            log.debug("save: After modifying CDATA by comp " + soc.getShortName() + ", got a removed.");
          }
          pdb.populateCSA(completeSociety);
        }

        // If we are allowing complex recipe target queries, then
        // we must save each recipes changes to the DB in turn, so that
        // later recipes can look at these changes to decide what to do.
        // If not though, and we're only looking at HNA data and perhaps
        // CMT data, plus COMM data, then these extra saves are not necessary
        if (doComplex) {
          try {
            // Incrementally save
            // so that later recipes have the benefit
            // of the earlier modifications
            if (log.isDebugEnabled()) {
              log.debug("About to save CSMI data, having just done mod in " + soc.getShortName());
            }
            pdb.populateCSMI(completeSociety);
          } catch (IllegalArgumentException iae) {
            if (log.isInfoEnabled()) {
              log.info("Caught iae " + iae.toString() + " while saving modifications. Must redo as a CSA");
            }
            pdb.populateCSA(completeSociety);
          }
        }
      } // end of loop over components to do mod

      // If we did not iteratively save above, must do a save here
      if (!doComplex) {
        if (log.isInfoEnabled())
          log.info("save: Now saving all modifications at once.");
        try {
          // Incrementally save
          // so that later recipes have the benefit
          // of the earlier modifications
          if (log.isDebugEnabled()) {
            log.debug("About to save CSMI data, having done all mods");
          }
          pdb.populateCSMI(completeSociety);
        } catch (IllegalArgumentException iae) {
          if (log.isInfoEnabled()) {
            log.info("Caught iae " + iae.toString() + " while saving modifications. Must redo as a CSA");
          }
          pdb.populateCSA(completeSociety);
        }
      }

      // Save the inclusion of these recipes in the experiment
      // without re-saving the recipes
      pdb.setModRecipes(recipes);

      // Now make sure the runtime/ config time assemblies
      // are all correct
      if (log.isDebugEnabled()) {
        log.debug("About to do fix assemblies");
      }
      if (!pdb.fixAssemblies()) {
        // Some sort of error trying to ensure the assemblies are all correct.
        if (log.isErrorEnabled()) {
          log.error("Failed to ensure correct assemblies saved.");
        }
      }

      resetModified();
    } catch (Exception sqle) {
      if (log.isErrorEnabled())
        log.error("Error saving experiment to database: ", sqle);
      return;
    } finally {
      final long saveStopTime = Calendar.getInstance().getTimeInMillis();
      // To debug slow saves, bug 2002.
      // Added per bug 2015
      if (log.isShoutEnabled())
        log.shout("save done. Experiment Save Time in Seconds: " + (saveStopTime - saveStartTime) / 1000l);

      if (pdb != null) {
        try {
          pdb.close();
        } catch (SQLException e) {
        }
      }
    }
  }

  private void saveToDb() {
    save(new DBConflictHandler() {
      public int handleConflict(final Object msg, final Object[] choices,
                                final Object defaultChoice) {
        return JOptionPane.showOptionDialog(null,
                                            msg,
                                            "Database Conflict",
                                            JOptionPane.WARNING_MESSAGE,
                                            JOptionPane.DEFAULT_OPTION,
                                            null,
                                            choices,
                                            defaultChoice);
      }
    });
  }

  /**
   * Generate a complete ComponentData tree for the experiment
   *
   * @return a <code>boolean</code>, true if any component was removed
   */
  public boolean generateCompleteSociety() {
    // If the exp/trialID is null and we have recipes, we must do a save
    // Otherwise the pdb recipes get when doing modifyComponentData
    // wont let them do the DB queries they want to do
    if ((getTrialID() == null || getExperimentID() == null) && getRecipeComponentCount() > 0)
      saveToDb();

    if (!modified && completeSociety != null)
      return false;
    generateHNACDATA();

//     if (log.isDebugEnabled())
//       log.debug("generatecompleteSoc: after genHNA have: " + completeSociety);

    boolean mods = askComponentsToAddCDATA();

//     if (log.isDebugEnabled())
//       log.debug("generatecompleteSoc: after askToAdd have: " + completeSociety);

    return mods |= allowModifyCData();
  }

  private boolean allowModifyCData() {
    // then give everyone a chance to modify what they've collectively produced
    boolean componentModified = false;
    final BaseComponent theSoc = getSocietyComponent();
    PopulateDb pdb = null;
    try {
      pdb = new PopulateDb(getExperimentID(), getTrialID());
      if (log.isDebugEnabled()) {
        log.debug("allowModify letting comps modify. comp: " + theSoc.getShortName());
      }
      // Recipes typically need to do DB queries
      // in order to do these insertions correctly,
      // so need the version of modify that takes a pdb called.
      theSoc.modifyComponentData(completeSociety, pdb);
      componentModified |= theSoc.componentWasRemoved();

      for (int i = 0, n = recipes.size(); i < n; i++) {
        final BaseComponent soc = (BaseComponent) recipes.get(i);
        if (log.isDebugEnabled()) {
          log.debug("allowModify letting comps modify. comp: " + soc.getShortName());
        }
        // Recipes typically need to do DB queries
        // in order to do these insertions correctly,
        // so need the version of modify that takes a pdb called.
        soc.modifyComponentData(completeSociety, pdb);
        componentModified |= soc.componentWasRemoved();
      }
    } catch (Exception e) {
      if (log.isErrorEnabled()) {
        log.error("allowModifyCData error with pdb", e);
      }
    } finally {
      if (pdb != null) {
        try {
          pdb.close();
        } catch (SQLException e) {
        }
      }
    }

//     if (log.isDebugEnabled())
//       log.debug("end of allowModify: complete is now: " + completeSociety);

    return componentModified;
  }

  ////////////////////////////////////////////
  // Configuration Writer Specific Operations
  ////////////////////////////////////////////

  /**
   * Dump out the INI files for the first trial to
   * the local results directory for that trial.
   * This saves the experiment to the database.
   * The caller is responsible for not unnecessarily
   * saving the experiment to the database.
   */
  public void dumpINIFiles() {
    // FIXME:
    // If this Exp's society's assembly has entries in the oplan
    // tables, then must save out what the assembly ID is such
    // that on load of the INI files we can preserve the OPLAN INFO

    // Generate the complete Society
    generateCompleteSociety();

    final ExperimentINIWriter cw = new ExperimentINIWriter(completeSociety);
    cw.setTrialID(getTrialID());

    File resultDir = getResultDirectory();
    // if user didn't specify results directory, save in local directory
    if (resultDir == null) {
      resultDir = new File(".");
    }
    final Trial trial = getTrial();
    final String dirname = resultDir.getAbsolutePath() + File.separatorChar +
        getExperimentName() + File.separatorChar +
        trial.getShortName() + File.separatorChar +
        "INIFiles-dump";
    File f = null;
    try {
      f = new File(dirname);
      // guarantee that directories exist
      if (!f.exists() && !f.mkdirs() && !f.exists())
        f = new File(".");
    } catch (Exception e) {
      JOptionPane.showMessageDialog(null, "Couldn't create results directory: " + e, "Can't create directory", JOptionPane.ERROR_MESSAGE);
      if (log.isErrorEnabled()) {
        log.error("Couldn't create results directory: ", e);
      }
    }
    try {
      JOptionPane.showMessageDialog(null, "Writing ini file to " + f.getAbsolutePath() + "...");
      cw.writeConfigFiles(f);
      // Also, write out the community XML file
      if (!CommunityDbUtils.dumpCommunityXML(f.getAbsolutePath() + File.separatorChar + "communities.xml", getCommAsbID()))
        log.error("Couldn't write communities.xml file");
      else if (log.isDebugEnabled())
        log.debug("got true from commDbUtils.dump with path " + f.getAbsolutePath() + File.separatorChar + "communities.xml, and asbID " + getCommAsbID());

    } catch (Exception e) {
      if (log.isErrorEnabled()) {
        log.error("Couldn't write ini files: ", e);
      }
    }
  }

  public Iterator getConfigFiles(final NodeComponent[] nodes) {
    if (DBUtils.dbMode) {
      // Send a config writer that only writes LeafComponentData
      try {
        createConfigWriter();
        return configWriter.getFileNames();
      } catch (Exception e) {
        if (log.isErrorEnabled()) {
          log.error("Exception, creating config writer", e);
        }
        return null;
      }
    } else {
      //      return new ExperimentINIWriter(getComponents(), nodes, this);
      return null;
    }
  }

  // Private Methods

  /**
   * Dumps a Host / Node / Agent mapping to an XML file.
   * The stored file can then be imported at a later date
   * to re-create all mappings in the current experiment.
   *
   */
  public void dumpHNA() {
    generateCompleteSociety();

    final ExperimentXML xmlWriter = new ExperimentXML();

    File resultDir = getResultDirectory();
    // if user didn't specify results directory, save in defult results directory
    // FIXME: This should use the CSMART global setting
    // CSMART.getResultDir() would do it if the experiment had access
    // to this...
    if (resultDir == null) {
      String resultDirName;
      try {
        resultDirName = System.getProperty("org.cougaar.install.path");
      } catch (RuntimeException e) {
        // just use current directory
        resultDirName = ".";
      }
      resultDir = new File(resultDirName + File.separatorChar + "results");
    }

    final Trial trial = getTrial();
    final String dirname = resultDir.getAbsolutePath() + File.separatorChar +
        getExperimentName() + File.separatorChar +
        trial.getShortName() + File.separatorChar +
        "HNA-XML";
    File f = null;
    try {
      f = new File(dirname);
      // guarantee that directories exist
      if (!f.exists() && !f.mkdirs() && !f.exists())
        f = new File(".");
    } catch (Exception e) {
      JOptionPane.showMessageDialog(null, "Couldn't create results directory: " + e, "Can't create directory", JOptionPane.ERROR_MESSAGE);
      if (log.isErrorEnabled()) {
        log.error("Couldn't create results directory: ", e);
      }
    }
    if (f != null) {
      try {
        JOptionPane.showMessageDialog(null, "Writing xml file to " + f.getAbsolutePath() + "...");

        xmlWriter.createExperimentFile(completeSociety, f);
      } catch (Exception e) {
        if (log.isErrorEnabled()) {
          log.error("dumpHNA: Couldn't write XML file: ", e);
        }
      }
    }
  }



  /**
   * If the experiment has still unbound properties,
   * then we can't run it yet. <br>
   * For each property in the experiment, if it is not set,
   * and we don't have a set of experimental values, return true;
   *
   * @return a <code>boolean</code> value
   */
  public boolean hasUnboundProperties() {
    // Dont use this!!!!
    return false;
  }
} // end of Experiment.java






