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

package org.cougaar.tools.csmart.recipe;

import org.cougaar.tools.csmart.core.property.ModifiableComponent;
import org.cougaar.tools.csmart.society.AgentComponent;

/**
 * Parent interface for all Recipes to a CSMART system.
 * Recipes may have Agents, and may need to be able to write
 * out information in the various Nodes. These interfaces are
 * exposed.
 */
public interface RecipeComponent extends ModifiableComponent {

  String CLASS_NAME_PROP = "recipe_class_name";
  String RECIPE_QUERY_FILE= "recipeQueries.q";

  String getRecipeName();

  /**
   * Get the agents, both assigned and unassigned.
   * @return array of agent components
   */
  AgentComponent[] getAgents();

  /**
   * Save the recipe to the database.
   * @return boolean true if save was successful
   */
  boolean saveToDatabase();

  /**
   * Return true if recipe is different than in the database.
   * @return boolean true if recipe is different than in database
   */
  boolean isModified();
}




