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
package org.cougaar.tools.csmart.recipe;


/**
 * Simple Class used by the UI to obtain all the Recipe Names and classes.
 * Whenever a new recipe is added, its name and class must be added here
 * or it will not appear in the UI.
 *
 * Created: Wed Mar 27 10:37:01 2002
 *
 */
public class RecipeList {

  // Add any new recipes to this array.
  private static final NameClassPair[] recipes = {
    new NameClassPair("Basic Metric", BasicMetric.class),
    new NameClassPair("Specific Insertion", SpecificInsertionRecipe.class),
    new NameClassPair("Parameter Insertion", ParameterInsertionRecipe.class),
    new NameClassPair("Servlet Group Insertion", ServletGroupInsertionRecipe.class),
    new NameClassPair("Adaptivity Support", AdaptivitySupportRecipe.class),
    new NameClassPair("Complete Agent", CompleteAgentRecipe.class),
    new NameClassPair("Component Collection", ComponentCollectionRecipe.class),
    //    new NameClassPair("Switch Plugin", SwitchPluginRecipe.class),
  };

  public static final String[] getRecipeNames() {
    String[] names = new String[recipes.length];

    for(int i=0; i < recipes.length; i++) {
      names[i] = recipes[i].name;
    }
    
    return names;
  }    

  public static final Class[] getRecipeClasses() {
    Class[] classes = new Class[recipes.length];

    for(int i=0; i < recipes.length; i++) {
      classes[i] = recipes[i].cls;
    }
    
    return classes;
  }    

  public static final int getRecipeCount() {
    return recipes.length;
  }

  public static final String getRecipeName(int i) 
    throws ArrayIndexOutOfBoundsException {
    if(i < 0 || i > recipes.length) {
      throw new ArrayIndexOutOfBoundsException(i);
    }
    return recipes[i].name;
  }

  public static final Class getRecipeClass(int i) 
    throws ArrayIndexOutOfBoundsException {
    if(i < 0 || i > recipes.length) {
      throw new ArrayIndexOutOfBoundsException(i);
    }
    return recipes[i].cls;
  }

  private static final class NameClassPair {
    public String name;
    public Class cls;
    public NameClassPair(String name, Class cls) {
      this.cls = cls;
      this.name = name;
    }
    public String toString() {
      return name;
    }
  }

}// RecipeList
