/*
 * <copyright>
 *  
 *  Copyright 2000-2004 BBNT Solutions, LLC
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

import org.cougaar.tools.csmart.core.property.range.Range;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;

public class PropertyHelper {
  private static final Class[] stringArgType = {String.class};


  public PropertyHelper() {
  }

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
    //    Logger log = CSMART.createLogger("org.cougaar.tools.csmart.core.property.PropertyHelper");

 //    if(log.isDebugEnabled()) {
//        log.debug("validateValue(Class, Set, Object) \n" +
//                 cls + "\n"  +
//                 "Set: " + validValues + "\n"+
//                 "Object: " + newValue + "\n" +
//                 "Object Class: " + newValue.getClass());
//     }
    if (cls == null) return newValue;
    if (cls.isArray()) {
      if (!newValue.getClass().isArray()) {
        newValue = convertStringToArray(newValue.toString());
      }
      Class cclass = cls.getComponentType();
      if (cclass == Integer.TYPE) {
        return validateIntArrayValue(validValues, newValue);
      }
      if (cclass == String.class) {
        return validateStringArrayValue(validValues, newValue);
      }
      return newValue;
    } else if (cls.equals(ArrayList.class)) {
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
