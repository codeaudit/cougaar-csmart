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
package org.cougaar.tools.csmart.society;

import org.cougaar.tools.csmart.core.property.ModifiableComponent;

/**
 * A simple interface implemented by configurable components 
 * that represent assets. <br>
 */
public interface AssetComponent extends ModifiableComponent {
  /** Some default Property Definitions **/

  /** Property Type Definition **/
  //  String PROP_TYPE = "Asset Type";

  /** Property Type Description Definition **/
  //  String PROP_TYPE_DESC = "Type of Asset";

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
