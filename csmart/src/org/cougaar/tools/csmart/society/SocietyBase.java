/**
 * <copyright>
 *  
 *  Copyright 2002-2004 BBNT Solutions, LLC
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
package org.cougaar.tools.csmart.society;

import org.cougaar.tools.csmart.core.cdata.ComponentData;
import org.cougaar.tools.csmart.core.db.PopulateDb;
import org.cougaar.tools.csmart.core.property.*;
import org.cougaar.tools.csmart.core.property.name.CompositeName;
import org.cougaar.tools.csmart.society.cdata.SocietyCDataComponent;
import org.cougaar.tools.csmart.ui.viewer.CSMART;
import org.cougaar.tools.csmart.ui.viewer.GUIUtils;
import org.cougaar.util.log.Logger;

import java.io.FileFilter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

/**
 * Implements generic methods required by all societies.
 *
 */
public abstract class SocietyBase 
  extends ModifiableConfigurableComponent
  implements SocietyComponent, PropertiesListener {

  protected static final String DESCRIPTION_RESOURCE_NAME = "/org/cougaar/tools/csmart/society/society-base-description.html";
  protected static final String BACKUP_DESCRIPTION =
    "A Society: Agents, Binders, Plugins, etc.";

  protected transient boolean isRunning = false;
  protected boolean isSelfTerminating = false;

  protected transient Logger log;

  protected String assemblyId;
  public String oldAssemblyId;
  protected boolean modified = true;

  // modification event
  public static final int SOCIETY_SAVED = 2;

  // used to block modify notifications while saving
  private transient boolean saveInProgress = false; 

  /**
   * Constructs a <code>SocietyBase</code> object
   * with the given name.
   * @param name Name for this component
   */
  public SocietyBase(String name){
    super(name);
    createLogger();
    installListeners();
  }

  private void createLogger() {
    log = CSMART.createLogger("org.cougaar.tools.csmart.society.SocietyBase");
  }

  /**
   * Returns the name of this Society
   *
   * @return Society Name
   */
  public String getSocietyName() {
    return getShortName();
  }

  public void setName(String newName) {
    if (newName == null || newName.equals("") || newName.equals(getSocietyName())) 
      return;

    boolean temp = modified;
    //    String oldname = getSocietyName();
    super.setName(newName);
    if (getAssemblyId() != null) {
      // do the DB save that is necessary
      try {
	PopulateDb.changeSocietyName(getAssemblyId(), newName);
      } catch (Exception e) {
	if (log.isErrorEnabled()) {
	  log.error("setName exception changing name from " + getSocietyName() + " to " + newName + " for assembly " + getAssemblyId(), e);
	}
	// On error, mark the society as modified
	temp = true;
      }
    } else {
      // Couldnt do the save ourselves, so mark the society as modified
      temp = true;
    }
    modified = temp;
  }

  /**
   * Returns the agents in this Society
   * @return an array of <code>AgentComponent</code> objects
   */
  public AgentComponent[] getAgents() {
    ArrayList agents = 
      new ArrayList(getDescendentsOfClass(AgentComponent.class));
    return (AgentComponent[])agents.toArray(new AgentComponent[agents.size()]);
  }

  /**
   * Get the assembly id for this Society.
   * @return a <code>String</code> which is the assembly id for this Society
   */
  public String getAssemblyId() {
    return this.assemblyId;
  }

  /**
   * Set by the experiment controller to indicate that the
   * society is running.
   * The society is running from the moment that any node
   * is successfully created 
   * (via the app-server's "create" method)
   * until all nodes are terminated (aborted, self terminated, or
   * manually terminated).
   * @param isRunning flag indicating whether or not the society is running
   */
   public void setRunning(boolean isRunning) {
    this.isRunning = isRunning;
  }

  /**
   * Returns whether or not the society is running, 
   * i.e. can be dynamically monitored. 
   * Running societies are not editable, but they can be copied,
   * and the copy can be edited. 
   * @return true if society is running and false otherwise
   */
  public boolean isRunning() {
    return isRunning;
  }

  /**
   * Return a file filter which can be used to fetch
   * the metrics files for this experiment. 
   * @return <code>FileFilter</code> to get metrics files for this experiment
   */
  public FileFilter getResultFileFilter() {
    return null;
  }

  /**
   * Return a file filter which can be used to delete
   * the files generated by this experiment.
   * @return <code>FileFilter</code> for cleanup
   */
  public FileFilter getCleanupFileFilter() {
    return null;
  }

  /**
   * Returns whether the society is self terminating or must
   * be manually terminated.
   * Self terminating nodes cause the app-server to send back
   * a "process-destroyed" message when the node terminates.
   * @return true if society is self terminating
   */
  public boolean isSelfTerminating() {
    return this.isSelfTerminating;
  }

  /**
   * Sets if the society is self terminating or not.
   * Self terminating nodes cause the app-server to send back
   * a "process-destroyed" message when the node terminates.
   *
   * @param isSelfTerminating true if society is self terminating
   */
  protected void setSelfTerminating(boolean isSelfTerminating) {
    this.isSelfTerminating = isSelfTerminating;
  }

  /**
   * Returns the description of this society
   *
   * @return an <code>URL</code> value
   */
  public URL getDescription() {
    return getClass().getResource(DESCRIPTION_RESOURCE_NAME);

  }

  /**
   * Modifies any part of the ComponentData Structure.
   *
   * @param data Completed ComponentData structure for the society
   * @return a <code>ComponentData</code> value
   */
  public ComponentData modifyComponentData(ComponentData data) {
    return data;
  }

  /**
   * Adds any relevent <code>ComponentData</code> for this component.
   * This method does not modify any existing <code>ComponentData</code>
   *
   * @see ComponentData
   * @param data Pointer to the global <code>ComponentData</code>
   * @return an updated <code>ComponentData</code> object
   */
  public ComponentData addComponentData(ComponentData data) {
    ComponentData[] children = data.getChildren();
    for(int i=0; i < children.length; i++) {
      ComponentData child = children[i];
      // for each child component data, if it's an agent's component data
      if (child.getType() == ComponentData.AGENT) {
        // get all my agent components
	Iterator iter = 
          ((Collection)getDescendentsOfClass(AgentComponent.class)).iterator();
	while(iter.hasNext()) {
	  AgentComponent agent = (AgentComponent)iter.next();
          // if the component data name matches the agent name
	  if (child.getName().equals(agent.getShortName().toString())) {
            // then set me as the owner of the component data
	    child.setOwner(this);
            // and add the component data
	    agent.addComponentData(child);
	    //	    child.resetModified();
	    // Dont do above cause it leaves the HNA open, but it
	    // would at least ensure that if other recipe snuck in earlier,
	    // this worked OK.
	  }
	}		
      } else {
	// FIXME!! Will we support other top-level components?
	// Process children of component data
	addComponentData(child);
      }      
    }

    // Reset the modified flag so nothing needed in Experiment.java,
    // and only recipe changes will appear in the tree
    // see comment in Experiment.save
    data.resetModified();
    // Note that this resets everything. So the HNA must be in the DB.
    // And all recipe addCDatas must come after the society addCData
    return data;
  }

  private void readObject(ObjectInputStream ois)
    throws IOException, ClassNotFoundException
  {
    ois.defaultReadObject();
    createLogger();
    installListeners();
    isRunning = false;
    saveInProgress = false;
  }

  /**
   * Save this society to the database. Only to be used
   * after creating a new society. Not to be used from DB societies
   * which are already in the database.
   *
   * @return a <code>boolean</code>, false on error
   */
  public boolean saveToDatabase() {
    saveInProgress = true;
    if(log.isInfoEnabled()) {
      log.info("saveToDatabase society (" + getSocietyName() + ") with asb: " + getAssemblyId() + " and old Assembly: " + oldAssemblyId);
    }

    // TODO:
    // Should I notice when I need to save and only save then?
    // Should I resist creating a new assembly ID, to avoid
    // breaking other experiments? Or always create one?
    
    String oldCMTAsbid = oldAssemblyId;
    String currAssID = getAssemblyId();
    String name = getSocietyName();

    if (currAssID != null && currAssID.startsWith("CMT")) {
      if (log.isDebugEnabled()) {
	log.debug("saveToDB not saving CMT society (" + getSocietyName() + ") with id " + currAssID + " and old ID " + oldCMTAsbid + " into same ID. Will create new one and new name -- like a copy, except done in place");
      }
      oldCMTAsbid = currAssID;
      currAssID = null;
      name = name + " edited";
    }

    // And what is my current assemblyID?
    // How do I know if it is different?
    // FIXME: Maybe I need a new
    
    // But probably only want to pass it in if it was in fact a CMT assembly, no?
    // Or does it hurt to pass it in?
    PopulateDb pdb = null;
    boolean ret = true;
    try {
      // FIXME: Is there a non-gui conflict handler I should use?
      pdb = new PopulateDb(oldCMTAsbid, name, currAssID, GUIUtils.createSaveToDbConflictHandler(null), false);
      pdb.populateCSA(SocietyComponentCreator.getComponentData(this));
      // Set the new CSA assembly ID on the society - get it from the PDB
      //      setAssemblyId(pdb.getCMTAssemblyId());
      assemblyId = pdb.getCMTAssemblyId();
      // What about fixAssemblies?
      // is it really populateCSA?
      pdb.close();
    } catch (Exception sqle) {
      if (log.isErrorEnabled()) {
	log.error("Error saving society to database: ", sqle);
      }
      ret = false;
    } finally {
      if (pdb != null) {
	try {
	  pdb.close();
	} catch (SQLException e) {}
      }
    }
    modified = false;
    saveInProgress = false;
    // tell listeners society is now saved
    fireModification(new ModificationEvent(this, SOCIETY_SAVED));
    return ret;
  }
  
  // Save the copy in the database before returning
  /**
   * Copy the given society, including the modified status.
   * The copy will store the original AssemblyId in the oldAssemblyId slot.
   *
   * @param name a <code>String</code> new society name
   * @return a <code>ModifiableComponent</code> society copy
   */
  public ModifiableComponent copy(String name) {
    if (log.isDebugEnabled()) {
      log.debug("Copying society " + this.getSocietyName() + " with assembly " + getAssemblyId() + " into new name " + name);
    }
    //    ModifiableComponent component = super.copy(name);
    ComponentData cdata = SocietyComponentCreator.getComponentData(this);
    cdata.setName(name);
    SocietyComponent component = new SocietyCDataComponent(cdata, null);
    component.initProperties();

    // Let the new society be marked as saved? That would force
    // a save on exit though...
    ((SocietyBase)component).modified = this.modified;

    // copy the assembly ID - the one under which this societies'
    // data is currently in the DB, but must be copied
    ((SocietyBase)component).oldAssemblyId = getAssemblyId();

    return component;
  }

  /**
   * Copy this Society and save the copy to the database, under
   * the given new name. If the save fails, the new society
   * will be marked modified when this method returns
   *
   * @param name a <code>String</code> new society name
   * @return a <code>ModifiableComponent</code> new society
   */
  public ModifiableComponent copyAndSave(String name) {
    ModifiableComponent component = copy(name);
    if (! ((SocietyBase)component).saveToDatabase()) {
      ((SocietyBase)component).modified = true;
      if (log.isWarnEnabled()) {
	log.warn("Error saving copy of society " + getSocietyName());
      }
    }
    return component;
  }

  /**
   * Has this society been modified, such that a save would do something.
   *
   * @return a <code>boolean</code>, false if no save necessary
   */
  public boolean isModified() {
    return modified;
  }

  public void fireModification() {
    modified = true;
    super.fireModification();
  }

  // only listen on local properties
  // modifications of properties of agents, plugins, binders, etc. 
  // should be propagated, such that this society knows it's been modified

  private void installListeners() {
    addPropertiesListener(this);
    for (Iterator i = getLocalPropertyNames(); i.hasNext(); ) {
      Property p = getProperty((CompositeName)i.next());
      p.addPropertyListener(myPropertyListener);
    }
  }

  public void propertyAdded(PropertyEvent e) {
    Property addedProperty = e.getProperty();
    Property myProperty = 
      getProperty(addedProperty.getName().last().toString());
    if (myProperty != null) {
      setPropertyVisible(addedProperty, true);
      addedProperty.addPropertyListener(myPropertyListener);
      fireModification();
    }
  }

  public void propertyRemoved(PropertyEvent e) {
    e.getProperty().removePropertyListener(myPropertyListener);
    fireModification();
  }

  PropertyListener myPropertyListener =
    new PropertyListener() {
      public void propertyValueChanged(PropertyEvent e) {
	if (e == null || e.getProperty() == null)
	  return;
	if (e.getProperty().getValue() == null) {
	  if (e.getPreviousValue() == null)
	    return;
	  else
	    fireModification();
	} else if (e.getPreviousValue() == null) {
	  fireModification();
	} else if (! e.getProperty().getValue().toString().trim().equals(e.getPreviousValue().toString()))
	  fireModification();
      }
      
      public void propertyOtherChanged(PropertyEvent e) {
	fireModification();
      }
    };
  
  ModificationListener myModificationListener = new MyModificationListener();

  public int addChild(ComposableComponent c) {
    ((ModifiableConfigurableComponent)c).addModificationListener(myModificationListener);
    fireModification();
    return super.addChild(c);
  }

  public void removeChild(ComposableComponent c) {
    ((ModifiableConfigurableComponent)c).removeModificationListener(myModificationListener);
    fireModification();
    super.removeChild(c);
  }


  class MyModificationListener implements ModificationListener, ConfigurableComponentListener {
    public void modified(ModificationEvent e) {
      // don't propagate modifications when we're saving
      if (!saveInProgress)
        fireModification();
    }
  }

}// SocietyBase
