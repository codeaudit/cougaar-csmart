/*
 * <copyright>
 * This software is to be used only in accordance with the COUGAAR license
 * agreement. The license agreement and other information can be found at
 * http://www.cougaar.org
 *
 * © Copyright 2001 BBNT Solutions LLC
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
