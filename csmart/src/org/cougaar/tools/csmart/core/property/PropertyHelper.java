/*
 * <copyright>
 *  Copyright 2000-2001 BBNT Solutions, LLC
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

package org.cougaar.tools.csmart.ui.component;

import java.util.*;
import java.lang.reflect.*;

public class PropertyHelper {
    private static final Class[] stringArgType = {String.class};

    private static Object valueFromString(Class cls, String s)
        throws InvalidPropertyValueException
    {
        try {
            Constructor c = cls.getConstructor(stringArgType);
            return c.newInstance(new String[] {s});
        } catch (Exception e) {
            throw new InvalidPropertyValueException("Cannot create " + cls
                                                    + " from " + s);
        }
    }

//      public static Object convertStringToArray(String s) {
//          while (s.startsWith("{")) s = s.substring(1);
//          while (s.endsWith("}")) s = s.substring(0, s.length() - 1);
//          StringTokenizer tokens = new StringTokenizer(s, ",");
//          List result = new ArrayList();
//          while (tokens.hasMoreTokens()) {
//              result.add(tokens.nextToken().trim());
//          }
//          return result.toArray(new String[result.size()]);
//      }

  /**
   * Convert string to array, allowing for nested braces, i.e.
   * this strips off just one set of braces each time it is called.
   */

  public static Object convertStringToArray(String s) {
    if (s.startsWith("{")) s = s.substring(1);
    if (s.endsWith("}")) s = s.substring(0, s.length() - 1);
    if (s.startsWith("{")) { 
      StringTokenizer tokens = new StringTokenizer(s, "{");
      List result = new ArrayList();
      while (tokens.hasMoreTokens()) {
        String tmp = tokens.nextToken();
        if (tmp.endsWith(",")) 
          tmp = tmp.substring(0, tmp.length() - 1);
        result.add("{" + tmp.trim());
      }
      return result.toArray(new String[result.size()]);
    } else {
      StringTokenizer tokens = new StringTokenizer(s, ",");
      List result = new ArrayList();
      while (tokens.hasMoreTokens()) {
        result.add(tokens.nextToken().trim());
      }
      return result.toArray(new String[result.size()]);
    }
  }

    public static Object convertStringToArrayList(String s) {
        while (s.startsWith("[")) s = s.substring(1);
        while (s.endsWith("]")) s = s.substring(0, s.length() - 1);
        StringTokenizer tokens = new StringTokenizer(s, ",");
        List result = new ArrayList();
        while (tokens.hasMoreTokens()) {
            result.add(tokens.nextToken().trim());
        }
        return result;
    }

    public static Object validateValue(Property p, Object newValue)
        throws InvalidPropertyValueException
    {
        return validateValue(p.getPropertyClass(), p.getAllowedValues(), newValue);
    }

    /**
     * Check if a value is valid. To be valid, the newValue must be an
     * instance of the specified class or convertible to that class
     * and must be included in the validValues set. The conversions
     * performed are as follows:
     * If the value class is unspecified, no checks are performed.
     *
     * If the class is an array class:
     *
     *   If the object is not an array, then the newValue is replaced
     *   with an array obtained by parsing its value as a string. The
     *   array elements are separated by commas and optionally
     *   surrounded by braces. Currently, only single level arrays are
     *   handled.
     *
     *   Once the newValue has been coerced into being an array, the
     *   elements of the newValue array are validated recursively
     *   using the Object equivalent of the component type of the
     *   required class.
     *
     * If the class is not an array class:
     *
     *   If the newValue is not an instance of the required class, the
     *   newValue is replaced with an instance of the required class
     *   by finding the constructor of the required class that accepts
     *   a single String argument and using it to create a newInstance
     *   using the String from the toString method of the newValue.
     *
     *   The value is checked against the validValues set.
     **/
    public static Object validateValue(Class cls, Set validValues, Object newValue)
        throws InvalidPropertyValueException
    {
        if (cls == null) return newValue;
        if (cls.isArray()) {
            if (!newValue.getClass().isArray()) {
                newValue = convertStringToArray(newValue.toString());
            }
            Class cclass = cls.getComponentType();
            if (cclass == Integer.TYPE)
                return validateIntArrayValue(validValues, newValue);
            if (cclass == String.class)
                return validateStringArrayValue(validValues, newValue);
            return newValue;
	} else if (cls.equals(ArrayList.class)) {
	  // System.out.println("!!! validateValue - is ArrayList");
	  if (!(newValue.getClass().getComponentType() == ArrayList.class)) {
	    newValue = convertStringToArrayList(newValue.toString());
	  }
	  // Now I want to make convert each element in this ArrayList into the appropriate type: Integer, etc
	  // Thats hard. for now, return them as Strings and hope thats OK.
	  // FIXME!!!
	  return newValue;
        } else {
            if (!cls.isInstance(newValue)) {
                newValue = valueFromString(cls, newValue.toString());
            }
            return validateValidValue(validValues, newValue);
        }
    }

    private static Object validateIntArrayValue(Set validValues, Object newValue)
        throws InvalidPropertyValueException
    {
        int len = Array.getLength(newValue);
        int[] result = new int[len];
        Class cls = newValue.getClass();
        if (cls == int[].class) {
            for (int i = 0; i < len; i++) {
                int x = Array.getInt(newValue, i);
                result[i] =
                    ((Integer) validateValidValue(validValues, new Integer(x)))
                    .intValue();
            }
        } else if (cls == String[].class) {
            for (int i = 0; i < len; i++) {
                String x = (String) Array.get(newValue, i);
		try {
		  result[i] =
                    ((Integer) validateValidValue(validValues, new Integer(x)))
                    .intValue();
		} catch (NumberFormatException e) {
		  // Guess that string wasnt an integer!
		  throw new InvalidPropertyValueException("Not an Integer: " + x);
		}
            }
        } else {
            throw new InvalidPropertyValueException("Unsupported value conversion, class is: " + cls);
        }
        return result;
    }

    private static Object validateStringArrayValue(Set validValues, Object newValue)
        throws InvalidPropertyValueException
    {
        int len = Array.getLength(newValue);
        String[] result = new String[len];
        Class cls = newValue.getClass();
        for (int i = 0; i < len; i++) {
            String x = Array.get(newValue, i).toString();
            result[i] = (String) validateValidValue(validValues, x);
        }
        return result;
    }

    private static Object validateValidValue(Set validValues, Object newValue)
        throws InvalidPropertyValueException
    {
        if (validValues == null) return newValue; // Any value is allowed
        for (Iterator i = validValues.iterator(); i.hasNext(); ) {
            Range range = (Range) i.next();
            if (range.isInRange(newValue)) return newValue;
        }
        throw new InvalidPropertyValueException("Out of range: " + newValue);
    }
}
