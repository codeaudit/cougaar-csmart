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

package org.cougaar.tools.csmart.configgen;

// could load this class dynamically
import org.cougaar.tools.csmart.configgen.community.SocietyBuilderImpl;

/**
 * Build and output all the society files.
 * @deprecated Use CSMART
 */
public class GenerateSociety  {

  public static void main(String[] args) {
    try {
      // could load this class dynamically
      //   - currently uses community-based templates
      //   - other dynamically-loaded implementations are possible
      //     e.g. randomly wire together agents
      SocietyBuilder sIn = new SocietyBuilderImpl();

      sIn.initialize(args);

      // could load this class dynamically
      //   - currently writes lots of files (.ini, .dat, etc)
      //   - other dynamically-loaded implementations are possible
      //     e.g. write to database
      SocietyWriter sOut = new SocietyWriterImpl();

      sOut.initialize(args);

      // build and configure the society
      Society soc = sIn.build();
      if (soc == null) {
        return;
      }

      // write the society
      sOut.write(soc);

      System.out.println("Successfully created society files.");
    } catch (Exception e) {
      System.err.println("Unable to create files for society:");
      int nargs = args.length;
      System.err.println("Args["+nargs+"]:");
      for (int i = 0; i < nargs; i++) {
        System.err.println("  "+i+") "+args[i]);
      }
      System.err.println("Exception:");
      e.printStackTrace();
    }
  }

}
