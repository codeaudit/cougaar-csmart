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
package org.cougaar.tools.csmart.core.cdata;

import java.io.Serializable;

/**
 * Represents a Relationship for an asset.
 * All relationships contain three values:
 * <ul>
 * <li> Type:  This is the type of relationship </li>
 * <li> Item:  This is Agent we are forming a relationship with </li>
 * <li> Role: This is a role performed by this asset. </li>
 * </ul>
 *
 * There are two type of ini file formats: <br>
 * "Supporting"    "1BDE"   "StrategicTransportProvider" <br>
 * Where the fields are: <br>
 * <Type>   <Supported Cluster>  <Role>
 * <br>and<br>
 * "StrategicTransportProvider" "UIC/1BDE" "UTC/RTOrg" "1BDE" "" "" <br>
 * Where the fields are: <br>
 * <Type> <ItemId> <TypeId> <Supported Cluster> <Start Time> <Stop Time>
 *
 * @author <a href="mailto:bkrisler@bbn.com">Brian Krisler</a>
 * @version 1.0
 */
public class RelationshipData implements Serializable {
  
  private String type = "";
  private String role = "";
  private String itemId = "";
  private String typeId = "";
  private long startTime = 0L;
  private long endTime = 0L;
  private String supported = "";

  /** Types of relationships **/
  // (not really important if everything else is correct)
  public static final String SUPERIOR = "Superior";
  public static final String SUPPORTING = "Supporting";

  /**
   * Creates a new <code>RelationshipData</code> instance.
   *
   */
  public RelationshipData() {
  }

  /**
   * Sets the type of relationship
   *
   * @param type of relationship
   */
  public void setType(String type) {
    if (type == null) 
      throw new IllegalArgumentException("Attempt to set Type to null for RelationshipData: " + this);

    this.type = type;
  }

  /**
   * Gets the type of relationship.
   *
   * @return relationship type.
   */
  public String getType() {
    return this.type;
  }

  /**
   * Sets the ItemId of a relationship, this
   * is usually the agent name we are forming
   * a relationship with.
   *
   * @param itemId name
   */
  public void setItemId(String itemId) {
    this.itemId = itemId;
  }

  /**
   * Gets the ItemId for this relationship
   *
   * @return an itemId name
   */
  public String getItemId() {
    return this.itemId;
  }

  public void setTypeId(String typeId) {
    this.typeId = typeId;
  }

  public String getTypeId() {
    return this.typeId;
  }

  /**
   * Sets a single role for this relationship
   *
   * @param role performed by this asset.
   */
  public void setRole(String role) {
    this.role = role;
  }

  /**
   * Gets a single role performed by this asset.
   *
   * @return role
   */
  public String getRole() {
    return this.role;
  }

  /**
   * Sets the start time for the time-phased object
   *
   * @param start 
   */
  public void setStartTime(long start) {
    this.startTime = start;
  }

  /**
   * Sets the End time for the time-phased object.
   *
   * @param end 
   */
  public void setEndTime(long end) {
    this.endTime = end;
  }

  /**
   * Gets the Start Time for the time
   * phased object
   *
   * @return start time.
   */
  public long getStartTime() {
    return startTime;
  }

  /**
   * Gets the End time for the time 
   * phased object. 
   *
   * @return end time
   */
  public long getEndTime() {
    return endTime;
  }
  
  /**
   * Sets the agent supported by this relationship
   *
   * @param supported agent
   */
  public void setSupported(String supported) {
     this.supported = supported;
   }
 
  /**
   * Gets the agent supported by this relationship
   *
   * @return agent
   */
  public String getSupported() {
     return this.supported;
   }

  /**
   * For debugging, get contents of relationship data.
   * @return String fields in relationship data
   */
  public String toString() {
    StringBuffer sb = new StringBuffer(300);
    sb.append("Type: " + type);
    sb.append(" Role: " + role);
    sb.append(" ItemId: " + itemId);
    sb.append(" TypeId: " + typeId);
    sb.append(" Start Time: " + startTime);
    sb.append(" End Time: " + endTime);
    sb.append(" Supported: " + supported);
    return sb.toString();
  }
}
