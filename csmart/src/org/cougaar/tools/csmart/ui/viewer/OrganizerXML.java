/* 
 * <copyright>
 * Copyright 2002 BBNT Solutions, LLC
 * under sponsorship of the Defense Advanced Research Projects Agency (DARPA).

 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the Cougaar Open Source License as published by
 * DARPA on the Cougaar Open Source Website (www.cougaar.org).

 * THE COUGAAR SOFTWARE AND ANY DERIVATIVE SUPPLIED BY LICENSOR IS
 * PROVIDED 'AS IS' WITHOUT WARRANTIES OF ANY KIND, WHETHER EXPRESS OR
 * IMPLIED, INCLUDING (BUT NOT LIMITED TO) ALL IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE, AND WITHOUT
 * ANY WARRANTIES AS TO NON-INFRINGEMENT.  IN NO EVENT SHALL COPYRIGHT
 * HOLDER BE LIABLE FOR ANY DIRECT, SPECIAL, INDIRECT OR CONSEQUENTIAL
 * DAMAGES WHATSOEVER RESULTING FROM LOSS OF USE OF DATA OR PROFITS,
 * TORTIOUS CONDUCT, ARISING OUT OF OR IN CONNECTION WITH THE USE OR
 * PERFORMANCE OF THE COUGAAR SOFTWARE.
 * </copyright>
 */
package org.cougaar.tools.csmart.ui.viewer;

import java.io.File;
import java.io.IOException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.swing.tree.*;
import java.util.Enumeration;

import org.cougaar.tools.csmart.util.XMLUtils;
import org.cougaar.tools.csmart.experiment.Experiment;
import org.cougaar.tools.csmart.recipe.RecipeComponent;
import org.cougaar.tools.csmart.core.property.ConfigurableComponent;
import org.cougaar.util.log.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

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
      exp.setAttribute(ID_ATTR, ((Experiment)o).getExperimentID());
      // what about experiment result directory?
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

  // Organizer itself can call loadXMLFile
  // create the root Node
  // then I dont know what....

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
