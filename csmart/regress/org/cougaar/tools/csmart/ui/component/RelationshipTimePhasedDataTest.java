/* 
 * <copyright>
 * This software is to be used only in accordance with the COUGAAR license
 * agreement. The license agreement and other information can be found at
 * http://www.cougaar.org
 * 
 *       © Copyright 2001 by BBNT Solutions LLC.
 * </copyright>
 */

package org.cougaar.tools.csmart.ui.component;

import junit.framework.*;
import java.util.Iterator;

public class RelationshipTimePhasedDataTest extends TestCase {

  private RelationshipTimePhasedData data = null;

  public RelationshipTimePhasedDataTest(String name) {
    super(name);
  }

  protected void setUp() {
    data = new RelationshipTimePhasedData();
  }

  public void testRole() {
    data.setRole("Role");
    assertEquals("Test Role", "Role", data.getRole());
  }

  public void testItem() {
    data.setItem("Item");
    assertEquals("Test Item", "Item", data.getItem());
  }

  public void testType() {
    data.setType("Type");
    assertEquals("Test Type", "Type", data.getType());
  }

  public void testCluster() {
    data.setCluster("Cluster");
    assertEquals("Test Cluster", "Cluster", data.getCluster());
  }

  public static Test suite() {
    return new TestSuite(RelationshipTimePhasedDataTest.class);
  }

  public static void main(String[] args) {
    junit.textui.TestRunner.run(suite());
  }

}
