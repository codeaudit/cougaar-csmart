/**
 * <copyright>
 *  
 *  Copyright 2002-2004 BBNT Solutions, LLC
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
package org.cougaar.tools.csmart.society;

import org.cougaar.tools.csmart.core.cdata.RelationshipData;
import org.cougaar.tools.csmart.core.property.ModifiableConfigurableComponent;
import org.cougaar.tools.csmart.core.property.Property;
import org.cougaar.tools.csmart.ui.viewer.CSMART;
import org.cougaar.util.log.Logger;

import java.text.DateFormat;
import java.util.Date;

/**
 * RelationshipBase.java
 *
 * A component Representing a relationship.
 * In a prototype-dat.ini file, a relationship is:
 * <role> <item> <type> <otherCluster> <start time> <stop time>
 *
 * Created: Fri Feb 22 12:32:59 2002
 *
 * @author <a href="mailto:bkrisler@bbn.com">Brian Krisler</a>
 * @version 1.0
 */

public class RelationshipBase 
  extends ModifiableConfigurableComponent
  implements RelationshipComponent {
  private transient Logger log;

  public static final String PROP_TYPE = "Type";
  public static final String PROP_TYPE_DESC = "Type of Relationship";

  public static final String PROP_ROLE = "Role";
  public static final String PROP_ROLE_DESC = "Roles performed by this relationship";

  public static final String PROP_ITEM = "Item Id";
  public static final String PROP_ITEM_DESC = "Item Identification";

  public static final String PROP_TYPEID = "Type Id";
  public static final String PROP_TYPEID_DESC = "Type Identification";

  public static final String PROP_SUPPORTED = "Supported Agent";
  public static final String PROP_SUPPORTED_DESC = "Agent Supported by this relationship";

  public static final String PROP_STARTTIME = "Start Time";
  public static final String PROP_STARTTIME_DESC = "Time Relationship starts";

  public static final String PROP_STOPTIME = "Stop Time";
  public static final String PROP_STOPTIME_DESC = "Time Relationship ends";

  private Property propType;
  private Property propRole;
  private Property propItem;
  private Property propTypeId;
  private Property propSupported;
  private Property propStartTime;
  private Property propStopTime;
  private RelationshipData relationship;

  public RelationshipBase (RelationshipData relationship) {
    super(relationship.getSupported());
    createLogger();
    this.relationship = relationship;
  }

  private void createLogger() {
    log = CSMART.createLogger(this.getClass().getName());
  }

  // TODO: Roles are defined in org.cougaar.glm.ldm,
  // which this doesn't build with -- should they be
  // added here as allowedValues?
  public void initProperties() {
    if (relationship == null) { 
      if (log.isErrorEnabled()) {
	log.error("Report under bug 1304: " + this + " using CSMART " + CSMART.writeDebug() + " got null relationship to create from", new Throwable());
      }
    } else if (relationship.getType() == null) {
      if (log.isErrorEnabled()) {
	log.error("Report under bug 1304: " + this + " using CSMART " + CSMART.writeDebug() + " got null relationship type to create from in relationship: " + relationship, new Throwable());
      }
    }
    propType = addProperty(PROP_TYPE, relationship.getType());
    propType.setToolTip(PROP_TYPE_DESC);

    if (getProperty(PROP_TYPE) == null && log.isErrorEnabled()) {
      log.error("Please report bug 1304: Using CSMART " + CSMART.writeDebug() + " just finished setting Type and getProperty(type) returns null. Initializing from RelationshipData: " + relationship, new Throwable());
    }

    propRole = addProperty(PROP_ROLE, relationship.getRole());
    propRole.setToolTip(PROP_ROLE_DESC);
    propItem = addProperty(PROP_ITEM, relationship.getItemId());
    propItem.setToolTip(PROP_ITEM_DESC);
    propTypeId = addProperty(PROP_TYPEID, relationship.getTypeId());
    propTypeId.setToolTip(PROP_TYPEID_DESC);
    propSupported = addProperty(PROP_SUPPORTED, relationship.getSupported());
    propSupported.setToolTip(PROP_SUPPORTED_DESC);

    DateFormat format = DateFormat.getInstance();
    propStartTime = addProperty(PROP_STARTTIME,
                             format.format(new Date(relationship.getStartTime())));
    propStartTime.setToolTip(PROP_STARTTIME_DESC);

    propStopTime = addProperty(PROP_STOPTIME,
                            format.format(new Date(relationship.getEndTime())));
    propStopTime.setToolTip(PROP_STOPTIME_DESC);
  }

}// RelationshipBase
