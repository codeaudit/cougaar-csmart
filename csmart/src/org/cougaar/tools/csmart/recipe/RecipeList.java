/**
 * <copyright>
 *  Copyright 2002 BBNT Solutions, LLC
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
 *  </copyright>
 */
package org.cougaar.tools.csmart.recipe;

import java.lang.ArrayIndexOutOfBoundsException;


/**
 * RecipeList.java
 *
 * Simple Class used by the UI to obtain all the Recipe Names and classes.
 * Whenever a new recipe is added, its name and class must be added here
 * or it will not appear in the UI.
 *
 * Created: Wed Mar 27 10:37:01 2002
 *
 * @author <a href="mailto:bkrisler@bbn.com">Brian Krisler</a>
 * @version 1.0
 */
public class RecipeList {

  // Add any new recipes to this array.
  private static final NameClassPair[] recipes = {
    new NameClassPair("Basic Metric", BasicMetric.class),
    new NameClassPair("Component Insertion", ComponentInsertionRecipe.class),
    new NameClassPair("Specific Insertion", SpecificInsertionRecipe.class),
    new NameClassPair("Agent Insertion", AgentInsertionRecipe.class),
    new NameClassPair("Parameter Insertion", ParameterInsertionRecipe.class),
    new NameClassPair("Servlet Group Insertion", ServletGroupInsertionRecipe.class),
    new NameClassPair("Adaptivity Support", AdaptivitySupportRecipe.class),
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
