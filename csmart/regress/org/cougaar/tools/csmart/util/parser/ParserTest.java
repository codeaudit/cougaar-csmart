/* 
 * <copyright>
 *  Copyright 2001 BBNT Solutions, LLC
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
package org.cougaar.tools.csmart.util.parser;

import java.io.IOException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.io.FileNotFoundException;
import java.util.Enumeration;

import junit.framework.*;

import org.cougaar.tools.csmart.runtime.ldm.plugin.customer.*;

/**
 * Test the TaskFileParser in a fairly trivial way.<br>
 * Note that this writes out a hard-coded test data file.
 *
 */
public class ParserTest extends TestCase {

  private TaskFileParser tfp = new TaskFileParser();
  private Enumeration tasks = null;
  File testFile = null; // handle to test file, to remove when done
  private static final String testFileName = "testScript.ini";

  public ParserTest(String name) {
     super(name);
  }
    
  // Write out the test data file, then load it
  protected void setUp() {
    try {
      testFile = new File(testFileName);
    } catch (NullPointerException e) {
    }
    try {
      PrintWriter writer = new PrintWriter(new FileOutputStream(testFile));
      writer.println("# <WorldState>, <TaskType>, <Rate>, <Deviation>, <Vitality>, <Duration>");
      writer.println("PEACE, A, 50, 10, 0.2, 100");
      writer.println("WAR,B,5,25,0.98,2500");
      writer.println("PEACE,D,45,0,1,20");
      writer.close();
    } catch (SecurityException e) {
      fail("Got exception writing test file: " + e);
    } catch (FileNotFoundException e) {
      fail("Got exception writing test file: " + e);
    }

    try {
      tfp.load(testFileName);
      tfp.parse();
    } catch(IOException e) {
      fail("Got exception loading testcript: " + e);
    }  
  }
  
  protected void tearDown() {
    if (testFile != null) {
      try {
	testFile.delete();
      } catch (SecurityException e) {}
    }
  }

  public void testParse() {     
     tasks = tfp.elements();
     assertTrue(tasks.hasMoreElements());
  }
  
  public void testEntries() {
     
     tasks = tfp.elements();

     SingleCustomerTask ct = new SingleCustomerTask();

     // Test Inputs: PEACE, A, 50, 10, 0.2, 100   
     if( tasks.hasMoreElements() ) {     
        
        ct = (SingleCustomerTask) tasks.nextElement();
        
        assertEquals("World State Check", "PEACE", ct.getWorldState());
        assertEquals("Task Type Check", "A", ct.getTaskType());
        assertEquals("Rate Check", 50, ct.getRate());
        assertEquals("Deviation Check", 10, ct.getDeviation());
        assertEquals("Vitality Check", 0.2, ct.getVitality(), 0.01);
        assertEquals("Duration Check", 100, ct.getDuration());        
     }

     // Test Inputs: WAR, B, 5, 25, 0.98, 2500   
     if( tasks.hasMoreElements() ) {     
        
        ct = (SingleCustomerTask) tasks.nextElement();
        
        assertEquals("World State Check", "WAR", ct.getWorldState());
        assertEquals("Task Type Check", "B", ct.getTaskType());
        assertEquals("Rate Check", 5, ct.getRate());
        assertEquals("Deviation Check", 25, ct.getDeviation());
        assertEquals("Vitality Check", 0.98, ct.getVitality(), 0.01);
        assertEquals("Duration Check", 2500, ct.getDuration());        
        
     }                                  
     
     // Test Inputs: PEACE, D, 45, 0, 1, 20   
     if( tasks.hasMoreElements() ) {     
        
        ct = (SingleCustomerTask) tasks.nextElement();
        
        assertEquals("World State Check", "PEACE", ct.getWorldState());
        assertEquals("Task Type Check", "D", ct.getTaskType());
        assertEquals("Rate Check", 45, ct.getRate());
        assertEquals("Deviation Check", 0, ct.getDeviation());
        assertEquals("Vitality Check", 1.0, ct.getVitality(), 0.01);
        assertEquals("Duration Check", 20, ct.getDuration());        
        
     }                                  

  }
  
  public static Test suite() {
     return new TestSuite(ParserTest.class);     
  }

	public static void main(String[] args) {

     junit.textui.TestRunner.run(suite());          	                
	}
}
