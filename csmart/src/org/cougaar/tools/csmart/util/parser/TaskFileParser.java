/*
 * <copyright>
 *  Copyright 2001 BBNT Solutions, LLC
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

package org.cougaar.tools.csmart.util.parser;

import java.io.IOException;
import java.util.Vector;
import java.util.StringTokenizer;
import java.util.Enumeration;

import org.cougaar.tools.csmart.runtime.ldm.plugin.customer.*;

/**
 * A Simple Parser that parses New Task files.
 * The format for a task file is: <br>
 * <state>,<type>,<rate>,<deviation>,<vitality>,<duration>
 *
 * @author <a href="mailto:bkrisler@bbn.com">Brian Krisler</a>
 */ 
public class TaskFileParser extends GenericParser {

  private Vector tasks = null;

  /** Simple Constructor, initialize the Task Vector to a size of 25. **/  
  public TaskFileParser() {
     tasks = new Vector(25);
  }

  /**
   * @see Parser
   */         
  public void parse() throws IOException {
            
      while( in.ready() ) {
        String line = in.readLine();
        if (line != null && line.trim().length() != 0 && 
              !line.startsWith(commentChar)) {
           parseLine(line);
        }
      }
      in.close();
  }

  /**
   * Parses out all the values for a single task line and
   * creates a NewTask from the values.
   * <br>
   * @param line to be parsed into a new task.
   *
   */
  protected void parseLine(String line) {
     StringTokenizer st = new StringTokenizer(line,separatorChar);
     SingleCustomerTask newTask = new SingleCustomerTask();
     
     newTask.setWorldState(st.nextToken().trim());     
     newTask.setTaskType(st.nextToken().trim());         

     try {
       newTask.setRate((new Long(st.nextToken().trim())).longValue());
     } catch (NumberFormatException nfe) {
       throw new RuntimeException("CustomerPI unable to parse task file line's rate in line: " + line);
     }
     try {
       newTask.setDeviation((new Long(st.nextToken().trim())).longValue());
     } catch (NumberFormatException nfe) {
       throw new RuntimeException("CustomerPI unable to parse task file line's deviation in line: " + line);
     }
     try {
       newTask.setVitality((new Float(st.nextToken().trim())).floatValue());
     } catch (NumberFormatException nfe) {
       throw new RuntimeException("CustomerPI unable to parse task file line's vitality in line: " + line);
     }
     try {
       newTask.setDuration((new Long(st.nextToken().trim())).longValue());       
     } catch (NumberFormatException nfe) {
       throw new RuntimeException("CustomerPI unable to parse task file line's duration in line: " + line);
     }

     tasks.add(newTask);
  }
  
  
  /**
   * Returns all parsed elements.
   * <br>
   * @return Enumeration of all elements
   */
  public Enumeration elements() {
     return tasks.elements();     
  }
   
  /**
   * Dumps the contents of the parsed entires
   */
  public void dump() {
     Enumeration e = elements();
     SingleCustomerTask nt = null;
     while(e.hasMoreElements()) {
        nt = (SingleCustomerTask) e.nextElement();
        
        System.out.println("World State: " + nt.getWorldState());
        System.out.println("Task Type: " + nt.getTaskType());
        System.out.println("Rate: " + nt.getRate());
        System.out.println("Deviation: " + nt.getDeviation());
        System.out.println("Vitality: " + nt.getVitality());
        System.out.println("Duration: " + nt.getDuration());
     }
  }
    
}
