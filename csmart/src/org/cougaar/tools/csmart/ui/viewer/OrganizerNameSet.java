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

package org.cougaar.tools.csmart.ui.viewer;

import org.cougaar.tools.csmart.core.db.ExperimentDB;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

public class OrganizerNameSet extends UniqueNameSet {
  private Class[] noTypes = new Class[0];
  private Method getNameMethod = null;
  private String prompt;
  private String databaseCheck;
  private Method dbCheckMethod = null;

  /**
   * The namePrefix is used as the base to generate uniqu names.
   * The prompt is the prompt to display for the user to get a name.
   * The databaseCheck method is assumed to be the name of a method
   * in ExperimentDB that takes the component name as an argument
   * and returns a boolean indicating if that name is in the database.
   * @param namePrefix a single word prepended to unique objects
   * @param prompt a single word describing the object to be named
   * @param databaseCheck method name to check for name in database or null
   */
  public OrganizerNameSet(String namePrefix, String prompt,
                          String databaseCheck) {
    super(namePrefix);
    this.prompt = prompt;
    this.databaseCheck = databaseCheck;
  }

  public void init(DefaultMutableTreeNode root, Class leafClass,
                   String getNameMethodName) {
    try {
      getNameMethod = leafClass.getMethod(getNameMethodName, noTypes);
    } catch (Exception e) {
      if (log.isErrorEnabled()) {
        log.error("Getting name method: " + e);
      }
      return;
    }
    Object things[] = getObjects(leafClass, root);
    init(things, getNameMethod);
  }

  public Object getObjectInTree(Class leafClass, DefaultMutableTreeNode root,
                                String name) {
    Object[] things = getObjects(leafClass, root);
    Object[] noArgs = new Object[0];
    for (int i = 0; i < things.length; i++) {
      try {
        String thingName = (String) getNameMethod.invoke(things[i], noArgs);
        if (thingName.equals(name))
          return things[i];
      } catch (Exception e) {
        if(log.isErrorEnabled()) {
          log.error("Reading: " + things[i], e);
        }
      }
    }
    return null;
  }

  private Object[] getObjects(Class leafClass, DefaultMutableTreeNode root) {
    List result = new ArrayList();
    for (Enumeration e = root.depthFirstEnumeration(); e.hasMoreElements(); ) {
      DefaultMutableTreeNode node =
        (DefaultMutableTreeNode) e.nextElement();
      Object o = node.getUserObject();
      if (leafClass.isInstance(o))
        result.add(o);
    }
    return result.toArray((Object[]) Array.newInstance(leafClass, result.size()));
  }

  /**
   * Get a name for a folder, experiment, society, recipe, etc.
   * that is unique in the workspace and in the database.
   * @param originalName the current name of the object
   * @param allowExistingName true to allow existing name (true if renaming)
   * @return String new unique name
   */
  protected String getUniqueName(String originalName,
                                 boolean allowExistingName) {
    String name = null;
    while (true) {
      // get new name from user
      String inputPrompt = "Enter " + prompt + " Name";
      String title = prompt + " Name";
      name = 
        (String) JOptionPane.showInputDialog(null, inputPrompt, title,
                                             JOptionPane.QUESTION_MESSAGE,
                                             null, null, originalName);
      if (name == null) return null;

      name = name.trim();
      if (name.equals("")) return null;

      if (name.equals(originalName) && allowExistingName)
        return name;

      // if name is not unique in CSMART, tell user to try again
      if (contains(name)) {
        title = prompt + " Name Not Unique";
        int answer = 
          JOptionPane.showConfirmDialog(null,
                                        "Use an unique name",
                                        title,
                                        JOptionPane.OK_CANCEL_OPTION,
                                        JOptionPane.ERROR_MESSAGE);
        if (answer != JOptionPane.OK_OPTION) return null;
      } else {
        // if name is unique in CSMART and not checking database, return name
        if (databaseCheck == null)
          return name;
        // ensure that name is not in the database
        // if can't reach database, just assume that name is ok
        boolean inDatabase = isInDatabase(name);
        if (inDatabase) {
          title = prompt + " Name Not Unique";
          int answer = 
            JOptionPane.showConfirmDialog(null,
                                          "This name is in the database; use an unique name",
                                          title,
                                          JOptionPane.OK_CANCEL_OPTION,
                                          JOptionPane.ERROR_MESSAGE);
          if (answer != JOptionPane.OK_OPTION) return null;
        } else
          return name; // have unique name
      } // end if unique in CSMART
    } // end while loop
  }

  private void getDatabaseCheckMethod() {
    Class[] paramClasses = { String.class };
    try {
      dbCheckMethod =
        ExperimentDB.class.getMethod(databaseCheck, paramClasses);
    } catch (NoSuchMethodException e) {
      if(log.isErrorEnabled()) {
        log.error("No such method", e);
      }
    }
  }

  // ensure that name is not in the database
  // if can't reach database, just assume that name is not there
  private boolean isInDatabase(String name) {
    if (databaseCheck == null)
      return false;
    if (dbCheckMethod == null)
      getDatabaseCheckMethod();
    if (dbCheckMethod == null)
      return false;
    boolean inDatabase = false;
    Object[] args = new Object[1];
    args[0] = name;
    try {
      Object result = dbCheckMethod.invoke(null, args);
      if (result instanceof Boolean)
        inDatabase = ((Boolean)result).booleanValue();
      else
        if(log.isErrorEnabled()) {
          log.error("In Database Check returned unexpected result: " + 
                    inDatabase);
        }
    } catch (Exception e) {
      if(log.isErrorEnabled()) {
        log.error("Exception checking for unique name in database", e);
      }
    }
    return inDatabase;
  }

  protected boolean isUniqueName(String name) {
    return !contains(name) && !isInDatabase(name);
  }

  /**
   * Generate a name that is unique in the CSMART workspace,
   * and in the database.  The user is only prompted
   * for a new name, if the generated name is not unique.
   */

  protected String generateUniqueName(String name, 
                                      boolean allowExistingName) {
    if (!allowExistingName)
      name = generateName(name);
    if (isUniqueName(name))
      return name;
    else
      return getUniqueName(name, allowExistingName);
  }

}
