/*
 * <copyright>
 * This software is to be used only in accordance with the COUGAAR license
 * agreement. The license agreement and other information can be found at
 * http://www.cougaar.org
 *
 * © Copyright 2000, 2001 BBNT Solutions LLC
 * </copyright>
 */

package org.cougaar.tools.csmart.ui.util;

import java.lang.reflect.Array;
import java.util.Iterator;

import org.cougaar.tools.csmart.ui.component.*;

/**
 * Provides methods to render default values and allowed values as Strings.
 */

public class Renderer {

  private static interface ArrayElementRenderer {
    String render(Object ary, int ix);
  }

  private static ArrayElementRenderer intRenderer =
  new ArrayElementRenderer() {
    public String render(Object ary, int ix) {
      return String.valueOf(((int[]) ary)[ix]);
    }
  };
  private static ArrayElementRenderer longRenderer =
  new ArrayElementRenderer() {
    public String render(Object ary, int ix) {
      return String.valueOf(((long[]) ary)[ix]);
    }
  };
  private static ArrayElementRenderer shortRenderer =
  new ArrayElementRenderer() {
    public String render(Object ary, int ix) {
      return String.valueOf(((short[]) ary)[ix]);
    }
  };
  private static ArrayElementRenderer byteRenderer =
  new ArrayElementRenderer() {
    public String render(Object ary, int ix) {
      return String.valueOf(((byte[]) ary)[ix]);
    }
  };
  private static ArrayElementRenderer charRenderer =
  new ArrayElementRenderer() {
    public String render(Object ary, int ix) {
      return String.valueOf(((char[]) ary)[ix]);
    }
  };
  private static ArrayElementRenderer doubleRenderer =
  new ArrayElementRenderer() {
    public String render(Object ary, int ix) {
      return String.valueOf(((double[]) ary)[ix]);
    }
  };
  private static ArrayElementRenderer floatRenderer =
  new ArrayElementRenderer() {
    public String render(Object ary, int ix) {
      return String.valueOf(((float[]) ary)[ix]);
    }
  };
  private static ArrayElementRenderer booleanRenderer =
  new ArrayElementRenderer() {
    public String render(Object ary, int ix) {
      return String.valueOf(((boolean[]) ary)[ix]);
    }
  };

  private static class ObjectArrayElementRenderer implements ArrayElementRenderer {
    Class cls;
    public ObjectArrayElementRenderer(Class cls) {
      this.cls = cls;
    }
    public String render(Object ary, int ix) {
      return renderValue(cls, ((Object[]) ary)[ix]);
    }
  }

  private static String renderRange(Range range) {
    return range.getMinimumValue() + ".." + range.getMaximumValue();
  }

  private static String renderObjectArray(Class cls, Object ary) {
    return renderArray(ary, new ObjectArrayElementRenderer(cls));
  }

  private static String renderArray(Object ary, ArrayElementRenderer renderer) {
    StringBuffer buf = new StringBuffer();
    for (int i = 0, n = Array.getLength(ary); i < n; i++) {
      if (i == 0) {
	buf.append("{");
      } else {
	buf.append(",");
      }
      buf.append(renderer.render(ary, i));
    }
    buf.append("}");
    return buf.toString();
  }

  /**
   * Return string representation of objects, arrays of objects, and ranges.
   * Objects are represented using toString
   * Arrays are represented as {x,y,z}
   * Ranges are represented as x..y
   * @param cls the desired class of the object
   * @param val the object
   * @return the String representation of the object
   */

  public static String renderValue(Class cls, Object val) {
    if (val == null) return "<not set>";
    if (cls.isArray()) {
      Class ccls = cls.getComponentType();
      if (ccls.isPrimitive()) {
	if (ccls == Integer.TYPE)   return renderArray(val, intRenderer);
	if (ccls == Long.TYPE)      return renderArray(val, longRenderer);
	if (ccls == Short.TYPE)     return renderArray(val, shortRenderer);
	if (ccls == Byte.TYPE)      return renderArray(val, byteRenderer);
	if (ccls == Character.TYPE) return renderArray(val, charRenderer);
	if (ccls == Double.TYPE)    return renderArray(val, doubleRenderer);
	if (ccls == Float.TYPE)     return renderArray(val, floatRenderer);
	if (ccls == Boolean.TYPE)   return renderArray(val, booleanRenderer);
      } else {
	return renderArray(val, new ObjectArrayElementRenderer(ccls));
      }
    }
    if (cls == Range.class) {
      return renderRange((Range) val);
    }
    return val.toString();
  }

  public static void main(String[] args) {
    ABCSocietyComponent abc = new ABCSocietyComponent();
    Iterator i = abc.getPropertyNames();
    while (i.hasNext()) {
      Property prop = abc.getProperty((CompositeName)i.next());
      Class cls = prop.getPropertyClass();
      if (cls == null) {
	System.err.println("getPropertyClass is null for " + prop.getName());
	Object value = prop.getValue();
	if (value == null) {
	  cls = Object.class;
	} else {
	  cls = value.getClass();
	}
      }
      System.out.println("Value: " + 
			 Renderer.renderValue(cls, prop.getValue()));
      System.out.println("Default value: " + 
			 Renderer.renderValue(cls, prop.getDefaultValue()));
      System.out.println("Allowed value: " +
			 Renderer.renderValue(cls, prop.getAllowedValues()));
      if (cls.isArray())
	System.out.println("Class: Array of " + 
			   cls.getComponentType().getName());
      else
	System.out.println("Class: " + cls.getName());
    }
  }

}
