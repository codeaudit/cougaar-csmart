/* 
 * <copyright>
 * Copyright 2001-2002 BBNT Solutions, LLC
 * under sponsorship of the Defense Advanced Research Projects Agency (DARPA).

 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the Cougaar Open Source License as published by
 * DARPA on the Cougaar Open Source Website (www.cougaar.org).

 * THE COUGAAR SOFTWARE AND ANY DERIVATIVE SUPPLIED BY LICENSOR IS
 * PROVIDED 'AS IS' WITHOUT WARRANTIES OF ANY KIND, WHETHER EXPRESS OR
 * IMPLIED, INCLUDING (BUT NOT LIMITED TO) ALL IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE, AND WITHOUT
 * ANY WARRANTIES AS TO NON-INFRINGEMENT.  IN NO EVENT SHALL COPYRIGHT
 * HOLDER BE LIABLE FOR ANY DIRECT, SPECIAL, INDIRECT OR CONSEQUENTIAL
 * DAMAGES WHATSOEVER RESULTING FROM LOSS OF USE OF DATA OR PROFITS,
 * TORTIOUS CONDUCT, ARISING OUT OF OR IN CONNECTION WITH THE USE OR
 * PERFORMANCE OF THE COUGAAR SOFTWARE.
 * </copyright>
 */
package org.cougaar.tools.csmart.core.db;

public interface DBConflictHandler {
  Object KEEP_CHOICE = "Keep Existing Definition";
  Object OVERWRITE_CHOICE = "Overwrite";
  Object KEEP_ALL_CHOICE = "Keep All";
  Object OVERWRITE_ALL_CHOICE = "Overwrite All";
  Object[] STANDARD_CHOICES = {
    KEEP_CHOICE,
    OVERWRITE_CHOICE,
    KEEP_ALL_CHOICE,
    OVERWRITE_ALL_CHOICE
  };
  int KEEP = 0;
  int OVERWRITE = 1;
  int KEEP_ALL = 2;
  int OVERWRITE_ALL = 3;

  int handleConflict(Object msg, Object[] options, Object defaultOption);
}
