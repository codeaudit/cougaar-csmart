package org.cougaar.tools.csmart.util.parser;

import junit.framework.*;
import java.io.*;
import java.util.*;

/**
 * SimpleParserTest.java
 *
 *
 * Created: Fri Jan 26 14:52:44 2001
 *
 * @author Brian Krisler
 * @version
 */
public class SimpleParserTest extends TestCase {

  private SimpleParser sp = new SimpleParser();

  public SimpleParserTest(String name) {
    super(name);
  }
  
  protected void setUp() {
    try {
      sp.load("simpleParser.dat");
      sp.parse();
    } catch(IOException e) {}
  }

  public void testParse() {
    ArrayList a = sp.getList();
    assertNotNull(a);
  }

  public void testEntires() {
    ArrayList a = sp.getList();
    
    ArrayList b = null;
  
    b = (ArrayList) a.get(0);
    // Test the first entry (Asset One)
    assertEquals("Asset One", (String)b.get(0));
    assertEquals("1", (String)b.get(1));
    assertEquals("2", (String)b.get(2));
    assertEquals("3", (String)b.get(3));
    assertEquals("4", (String)b.get(4));
    assertEquals("5", (String)b.get(5));
    assertEquals("6", (String)b.get(6));
    assertEquals("AA", (String)b.get(7));
    assertEquals("BB", (String)b.get(8));
    assertEquals("CC", (String)b.get(9));
    assertEquals("DD", (String)b.get(10));

  }

  public static Test suite() {
    return new TestSuite(SimpleParserTest.class);     
  }
  
  public static void main(String[] args) {
    junit.textui.TestRunner.run(suite());          	                
  }

} // SimpleParserTest

