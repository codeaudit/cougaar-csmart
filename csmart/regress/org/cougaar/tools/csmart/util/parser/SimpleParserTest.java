/* 
 * <copyright>
 * This software is to be used only in accordance with the COUGAAR license
 * agreement. The license agreement and other information can be found at
 * http://www.cougaar.org
 * 
 *       © Copyright 2001 by BBNT Solutions LLC.
 * </copyright>
 */
package org.cougaar.tools.csmart.util.parser;

import junit.framework.*;
import java.io.*;
import java.util.*;

public class SimpleParserTest extends TestCase {
  File testFile = null; // handle to test file, to remove when done
  private static final String testFileName = "simpleParser.dat";
  private SimpleParser sp = new SimpleParser();

  public SimpleParserTest(String name) {
    super(name);
  }
  
  // Build the test file from here, so we dont need to check it in
  // and dont need to make assumptions about the config path, cwd
  // of where the tests are run from
  protected void setUp() {
    try {
      testFile = new File(testFileName);
    } catch (NullPointerException e) {
    }
    try {
      PrintWriter writer = new PrintWriter(new FileOutputStream(testFile));
      writer.println("# This is a test script used by JUnit to test the Simple Parser");
      writer.println("# ");
      writer.println("# ***********  DO NOT MODIFY!!!!!  Modifying will break the regression testing.");
      writer.println("");
      writer.println("Asset One, 1, 2, 3, 4, 5, 6, AA, BB, CC, DD");
      writer.close();
    } catch (SecurityException e) {
      fail("Got exception writing test file: " + e);
    } catch (FileNotFoundException e) {
      fail("Got exception writing test file: " + e);
    }

    try {
      sp.load(testFileName);
      sp.parse();
    } catch(IOException e) {}
  }

  protected void tearDown() {
    if (testFile != null) {
      try {
	testFile.delete();
      } catch (SecurityException e) {}
    }
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

