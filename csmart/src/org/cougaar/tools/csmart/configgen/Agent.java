/*
 * <copyright>
 * This software is to be used only in accordance with the COUGAAR license
 * agreement. The license agreement and other information can be found at
 * http://www.cougaar.org
 *
 * © Copyright 2001 BBNT Solutions LLC
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
