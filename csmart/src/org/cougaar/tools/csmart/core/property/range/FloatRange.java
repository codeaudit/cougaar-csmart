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
 * An implementation of the Range interface for floats.
 */

public class FloatRange extends RangeBase implements Range {

  private float minValue;
  private float maxValue;
  private Float minValueObject;
  private Float maxValueObject;

  public FloatRange(float minValue, float maxValue) {
    this.minValue = minValue;
    this.maxValue = maxValue;
    minValueObject = new Float(minValue);
    maxValueObject = new Float(maxValue);
  }

  /**
   * Get the minimum value of the range. Values are allowed to be
   * equal to the minimum value
   * @return an object having the minimum value
   **/

  public Object getMinimumValue() {
    return minValueObject;
  }

  /**
   * Get the maximum value of the range. Values are allowed to
   * be equal to the maximum value
   * @return an object having the maximum value
   **/

  public Object getMaximumValue() {
    return maxValueObject;
  }

  /**
   * Test if an Object is in this Range
   * @param o the Object to test
   **/
  
  public boolean isInRange(Object o) {
    if (!(o instanceof Float))
      return false;
    float i = ((Float)o).floatValue();
    if (i >= minValue && i <= maxValue)
      return true;
    return false;
  }

  public String toString() {
    return minValue + ":" + maxValue;
  }

}
