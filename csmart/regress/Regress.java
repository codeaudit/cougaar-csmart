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

import junit.framework.*;

/**
 * This is the class run by the nightly build process to run JUnit tests.<br>
 * Run all of the CSMART JUnit tests.<br>
 * Developers must maintain this to keep the set of tests to run up-to-date.<br>
 *
 * @author <a href="mailto:ahelsing@bbn.com">Aaron Helsinger</a>
 */
public class Regress extends TestSuite {
  public Regress() {
    super();
    addTest(Regress.suite());
  }
  
  public static void main(String[] args) {
    junit.textui.TestRunner.run(suite());
  }

  public static Test suite() {
    TestSuite csmartTests = new TestSuite();
    csmartTests.addTest(new TestSuite(org.cougaar.tools.csmart.util.parser.ParserTest.class));
    csmartTests.addTest(new TestSuite(org.cougaar.tools.csmart.util.parser.SimpleParserTest.class));
    csmartTests.addTest(new TestSuite(org.cougaar.tools.csmart.util.ArgValueTest.class));    
    csmartTests.addTest(new TestSuite(org.cougaar.tools.csmart.ui.component.ComponentNameTest.class));
    csmartTests.addTest(new TestSuite(org.cougaar.tools.csmart.ui.component.ConcatenatedNameTest.class));
    csmartTests.addTest(new TestSuite(org.cougaar.tools.csmart.ui.component.DoubleRangeTest.class));
    csmartTests.addTest(new TestSuite(org.cougaar.tools.csmart.ui.component.FloatRangeTest.class));
    csmartTests.addTest(new TestSuite(org.cougaar.tools.csmart.ui.component.IntegerRangeTest.class));
    csmartTests.addTest(new TestSuite(org.cougaar.tools.csmart.ui.component.LongRangeTest.class));
    csmartTests.addTest(new TestSuite(org.cougaar.tools.csmart.ui.component.StringRangeTest.class));
    csmartTests.addTest(new TestSuite(org.cougaar.tools.csmart.ui.component.MultiNameTest.class));
    csmartTests.addTest(new TestSuite(org.cougaar.tools.csmart.ui.component.SimpleNameTest.class));
    csmartTests.addTest(new TestSuite(org.cougaar.tools.csmart.ui.component.PropertyAliasTest.class));    
    csmartTests.addTest(new TestSuite(org.cougaar.tools.csmart.ui.component.ConfigurableComponentPropertyTest.class));
    csmartTests.addTest(new TestSuite(org.cougaar.tools.csmart.ui.component.ConfigurableComponentTest.class));    
    csmartTests.addTest(new TestSuite(org.cougaar.tools.csmart.ui.component.GenericLeafComponentDataTest.class));    
    //csmartTests.addTest(new TestSuite(org.cougaar.tools.csmart.ui.component.AgentComponentDataTest.class));    
    csmartTests.addTest(new TestSuite(org.cougaar.tools.csmart.ui.component.CommunityTimePhasedDataTest.class));    

    csmartTests.addTest(new TestSuite(org.cougaar.tools.csmart.ui.component.TimePhasedDataTest.class));    
    csmartTests.addTest(new TestSuite(org.cougaar.tools.csmart.configgen.abcsociety.ABCAgentTest.class));    

    return csmartTests;
  }
}
