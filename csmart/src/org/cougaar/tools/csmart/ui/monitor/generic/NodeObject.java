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

package org.cougaar.tools.csmart.ui.monitor.generic;

import org.cougaar.util.PropertyTree;

import java.util.Vector;

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

  String getUID();

  String getLabel();

  String getToolTip();

  /**
   * The string that will be used to determine the color of the 
   * node; for example, the name of a community or agent.
   */
  String getColor();

  /**
   * Return null to mean "use same color as fill color".
   * Currently unused.
   */
  String getBorderColor();

  String getShape();

  /**
   * The number of sides.  A returned value of "0" is ignored (i.e. the
   * default is used).
   */
  String getSides();

  /**
   * The width aspect ratio.  A returned value of "0" is ignored 
   * (i.e. the default aspect ratio is used).
   */
  String getWidth();

  /**
   * The height aspect ratio.  A returned value of "0" is ignored 
   * (i.e. the default aspect ratio is used).
   */
  String getHeight();

  /**
   * The distortion.  A returned value of "0" is ignored (i.e. the
   * default is used).
   */
  String getDistortion();

  /**
   * The orientation.  A returned value of "0" is ignored (i.e. the
   * default is used).
   */
  String getOrientation();

  /**
   * The orientation.  A returned value of "normal" is ignored (i.e. the
   * default is used).
   */
  String getFontStyle();

  /**
   * Return a PropertyTree containing properties (names/values)
   * associated with this node.  Note that names must not
   * have spaces (but values can have spaces).
   * By convention, attribute names are defined with underbars in place
   * of spaces, and the underbars are removed by the table model
   * attached to the JTable that displays the attributes.
   * @see CSMARTFrame#getAttributeTableModel
   */
  PropertyTree getProperties();

  /**
   * Return a vector of the nodes at the tail ends of
   * links coming in to this node.  The nodes are identified by UID (i.e.
   * the vector returned is a vector of Strings which are UIDs as defined
   * by the getUID method in this interface).
   * Return null if there are no links.
   */
  Vector getIncomingLinks();

  /**
   * Return a vector of the nodes at the head ends of
   * links going out of this node.  The nodes are identified by UID (i.e.
   * the vector returned is a vector of Strings which are UIDs as defined
   * by the getUID method in this interface).
   * Return null if there are no links.
   */
  Vector getOutgoingLinks();

  /**
   * Return a vector of the nodes at the ends of bidirectional links.
   * The nodes are identified by UID (i.e.
   * the vector returned is a vector of Strings which are UIDs as defined
   * by the getUID method in this interface).
   * Return null if there are no links.
   */
  Vector getBidirectionalLinks();

  boolean isVisible();
}

