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
package org.cougaar.tools.csmart.core.property.name;

import junit.framework.*;
import org.cougaar.tools.csmart.core.property.ConfigurableComponent;

/**
 * Unit test for class ComponentName.
 */
public class ComponentNameTest extends TestCase {

  private ComponentName tst = null;
  private String componentName = "Name";
  private SimpleComponent sc = null;

  public ComponentNameTest(String name) {
    super(name);
  }

  protected void setUp() {    
    sc = new SimpleComponent(componentName);
    sc.initProperties();

    tst = new ComponentName(sc, componentName);
  }

  protected void tearDown() {
  }

  public void testGetConfigurableComponent() {
    assertEquals(sc, tst.getConfigurableComponent());
  }

  public void testSetComponent() {
    SimpleComponent nc = new SimpleComponent("New Component");
    tst.setComponent(nc);
    assertEquals(nc, tst.getConfigurableComponent());
  }

  public static Test suite() {
    return new TestSuite(ComponentNameTest.class);
  }

  public static void main(String[] args) {
    junit.textui.TestRunner.run(suite());
  }

  public class SimpleComponent extends ConfigurableComponent {
    public SimpleComponent(String name) {
      super(name);
    }
    public void initProperties() {
    }
  }
}
