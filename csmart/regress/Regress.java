/* 
 * <copyright>
 *  
 *  Copyright 2001-2004 BBNT Solutions, LLC
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

import junit.framework.*;

/**
 * This is the class run by the nightly build process to run JUnit tests.<br>
 * Run all of the CSMART JUnit tests.<br>
 * Developers must maintain this to keep the set of tests to run up-to-date.<br>
 *
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
    csmartTests.addTest(new TestSuite(org.cougaar.tools.csmart.util.parser.SimpleParserTest.class));
    csmartTests.addTest(new TestSuite(org.cougaar.tools.csmart.util.ArgValueTest.class));    
    csmartTests.addTest(new TestSuite(org.cougaar.tools.csmart.core.property.name.ComponentNameTest.class));
    csmartTests.addTest(new TestSuite(org.cougaar.tools.csmart.core.property.name.ConcatenatedNameTest.class));
    csmartTests.addTest(new TestSuite(org.cougaar.tools.csmart.core.property.range.DoubleRangeTest.class));
    csmartTests.addTest(new TestSuite(org.cougaar.tools.csmart.core.property.range.FloatRangeTest.class));
    csmartTests.addTest(new TestSuite(org.cougaar.tools.csmart.core.property.range.IntegerRangeTest.class));
    csmartTests.addTest(new TestSuite(org.cougaar.tools.csmart.core.property.range.LongRangeTest.class));
    csmartTests.addTest(new TestSuite(org.cougaar.tools.csmart.core.property.range.StringRangeTest.class));
    csmartTests.addTest(new TestSuite(org.cougaar.tools.csmart.core.property.name.MultiNameTest.class));
    csmartTests.addTest(new TestSuite(org.cougaar.tools.csmart.core.property.name.SimpleNameTest.class));
    csmartTests.addTest(new TestSuite(org.cougaar.tools.csmart.core.property.PropertyAliasTest.class));    
    csmartTests.addTest(new TestSuite(org.cougaar.tools.csmart.core.property.ConfigurableComponentPropertyTest.class));
    csmartTests.addTest(new TestSuite(org.cougaar.tools.csmart.core.property.ConfigurableComponentTest.class));    
    csmartTests.addTest(new TestSuite(org.cougaar.tools.csmart.core.cdata.GenericLeafComponentDataTest.class));    
    csmartTests.addTest(new TestSuite(org.cougaar.tools.csmart.core.cdata.CommunityTimePhasedDataTest.class));    
    csmartTests.addTest(new TestSuite(org.cougaar.tools.csmart.experiment.LeafOnlyConfigWriterTest.class));    
    csmartTests.addTest(new TestSuite(org.cougaar.tools.csmart.core.cdata.AgentComponentDataTest.class));    

    csmartTests.addTest(new TestSuite(org.cougaar.tools.csmart.core.cdata.TimePhasedDataTest.class));    
    csmartTests.addTest(new TestSuite(org.cougaar.tools.csmart.experiment.ExperimentTest.class));    

    // Leave this commented out for now - needs a UI, so nightly build
    // may have trouble....
    //    csmartTests.addTest(new TestSuite(org.cougaar.tools.csmart.ui.console.ConsoleTextPaneTest.class));    

    return csmartTests;
  }
}
