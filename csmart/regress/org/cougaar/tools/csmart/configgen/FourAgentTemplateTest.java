package org.cougaar.tools.csmart.configgen;

/**
 * FourAgentTemplateTest.java
 *
 *
 * Created: Thu Feb 22 15:10:27 2001
 *
 * @author Brian Krisler
 * @version 1.0
 */
import junit.framework.*;
import java.util.*;

public class FourAgentTemplateTest extends TestCase {

  private FourAgentTemplate fat = new FourAgentTemplate();

  public FourAgentTemplateTest(String name) {    
    super(name);
  }

  protected void setUp() {
  }

  public void testGetCustomer() {
    assertNotNull(fat.getCustomers());
  }

  public void testNumberCustomers() {
    assertEquals(1, fat.getNumberCustomers());
  }

  public void testStringAsset() {
 
    assertNotNull(fat.getCustomers());
    Iterator i = fat.getCustomers();
    Customer c = (Customer)i.next();
    assertNotNull(c.getAssets());
    ArrayList alist = c.getAssets();
    assert(alist.get(0) instanceof String);
  }

  public static Test suite() {
    return new TestSuite(FourAgentTemplateTest.class);
  }

  public static void main(String[] args) {
    junit.textui.TestRunner.run(suite());
  }
} // FourAgentTemplateTest
