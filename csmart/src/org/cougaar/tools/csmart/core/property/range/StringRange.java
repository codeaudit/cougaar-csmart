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

package org.cougaar.tools.csmart.core.property.range;

/**
 * Implementation of Range for Strings.  Note that a StringRange
 * always has a single String value.
 */

public class StringRange extends RangeBase implements Range {
  static final long serialVersionUID = -7351616054609766392L;

  private String value;

  public StringRange(String value) {
    this.value = value;
  }

  /**
   * Get the minimum value of the range. Values are allowed to be
   * equal to the minimum value
   * @return an object having the minimum value
   **/

  public Object getMinimumValue() {
    return value;
  }

  /**
   * Get the maximum value of the range. Values are allowed to
   * be equal to the maximum value
   * @return an object having the maximum value
   **/

  public Object getMaximumValue() {
    return value;
  }

  /**
   * Test if an Object is in this Range
   * @param o the Object to test
   **/
  
  public boolean isInRange(Object o) {
    return value.equals(o);
  }

  public String toString() {
    return value;
  }

  /**
   * Two StringRanges are equal if their values are equal.
   * Also, A StringRange is equal to a String if the value equals the String
   *
   * @param o an <code>Object</code> to compare
   * @return a <code>boolean</code>, true if the value is equal
   */
  public boolean equals(Object o) {
    if (o instanceof StringRange) {
      StringRange other = (StringRange)o;
      if (value != null && value.equals(other.getMinimumValue()))
	return true;
      if (value == null && other.getMinimumValue() == null)
	return true;
    } else if (o instanceof String) {
      String so = (String)o;
      if (value != null && value.equals(so))
	return true;
      if (value == null && so == null)
	return true;
    }
    return false;
  }
      
}

