/*
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
package org.cougaar.tools.csmart.ui.viewer;

import org.cougaar.tools.csmart.core.db.ExperimentDB;
import org.cougaar.tools.csmart.core.property.ConfigurableComponent;
import org.cougaar.tools.csmart.experiment.DBExperiment;
import org.cougaar.tools.csmart.experiment.Experiment;
import org.cougaar.tools.csmart.recipe.RecipeComponent;
import org.cougaar.tools.csmart.util.XMLUtils;
import org.cougaar.util.log.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.util.Enumeration;
import java.util.Map;

/**
 * Write out to XML enough content to recreate a users workspace. Also used to read it in,
 * loading from the DB the named experiments and recipes. Note that societies are not captured.
 **/
public class OrganizerXML extends XMLUtils {
  public static final String WORKSPACE_NODE = "Workspace";
  public static final String FOLDER_NODE = "Folder";
  public static final String EXPERIMENT_NODE = "Experiment";
  public static final String RECIPE_NODE = "Recipe";
  public static final String RESULTDIR_NODE = "ResultDir";
  public static final String NAME_ATTR = "Name";
  public static final String ID_ATTR = "ID";

  private Logger log;

  private Document doc;
  private Organizer organizer;

  public OrganizerXML (){
    log = CSMART.createLogger("org.cougaar.tools.csmart.ui.viewer.OrganizerXML");
  }


  /**
   * Given the root node of the workspace and the global result directory name,
   * generate an XML document for the workspace.
   *
   * @param rootNode a <code>DefaultMutableTreeNode</code> root of the workspace organizer
   * @param resultDirName a <code>String</code> global result directory name
   * @return a <code>Document</code> capturing the necc info
   */
  public Document createXMLDocument(DefaultMutableTreeNode rootNode, String resultDirName) {
    if(rootNode == null) return null;

    Document doc = null;

    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
    try {
      DocumentBuilder db = dbf.newDocumentBuilder();

      doc = db.newDocument();

      Element root = doc.createElement(WORKSPACE_NODE);
      // this should be same as the name of the file, so leave it out
      //      root.setAttribute(NAME_ATTR, rootNode.getUserObject());
      doc.appendChild(root);
      if (resultDirName != null) {
	Element rsFile = doc.createElement(RESULTDIR_NODE);
	rsFile.setAttribute(NAME_ATTR, resultDirName);
	root.appendChild(rsFile);
      }

      // walk the tree -- for each child of the root node:
      Enumeration kids = rootNode.children();
      while (kids.hasMoreElements()) {
	DefaultMutableTreeNode childNode = (DefaultMutableTreeNode)kids.nextElement();
	Element next = getNextElement(childNode, doc);
	if (next != null)
	  root.appendChild(next);
      }
    } catch(ParserConfigurationException pce) {
      if(log.isErrorEnabled()) {
        log.error("Exception creating DocumentBuilder.", pce);
      }
    }
    return doc;
  }

  /**
   * Generate the next element for the XML version of the workspace.
   *
   * @param nextNode a <code>DefaultMutableTreeNode</code> to write out
   * @param doc a <code>Document</code> to use to generate elements
   * @return an <code>Element</code> to add (added by the caller), possibly null
   */
  private Element getNextElement(DefaultMutableTreeNode nextNode, Document doc) {
    Object o = nextNode.getUserObject();
    if (o instanceof Experiment) {
      Element exp = doc.createElement(EXPERIMENT_NODE);
      exp.setAttribute(NAME_ATTR, ((Experiment)o).getExperimentName());
      exp.setAttribute(ID_ATTR, ((DBExperiment)o).getExperimentID());
      // what about experiment result directory?
      File eResDir = ((Experiment)o).getResultDirectory();
      if (eResDir != null) {
	Element resDir = doc.createElement(RESULTDIR_NODE);
	resDir.setAttribute(NAME_ATTR, eResDir.getPath());
	exp.appendChild(resDir);
      }
      return exp;
    } else if (o instanceof RecipeComponent) {
      Element rec = doc.createElement(RECIPE_NODE);
      rec.setAttribute(NAME_ATTR, ((RecipeComponent)o).getRecipeName());
      return rec;
    } else if (o instanceof ConfigurableComponent) {
      // A society or something to skip
      return null;
    } else {
      // A folder
      Element folder = doc.createElement(FOLDER_NODE);
      folder.setAttribute(NAME_ATTR, o.toString());
      Enumeration kids = nextNode.children();
      while (kids.hasMoreElements()) {
	DefaultMutableTreeNode childNode = (DefaultMutableTreeNode)kids.nextElement();
	Element next = getNextElement(childNode, doc);
	if (next != null)
	  folder.appendChild(next);
      }
      return folder;
    }
  }

  /**
   * Load the given XML file, creating the workspace in the given Organizer.
   * The caller is expected to have set up the organizer with a workspace,
   * TreeModel, and root Node.
   *
   * @param workspacefilename a <code>String</code> file name to load
   * @param organizer an <code>Organizer</code> to fill in
   * @return a <code>DefaultMutableTreeNode</code> root node in the workspace.
   */
  public DefaultMutableTreeNode populateWorkspace (String workspacefilename, Organizer organizer) {
    if (organizer == null)
      return null;
    doc = loadXMLFile(workspacefilename);
    if (doc == null) {
      if (log.isWarnEnabled())
	log.warn("Not restoring workspace " + workspacefilename);
      return null;
    }
    this.organizer = organizer;

    return parse(doc.getDocumentElement(), organizer.root);
  }


  // Parsing an XML workspace representation.
  // Given a root element (that has been handled), and a root Workspace
  // node that has been added, add any children
  private DefaultMutableTreeNode parse(Element element, DefaultMutableTreeNode parentNode) {
    NodeList children = element.getChildNodes();
    for(int i=0; i < children.getLength(); i++) {
      Node child = children.item(i);
      if (child.getNodeType() == Node.ELEMENT_NODE) {
	if (child.getNodeName().equals(WORKSPACE_NODE)) {
	  // The root workspace node has already been added
	  parse(((Element)child), parentNode);
	} else if (child.getNodeName().equals(RESULTDIR_NODE)) {
	  // There are 2 kinds of result directories - global and experiment
	  String resdir = ((Element)child).getAttribute(NAME_ATTR);
	  if (resdir != null) {
	    if (element.getNodeName().equals(WORKSPACE_NODE)) {
	      // If the parent element is the hi-level workspace,
	      // then this is the global result node
	      organizer.csmart.setResultFile(resdir);
	    } else {
	      // Otherwise we're trying to set the resultdir on an experiment
	      if (parentNode != null && parentNode.getUserObject() instanceof Experiment) {
		Experiment exp = (Experiment)parentNode.getUserObject();
		exp.setResultDirectory(new File(resdir));
	      }
	    }
	  }
	} else if (child.getNodeName().equals(EXPERIMENT_NODE)) {
	  // For experiments, get the name and ID
	  String eName = ((Element)child).getAttribute(NAME_ATTR);
	  String eID = ((Element)child).getAttribute(ID_ATTR);
	  if (log.isInfoEnabled())
	    log.info("Adding experiment " + eName + "(" + eID + ") to workspace.");

	  // Double check that there is an experiment of that name/ID in the DB, and do something if not? Popup?
	  // If name is wrong but ID is Ok, we'll lose the recipes at least!
	  Map expNamesMap = ExperimentDB.getExperimentNames();
	  String dbid = (String)expNamesMap.get(eName);
	  if (dbid == null) {
	    if (log.isWarnEnabled()) {
	      log.warn("XML file Exp name " + eName + " not found in DB!");
	      log.warn("Will skip loading that experiment. Look for it manually later.");
	    }
	    // Or perhaps load it anyhow, losing recipes & whatnot?
	  } else if (! dbid.equals(eID)) {
	    if (log.isWarnEnabled()) {
	      log.warn("XML file Exp " + eName + " lists experiment ID of " + eID + " but DB says experiment with that name has ID " + dbid + "!");
	      log.warn("Will skip loading that experiment. Load it manually later if you really want it.");
	    }
	  } else {
	    // This will load the experiment (incl CMTDialog),
	    // create the Node, add it to the workspace, and also
	    // add any recipes & societies & whatnot to the workspace
	    // FIXME: This method does a GUIUtils delay, which means
	    // the recipes get added to the workspace
	    // before the experiment has been fully loaded and added,
	    // which means all the experiments end up at the bottom,
	    // even if the user's organizer had listed it earlier
	    organizer.selectGivenExperimentFromDatabase(eName, eID, false);
	    DefaultMutableTreeNode eNode = organizer.getSelectedNode();
	    // But we recurse in case there is a resultdir
	    parse(((Element)child), eNode);
	  }
	} else if (child.getNodeName().equals(RECIPE_NODE)) {
	  // For recipes, we load them be name
	  String rName = ((Element)child).getAttribute(NAME_ATTR);
	  RecipeComponent rc = organizer.helper.loadRecipeNamed(rName);
	  // However, make sure it's not already in the workspace
	  // before adding it to the workspace
	  // -- this will create the node, add it, and reset the selection
	  if (rc == null) {
	    if (log.isErrorEnabled())
	      log.error("Could not find recipe named " + rName + " in database! Skipping...");
	    // Popup?
	  } else if (! organizer.isInWorkspace(rc)) {
	    if (log.isInfoEnabled())
	      log.info("Adding recipe " + rName + " to workspace.");
	    organizer.addRecipeToWorkspace(rc, parentNode);
	  }
	} else if (child.getNodeName().equals(FOLDER_NODE)) {
	  String fName = ((Element)child).getAttribute(NAME_ATTR);
	  if (log.isInfoEnabled())
	    log.info("Adding folder " + fName + " to workspace.");
	  DefaultMutableTreeNode fNode = organizer.addFolderToWorkspace(fName, parentNode);
	  // (this will set the new selected node to be that folder)
	  // also, it returns the new workspace node
	  // recurse into the folder contents
	  parse(((Element)child), fNode);
	}
      }
    }
    return parentNode;
  }

  /**
   * Given the root node in an XML document, find the result dir name, if any.
   *
   * @param root a <code>Node</code> at the root of an XML document
   * @return a <code>String</code> result directory name, possibly null
   */
  public String parseResultDirName(Node root) {
    NodeList children = root.getChildNodes();
    for(int i=0; i < children.getLength(); i++) {
      Node child = children.item(i);
      if (child.getNodeType() == Node.ELEMENT_NODE) {
	if (child.getNodeName().equals(RESULTDIR_NODE)) {
	  return ((Element)child).getAttribute(NAME_ATTR);
	} else if (child.getNodeName().equals(WORKSPACE_NODE)) {
	  return parseResultDirName(child);
	}
      }
    }
    return null;
  }

}
