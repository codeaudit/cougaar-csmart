/*
 * <copyright>
 *  Copyright 2000-2002 BBNT Solutions, LLC
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
package org.cougaar.tools.csmart.society;

import org.cougaar.tools.csmart.core.property.ModifiableComponent;

/**
 * A simple interface implemented by configurable components 
 * that represent assets. <br>
 */
public interface AssetComponent extends ModifiableComponent {
  /** Some default Property Definitions **/

  /** Property Type Definition **/
  String PROP_TYPE = "Asset Type";

  /** Property Type Description Definition **/
  String PROP_TYPE_DESC = "Type of Asset";

  /** Property Class Definition **/
  String PROP_CLASS = "Asset Class";

  /** Property Class Description Definition **/
  String PROP_CLASS_DESC = "Class of the Asset";

  /** Property UID Definition **/
  String PROP_UID = "UID";

  /** Property UID Description Definition **/
  String PROP_UID_DESC = "UID of the Asset";

  /** Property Unitname Definition **/
  String PROP_UNITNAME = "Unit Name";

  /** Property Unitname Description **/
  String PROP_UNITNAME_DESC = "Unit Name of the Asset";

  /** Property UIC Definition **/
  String PROP_UIC = "UIC";

  /** Property UIC Description Definition **/
  String PROP_UIC_DESC = "UIC of the Asset";

}
