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

package org.cougaar.tools.csmart.configgen;

import java.util.List;
import java.util.Map;

/**
 * Represents an Agent.
 *
 * All immutable "getters" from the SocietyWriter's point of view.
 */
public interface Agent  {
  
  /** 
   * @return the name of this <code>Agent</code>. 
   */
  public String getName();

  /**
   * Roles are:<pre>
   *  - null
   *  - a String
   *  - a List of Strings, each unique within the List</pre>.
   *
   * @see MergeUtils
   */
  public Object getRoles();

  public float getLatitude();

  public float getLongitude();

  public long getStartMillis();

  public long getStopMillis();

  public long getSniffInterval();

  public long getCustomerDemand();

  /**
   * Return a <code>List</code> of <code>CustomerTask</code>s.
   */
  public List getCustomerTasks();

  public long getLocalProduction();

  /**
   * Return a <code>List</code> of <code>LocalAsset</code>s.
   */
  public List getLocalAssets();
  
  /**
   * Return a <code>Map</code> of <pre>
   *   (<code>String</code> task verb,
   *    <code>Object</code>roles)</pre>.
   */
  public Map getAllocationTable();

  /**
   * Return a <code>List</code> of "supported" <code>Agent</code> names.
   */
  public List getSupportedAgentNames();

} // Agent
