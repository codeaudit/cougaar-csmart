/**
 * <copyright>
 *  Copyright 2002-2003 BBNT Solutions, LLC
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
package org.cougaar.tools.csmart.experiment;

import java.io.File;
import java.lang.IllegalArgumentException;
import java.lang.IndexOutOfBoundsException;
import java.util.List;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.cougaar.tools.csmart.recipe.RecipeBase;
import org.cougaar.tools.csmart.recipe.RecipeComponent;
import org.cougaar.tools.csmart.society.SocietyBase;
import org.cougaar.tools.csmart.society.SocietyComponent;

/**
 *  Unit Test for classExperiment
 *
 *
 * Created: Fri Mar 22 13:30:26 2002
 *
 * @author <a href="mailto:bkrisler@bbn.com">Brian Krisler</a>
 * @version
 */
public class ExperimentTest extends TestCase {

  private Experiment experiment = null;

  /** 
   * Creates a new <code>ExperimentTest</code> instance.
   *
   * @param name test name
   */
  public ExperimentTest (String name){
    super(name);
  }

  protected void setUp() {
    experiment = new Experiment("testExperiment");
  }

  public void testSocietyOperations() {
    SocietyComponent sc = new SimpleSociety("Testing");
    assertEquals("Test getSocietyComponentCount", 0, experiment.getSocietyComponentCount());

    experiment.addSocietyComponent(sc);

    // Try to add a second one.
    try {
      experiment.addSocietyComponent(sc);
      fail("Expected IllegalArgumentException");
    } catch (IllegalArgumentException ee) {}

    assertEquals("Test getSocietyComponentCount", 1, experiment.getSocietyComponentCount());
    assertEquals("Test getSocietyComponent", sc, experiment.getSocietyComponent());
    experiment.removeSocietyComponent();
    assertEquals("Test remove success", 0, experiment.getSocietyComponentCount());    
  }


  public void testRecipeOperations() {
    RecipeComponent rc = new SimpleRecipe("Testing");
    RecipeComponent rc1 = new SimpleRecipe("Testing1");
    RecipeComponent rc2 = new SimpleRecipe("Testing2");

    assertEquals("Test getRecipeCount", 0, experiment.getRecipeComponentCount());

    experiment.addRecipeComponent(rc);

    // Try to add a second one, of same name.
    try {
      experiment.addRecipeComponent(rc);
      fail("Expected IllegalArgumentException");
    } catch (IllegalArgumentException ee) {}

    assertEquals("Test getRecipeCount", 1, experiment.getRecipeComponentCount());
    assertEquals("Test getRecipe(int)", rc, experiment.getRecipeComponent(0));

    // Try boundries
    try {
      experiment.getRecipeComponent(-1);
      fail("Expected IndexOutOfBoundsException");
    } catch (IndexOutOfBoundsException ee) {}

    try {
      experiment.getRecipeComponent(5);
      fail("Expected IndexOutOfBoundsException");
    } catch (IndexOutOfBoundsException ee) {}

    experiment.removeRecipeComponent(rc);
    assertEquals("Test Remove", 0, experiment.getRecipeComponentCount());

    RecipeComponent[] all = new RecipeComponent[3];
    all[0] = rc;
    all[1] = rc1;
    all[2] = rc2;
    experiment.setRecipeComponents(all);
    assertEquals("Test setRecipes", 3, experiment.getRecipeComponentCount());

    RecipeComponent[] dump = experiment.getRecipeComponents();
    assertEquals("Test getRecipes 1", all[0], dump[0]);
    assertEquals("Test getRecipes 2", all[1], dump[1]);
    assertEquals("Test getRecipes 3", all[2], dump[2]);
  }

  public void testGenericComponentOperations() {
    RecipeComponent rc = new SimpleRecipe("r1");
    RecipeComponent rc2 = new SimpleRecipe("r2");
    SocietyComponent sc = new SimpleSociety("s");

    experiment.addComponent(rc);
    assertEquals("Test Successful recipe add", 1, experiment.getRecipeComponentCount());
    experiment.addComponent(sc);
    assertEquals("Test Successful society add", 1, experiment.getSocietyComponentCount());
    assertEquals("Test getComponentCount", 2, experiment.getComponentCount());
    experiment.removeComponent(rc);
    assertEquals("Test Successful recipe add", 0, experiment.getRecipeComponentCount());
   
  }

  public void testRunInProgress() {
    experiment.setRunInProgress(true);
    assertEquals("Test Run In Progress", true, experiment.isRunInProgress());
  }

  public void testEditInProgress() {
    experiment.setEditInProgress(true);
    assertEquals("Test Edit In Progress", true, experiment.isEditInProgress());
  }

  public void testResultDirectory() {
    experiment.setResultDirectory(new File("Test"));
    assertEquals("Test ResultDirectory", new File("Test"), experiment.getResultDirectory());
  }
  
  public void testHostComponent() {
    HostComponent h = experiment.addHost("New Host");

    // Try to add a second one, of same name.
    try {
      experiment.addHost("New Host");
      fail("addHost: Expected IllegalArgumentException");
    } catch (IllegalArgumentException ee) {}
    
    try {
      experiment.renameHost(h, "New Host");
      fail("renameHost: Expected IllegalArgumentException");
    } catch (IllegalArgumentException ee) {}

    experiment.renameHost(h, "New Name");
    HostComponent[] hosts = experiment.getHostComponents();
    assertEquals("Test get hosts", h, hosts[0]);

    experiment.removeHost(h);
    hosts = experiment.getHostComponents();
    assertEquals("Test Remove Host", 0, hosts.length);
    
  }


  public void testNodeComponent() {
    NodeComponent n = experiment.addNode("New Node");

    // Try to add a second one, of same name.
    try {
      experiment.addNode("New Node");
      fail("addNode: Expected IllegalArgumentException");
    } catch (IllegalArgumentException ee) {}
    
    try {
      experiment.renameNode(n, "New Node");
      fail("renameNode: Expected IllegalArgumentException");
    } catch (IllegalArgumentException ee) {}

    experiment.renameNode(n, "New Name");
    NodeComponent[] nodes = experiment.getNodeComponents();
    assertEquals("Test get nodes", n, nodes[0]);

    experiment.removeNode(n);
    nodes = experiment.getNodeComponents();
    assertEquals("Test Remove Node", 0, nodes.length);

  }

  /**
   * @return a <code>TestSuite</code>
   */
  public static TestSuite suite(){
    return new TestSuite(ExperimentTest.class); 
  }

  /** 
   * Entry point 
   */ 
  public static void main(String[] args) {
    junit.textui.TestRunner.run(suite());
  }

  public class SimpleSociety extends SocietyBase {

    public SimpleSociety(String name) {
      super(name);
    }

    public void initProperties() {
    }
  }

  public class SimpleRecipe extends RecipeBase {

    public SimpleRecipe(String name) {
      super(name);
    }

    public void initProperties() {
    }
  }

}// ExperimentTest
