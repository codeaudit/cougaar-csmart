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
