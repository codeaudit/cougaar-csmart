package org.cougar.tools.csmart.util.parser;

import java.io.IOException;
import java.util.Enumeration;

import junit.framework.*;

import com.bbn.abc.ul.ldm.plugin.customer.*;

public class ParserTest extends TestCase {

  private TaskFileParser tfp = new TaskFileParser();
  private Enumeration tasks = null;

  public ParserTest(String name) {
     super(name);
  }
    
  protected void setUp() {
     try {
        tfp.load("testScript.ini");
        tfp.parse();
     } catch(IOException e) {}  
  }
  
  public void testParse() {     
     tasks = tfp.elements();
     assert(tasks.hasMoreElements());
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
