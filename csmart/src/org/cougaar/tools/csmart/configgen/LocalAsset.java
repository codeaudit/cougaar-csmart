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

/**
 * Represents a LocalAsset.  This is used by the
 * Society Generator and is created from values obtained
 * in both the Template files and the XML Script.
 */
public class LocalAsset  {

  public LocalAsset() {    
  }

  private String assetName;

  /**
   * Get the value of assetName.
   * @return Value of assetName.
   */
  public String getAssetName() {
    return assetName;
  }

  /**
   * Set the value of assetName.
   * @param v  Value to assign to assetName.
   */
  public void setAssetName(String  v) {
    this.assetName = v;
  }

  private long inventoryChaos;

  /**
   * Get the value of InventoryChaos.
   * @return Value of InventoryChaos.
   */
  public long getInventoryChaos() {
    return inventoryChaos;
  }

  /**
   * Set the value of InventoryChaos.
   * @param v  Value to assign to InventoryChaos.
   */
  public void setInventoryChaos(long  v) {
    this.inventoryChaos = v;
  }

  private long timeChaos;

  /**
   * Get the value of timeChaos.
   * @return Value of timeChaos.
   */
  public long getTimeChaos() {
    return timeChaos;
  }

  /**
   * Set the value of timeChaos.
   * @param v  Value to assign to timeChaos.
   */
  public void setTimeChaos(long  v) {
    this.timeChaos = v;
  }

  private long depleteFactor;

  /**
   * Get the value of DepleteFactor.
   * @return Value of DepleteFactor.
   */
  public long getDepleteFactor() {
    return depleteFactor;
  }

  /**
   * Set the value of DepleteFactor.
   * @param v  Value to assign to DepleteFactor.
   */
  public void setDepleteFactor(long v) {
    this.depleteFactor = v;
  }

  private long avgCompleteTime;

  /**
   * Get the value of avgCompleteTime.
   * @return Value of avgCompleteTime.
   */
  public long getAvgCompleteTime() {
    return avgCompleteTime;
  }

  /**
   * Set the value of avgCompleteTime.
   * @param v  Value to assign to avgCompleteTime.
   */
  public void setAvgCompleteTime(long v) {
    this.avgCompleteTime = v;
  }

  private Object roles;

  /**
   * Get the roles, which is either <tt>null</tt>, a <code>String</code>,
   * or a <code>List</code> of Strings.
   */
  public Object getRoles() {
    return roles;
  }

  /**
   * Set the value of roles.
   */
  public void setRoles(Object o) {
    this.roles = o;
  }

} // LocalAsset
