package org.cougaar.tools.csmart.configgen;

/**
 * ProviderTest.java
 *
 *
 * Created: Thu Feb 22 11:41:12 2001
 *
 * @author Brian Krisler
 * @version
 */

import java.util.ArrayList;
import junit.framework.*;

public class ProviderTest extends TestCase {
  
  private Provider provider = new Provider();

  public ProviderTest(String name) {
    super(name);
  }
  

  public void testName() {
    provider.setName("Name");
    
    try {
      provider.setName("Name");
      fail("IllegalArgumentException Expected");
    } catch(IllegalArgumentException e) {}

    assertEquals("Name", provider.getName());
  }

  public void testFactor() {
    provider.setFactor(10);

    try {
      provider.setFactor(10);
      fail("IllegalArgumentException Expected");
    } catch(IllegalArgumentException e) {}

    assertEquals(10, provider.getFactor());
  }

  public void testLocalAssets() {
    ArrayList alist = new ArrayList(1);

    alist.add("Asset");
    provider.setLocalAssets(alist);
    
    try {
      provider.setLocalAssets(alist);
      fail("IllegalArgumentException Expected");
    } catch(IllegalArgumentException e) {}

    assertNotNull(provider.getLocalAssets());
    ArrayList blist = provider.getLocalAssets();
    assertEquals("Asset", blist.get(0));
  }

  public void testOrgAssets() {
    ArrayList alist = new ArrayList(1);

    alist.add("Asset");
    provider.setOrgAssets(alist);
    
    try {
      provider.setOrgAssets(alist);
      fail("IllegalArgumentException Expected");
    } catch(IllegalArgumentException e) {}

    assertNotNull(provider.getOrgAssets());
    ArrayList blist = provider.getOrgAssets();
    assertEquals("Asset", blist.get(0));
  }

  public void testDeviation() {
    provider.setDeviation(25);

    try {
      provider.setDeviation(25);
      fail("IllegalArgumentException Expected");
    } catch(IllegalArgumentException e) {}

    assertEquals(25, provider.getDeviation());
  }
    
  public static Test suite() {
    return new TestSuite(ProviderTest.class);
  }

  public static void main(String[] args) {
    junit.textui.TestRunner.run(suite());
  }
} // ProviderTest
