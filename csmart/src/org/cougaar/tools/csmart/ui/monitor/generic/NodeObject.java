/*
 * <copyright>
 *  Copyright 2000-2001 BBNT Solutions, LLC
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

package org.cougaar.tools.csmart.ui.monitor.generic;

import java.util.*;

import org.cougaar.util.PropertyTree;

/**
 * Interface that must be implemented for an object to be graphed.
 * For documentation on available shapes, and the meaning and defaults
 * for width/height aspect ratios, distortion, orientation, and font style,
 * see the grappa documentation.
 */

public interface NodeObject {

  /**
   * Return a unique identifier for this node.
   */

  public String getUID();

  public String getLabel();

  public String getToolTip();

  /**
   * The string that will be used to determine the color of the 
   * node; for example, the name of a community or agent.
   */

  public String getColor();

  /**
   * Return null to mean "use same color as fill color".
   * Currently unused.
   */

  public String getBorderColor();

  public String getShape();

  /**
   * The number of sides.  A returned value of "0" is ignored (i.e. the
   * default is used).
   */

  public String getSides();

  /**
   * The width aspect ratio.  A returned value of "0" is ignored 
   * (i.e. the default aspect ratio is used).
   */

  public String getWidth();

  /**
   * The height aspect ratio.  A returned value of "0" is ignored 
   * (i.e. the default aspect ratio is used).
   */

  public String getHeight();

  /**
   * The distortion.  A returned value of "0" is ignored (i.e. the
   * default is used).
   */

  public String getDistortion();

  /**
   * The orientation.  A returned value of "0" is ignored (i.e. the
   * default is used).
   */

  public String getOrientation();

  /**
   * The orientation.  A returned value of "normal" is ignored (i.e. the
   * default is used).
   */

  public String getFontStyle();

  /**
   * Return a PropertyTree containing properties (names/values)
   * associated with this node.  Note that names must not
   * have spaces (but values can have spaces).
   * By convention, attribute names are defined with underbars in place
   * of spaces, and the underbars are removed by the table model
   * attached to the JTable that displays the attributes.
   * @see CSMARTFrame#getAttributeTableModel
   */

  public PropertyTree getProperties();

  /**
   * Return a vector of the nodes at the tail ends of
   * links coming in to this node.  The nodes are identified by UID (i.e.
   * the vector returned is a vector of Strings which are UIDs as defined
   * by the getUID method in this interface).
   * Return null if there are no links.
   */

  public Vector getIncomingLinks();

  /**
   * Return a vector of the nodes at the head ends of
   * links going out of this node.  The nodes are identified by UID (i.e.
   * the vector returned is a vector of Strings which are UIDs as defined
   * by the getUID method in this interface).
   * Return null if there are no links.
   */

  public Vector getOutgoingLinks();

  /**
   * Return a vector of the nodes at the ends of bidirectional links.
   * The nodes are identified by UID (i.e.
   * the vector returned is a vector of Strings which are UIDs as defined
   * by the getUID method in this interface).
   * Return null if there are no links.
   */

  public Vector getBidirectionalLinks();

  public boolean isVisible();

}

