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
package org.cougaar.tools.csmart.core.property;

import junit.framework.*;
import org.cougaar.tools.csmart.core.property.name.SimpleName;

/**
 * Unit test for class ConfigurableComponent.
 */
public class ConfigurableComponentTest extends TestCase {
  
  private SimpleComponent sc = null;

  public ConfigurableComponentTest(String name) {
    super(name);
  }

  protected void setUp() {
    sc = new SimpleComponent();
  }


  public void testAddChild() {
    assertEquals("Test addChild()", 0, sc.addChild(new SimpleComponent("Child")));         	 
  }

  public void testRemoveChild() {
    SimpleComponent child = new SimpleComponent("Child");
    sc.addChild(child);
    sc.removeChild(0);
    assertEquals("Test removeChild(int)", 0, sc.getChildCount());

    sc.addChild(child);
    sc.removeChild(child);
    assertEquals("Test removeChild(ConfigurableComponent", 0, sc.getChildCount());
  }

  public void testRemoveAllChildren() {
    sc.addChild(new SimpleComponent("Child1"));
    sc.addChild(new SimpleComponent("Child2"));
    sc.addChild(new SimpleComponent("Child3"));
    sc.addChild(new SimpleComponent("Child4"));

    sc.removeAllChildren();
    assertEquals("Test removeAllChildren()", 0, sc.getChildCount());
  }

  public void testGetChildCount() {
    sc.addChild(new SimpleComponent("Child1"));
    assertEquals("1. Test getChildCount()", 1, sc.getChildCount());
    sc.addChild(new SimpleComponent("Child2"));
    assertEquals("2. Test getChildCount()", 2, sc.getChildCount());
    sc.addChild(new SimpleComponent("Child3"));
    assertEquals("3. Test getChildCount()", 3, sc.getChildCount());
    sc.addChild(new SimpleComponent("Child4"));
    assertEquals("4. Test getChildCount()", 4, sc.getChildCount());
  }

  public void testgetChild() {
    SimpleName name = new SimpleName("ChildName");
    SimpleName failed = new SimpleName("This should fail");
    SimpleComponent tmp = new SimpleComponent("ChildName");
    SimpleComponent t2 = new SimpleComponent("NewChild");

    sc.addChild(tmp);
    sc.addChild(t2);

    assertEquals("Test getChild(int)", tmp, sc.getChild(0));
 
    assertEquals("1. Test getChild(CompositeName)", tmp, sc.getChild(name));
    assertNull("2. Test getChild(CompositeName)", sc.getChild(failed));
  }

  public void testaddProperty() {
    ConfigurableComponentProperty ccp = new ConfigurableComponentProperty(new SimpleComponent(), "Prop1", new Integer(1));
    sc.addProperty(ccp);
    assertEquals("Test addProperty", ccp, sc.getProperty(ccp.getName()));
  }

  public static Test suite() {
    return new TestSuite(ConfigurableComponentTest.class);
  }

  public static void main(String[] args) {
    junit.textui.TestRunner.run(suite());
  }
  
  public class SimpleComponent extends ConfigurableComponent {
    public SimpleComponent() {
      super("SimpleComponent");
    }

    public SimpleComponent(String name) {
      super(name);
    }

    public void initProperties() {
    }    
  }

}
