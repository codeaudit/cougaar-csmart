/*
 * <copyright>
 *  
 *  Copyright 2001-2004 BBNT Solutions, LLC
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

package org.cougaar.tools.csmart.ui.monitor.xml;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;

import java.util.Vector;

/**
 * Parse an XML document using a SAX Parser and convert elements into
 * NodeObjects for graphing with the Graphviz tools.
 */

public class XMLHandler extends DefaultHandler {
  // feature ids -- unlcear which of these we need

  /** Namespaces feature id (http://xml.org/sax/features/namespaces). */
  protected static final String NAMESPACES_FEATURE_ID = "http://xml.org/sax/features/namespaces";

  /** Namespace prefixes feature id (http://xml.org/sax/features/namespace-prefixes). */
  protected static final String NAMESPACE_PREFIXES_FEATURE_ID = "http://xml.org/sax/features/namespace-prefixes";

  /** Validation feature id (http://xml.org/sax/features/validation). */
  protected static final String VALIDATION_FEATURE_ID = "http://xml.org/sax/features/validation";

  /** Schema validation feature id (http://apache.org/xml/features/validation/schema). */
  protected static final String SCHEMA_VALIDATION_FEATURE_ID = "http://apache.org/xml/features/validation/schema";

  /** Schema full checking feature id (http://apache.org/xml/features/validation/schema-full-checking). */
  protected static final String SCHEMA_FULL_CHECKING_FEATURE_ID = "http://apache.org/xml/features/validation/schema-full-checking";

  /** Dynamic validation feature id (http://apache.org/xml/features/validation/dynamic). */
  protected static final String DYNAMIC_VALIDATION_FEATURE_ID = "http://apache.org/xml/features/validation/dynamic";

  // default settings

  /** Default parser name. */
  protected static final String DEFAULT_PARSER_NAME = "org.apache.xerces.parsers.SAXParser";

  /** Default namespaces support (true). */
  protected static final boolean DEFAULT_NAMESPACES = true;

  /** Default namespace prefixes (false). */
  protected static final boolean DEFAULT_NAMESPACE_PREFIXES = false;

  /** Default validation support (false). */
  protected static final boolean DEFAULT_VALIDATION = false;

  /** Default Schema validation support (false). */
  protected static final boolean DEFAULT_SCHEMA_VALIDATION = false;

  /** Default Schema full checking support (false). */
  protected static final boolean DEFAULT_SCHEMA_FULL_CHECKING = false;

  /** Default dynamic validation support (false). */
  protected static final boolean DEFAULT_DYNAMIC_VALIDATION = false;

  /** Default memory usage report (false). */
  protected static final boolean DEFAULT_MEMORY_USAGE = false;

  /** Default "tagginess" report (false). */
  protected static final boolean DEFAULT_TAGGINESS = false;

  private Vector nodeObjects = new Vector();

  public XMLHandler() {
  }

  /**
   * Return vector of node objects that were built from the XML document.
   */

  public Vector getNodeObjects() {
    return nodeObjects;
  }


  /** Start document. */
  public void startDocument() throws SAXException {
  }

  /** Start element. */
  public void startElement(String uri, String local, String raw,
                           Attributes attrs) throws SAXException {
//     System.out.println("URI: " + uri + " Local: " + local + " Raw: " + raw);
//     int n = attrs.getLength();
//     for (int i = 0; i < n; i++)
//       System.out.println(attrs.getLocalName(i) + " " + attrs.getValue(i));
    nodeObjects.add(new XMLNode(local, attrs));
  }

  /** Characters. */
  public void characters(char ch[],
                         int start, int length) throws SAXException {
  }

  /** Ignorable whitespace. */
  public void ignorableWhitespace(char ch[], int start, int length)
    throws SAXException {
  }

  /** Processing instruction. */
  public void processingInstruction(String target, String data)
    throws SAXException {
  }

  //
  // ErrorHandler methods
  //

  /** Warning. */
  public void warning(SAXParseException ex) throws SAXException {
    printError("Warning", ex);
  }

  /** Error. */
  public void error(SAXParseException ex) throws SAXException {
    printError("Error", ex);
  }

  /** Fatal error. */
  public void fatalError(SAXParseException ex) throws SAXException {
    printError("Fatal Error", ex);
  }

  /** Prints the error message. */
  protected void printError(String type, SAXParseException ex) {
    System.err.print("[");
    System.err.print(type);
    System.err.print("] ");
    if (ex== null) {
      System.out.println("!!!");
    }
    String systemId = ex.getSystemId();
    if (systemId != null) {
      int index = systemId.lastIndexOf('/');
      if (index != -1)
        systemId = systemId.substring(index + 1);
      System.err.print(systemId);
    }
    System.err.print(':');
    System.err.print(ex.getLineNumber());
    System.err.print(':');
    System.err.print(ex.getColumnNumber());
    System.err.print(": ");
    System.err.print(ex.getMessage());
    System.err.println();
    System.err.flush();

  } // printError(String,SAXParseException)

  public static void main(String argv[]) {

    // is there anything to do?
    if (argv.length == 0) {
      printUsage();
      System.exit(1);
    }

    // variables
    XMLHandler xmlHandler = new XMLHandler();
    XMLReader parser = null;

    // unclear which of these are needed
    boolean namespaces = DEFAULT_NAMESPACES;
    boolean namespacePrefixes = DEFAULT_NAMESPACE_PREFIXES;
    boolean validation = DEFAULT_VALIDATION;
    boolean schemaValidation = DEFAULT_SCHEMA_VALIDATION;
    boolean schemaFullChecking = DEFAULT_SCHEMA_FULL_CHECKING;
    boolean dynamicValidation = DEFAULT_DYNAMIC_VALIDATION;
    boolean memoryUsage = DEFAULT_MEMORY_USAGE;
    boolean tagginess = DEFAULT_TAGGINESS;

    // process arguments
    for (int i = 0; i < argv.length; i++) {
      String arg = argv[i];
      if (arg.startsWith("-")) {
        String option = arg.substring(1);
        if (option.equals("p")) {
          // get parser name
          if (++i == argv.length) {
            System.err.println("error: Missing argument to -p option.");
            continue;
          }
          String parserName = argv[i];

          // create parser
          try {
            parser = XMLReaderFactory.createXMLReader(parserName);
          }
          catch (Exception e) {
            try {
              parser = null;
              System.err.println("error: Unable to instantiate parser ("+parserName+")");
            }
            catch (Exception ex) {
              parser = null;
              System.err.println("error: Unable to instantiate parser ("+parserName+")");
            }
          }
          continue;
        }
        if (option.equalsIgnoreCase("n")) {
          namespaces = option.equals("n");
          continue;
        }
        if (option.equalsIgnoreCase("np")) {
          namespacePrefixes = option.equals("np");
          continue;
        }
        if (option.equalsIgnoreCase("v")) {
          validation = option.equals("v");
          continue;
        }
        if (option.equalsIgnoreCase("s")) {
          schemaValidation = option.equals("s");
          continue;
        }
        if (option.equalsIgnoreCase("f")) {
          schemaFullChecking = option.equals("f");
          continue;
        }
        if (option.equalsIgnoreCase("dv")) {
          dynamicValidation = option.equals("dv");
          continue;
        }
        if (option.equalsIgnoreCase("m")) {
          memoryUsage = option.equals("m");
          continue;
        }
        if (option.equalsIgnoreCase("t")) {
          tagginess = option.equals("t");
          continue;
        }
        if (option.equals("-rem")) {
          if (++i == argv.length) {
            System.err.println("error: Missing argument to -# option.");
            continue;
          }
          System.out.print("# ");
          System.out.println(argv[i]);
          continue;
        }
        if (option.equals("h")) {
          printUsage();
          continue;
        }
        System.err.println("error: unknown option ("+option+").");
        continue;
      }

      // use default parser?
      if (parser == null) {
        // create parser
        try {
          parser = XMLReaderFactory.createXMLReader(DEFAULT_PARSER_NAME);
        }
        catch (Exception e) {
          System.err.println("error: Unable to instantiate parser ("+DEFAULT_PARSER_NAME+")");
          continue;
        }
      }

      // set parser features -- unclear which of these are needed
      try {
        parser.setFeature(NAMESPACES_FEATURE_ID, namespaces);
      }
      catch (SAXException e) {
        System.err.println("warning: Parser does not support feature ("+NAMESPACES_FEATURE_ID+")");
      }
      try {
        parser.setFeature(NAMESPACE_PREFIXES_FEATURE_ID, namespacePrefixes);
      }
      catch (SAXException e) {
        System.err.println("warning: Parser does not support feature ("+NAMESPACE_PREFIXES_FEATURE_ID+")");
      }
      try {
        parser.setFeature(VALIDATION_FEATURE_ID, validation);
      }
      catch (SAXException e) {
        System.err.println("warning: Parser does not support feature ("+VALIDATION_FEATURE_ID+")");
      }
      try {
        parser.setFeature(SCHEMA_VALIDATION_FEATURE_ID, schemaValidation);
      }
      catch (SAXNotRecognizedException e) {
        // ignore
      }
      catch (SAXNotSupportedException e) {
        System.err.println("warning: Parser does not support feature ("+SCHEMA_VALIDATION_FEATURE_ID+")");
      }
      try {
        parser.setFeature(SCHEMA_FULL_CHECKING_FEATURE_ID, schemaFullChecking);
      }
      catch (SAXNotRecognizedException e) {
        // ignore
      }
      catch (SAXNotSupportedException e) {
        System.err.println("warning: Parser does not support feature ("+SCHEMA_FULL_CHECKING_FEATURE_ID+")");
      }
      try {
        parser.setFeature(DYNAMIC_VALIDATION_FEATURE_ID, dynamicValidation);
      }
      catch (SAXNotRecognizedException e) {
        // ignore
      }
      catch (SAXNotSupportedException e) {
        System.err.println("warning: Parser does not support feature ("+DYNAMIC_VALIDATION_FEATURE_ID+")");
      }

      // parse file
      parser.setContentHandler(xmlHandler);
      parser.setErrorHandler(xmlHandler);
      try {
        parser.parse(arg);
      } catch (SAXParseException e) {
        // ignore
      }
      catch (Exception e) {
        System.err.println("error: Parse error occurred - "+e.getMessage());
        Exception se = e;
        if (e instanceof SAXException) {
          se = ((SAXException)e).getException();
        }
        if (se != null)
          se.printStackTrace(System.err);
        else
          e.printStackTrace(System.err);
      }
    }
  } // end main

    /** Prints the usage. */
    private static void printUsage() {

        System.err.println("usage: java sax.Counter (options) uri ...");
        System.err.println();

        System.err.println("options:");
        System.err.println("  -p name     Select parser by name.");
        System.err.println("  -n  | -N    Turn on/off namespace processing.");
        System.err.println("  -np | -NP   Turn on/off namespace prefixes.");
        System.err.println("              NOTE: Requires use of -n.");
        System.err.println("  -v  | -V    Turn on/off validation.");
        System.err.println("  -s  | -S    Turn on/off Schema validation support.");
        System.err.println("              NOTE: Not supported by all parsers.");
        System.err.println("  -f  | -F    Turn on/off Schema full checking.");
        System.err.println("              NOTE: Requires use of -s and not supported by all parsers.");
        System.err.println("  -dv | -DV   Turn on/off dynamic validation.");
        System.err.println("              NOTE: Requires use of -v and not supported by all parsers.");
        System.err.println("  -m  | -M    Turn on/off memory usage report");
        System.err.println("  -t  | -T    Turn on/off \"tagginess\" report.");
        System.err.println("  --rem text  Output user defined comment before next parse.");
        System.err.println("  -h          This help screen.");

        System.err.println();
        System.err.println("defaults:");
        System.err.println("  Parser:     "+DEFAULT_PARSER_NAME);
        System.err.print("  Namespaces: ");
        System.err.println(DEFAULT_NAMESPACES ? "on" : "off");
        System.err.print("  Prefixes:   ");
        System.err.println(DEFAULT_NAMESPACE_PREFIXES ? "on" : "off");
        System.err.print("  Validation: ");
        System.err.println(DEFAULT_VALIDATION ? "on" : "off");
        System.err.print("  Schema:     ");
        System.err.println(DEFAULT_SCHEMA_VALIDATION ? "on" : "off");
        System.err.print("  Schema full checking:     ");
        System.err.println(DEFAULT_SCHEMA_FULL_CHECKING ? "on" : "off");
        System.err.print("  Dynamic:    ");
        System.err.println(DEFAULT_DYNAMIC_VALIDATION ? "on" : "off");
        System.err.print("  Memory:     ");
        System.err.println(DEFAULT_MEMORY_USAGE ? "on" : "off");
        System.err.print("  Tagginess:  ");
        System.err.println(DEFAULT_TAGGINESS ? "on" : "off");

        System.err.println();
        System.err.println("notes:");
        System.err.println("  The speed and memory results from this program should NOT be used as the");
        System.err.println("  basis of parser performance comparison! Real analytical methods should be");
        System.err.println("  used. For better results, perform multiple document parses within the same");
        System.err.println("  virtual machine to remove class loading from parse time and memory usage.");
        System.err.println();
        System.err.println("  The \"tagginess\" measurement gives a rough estimate of the percentage of");
        System.err.println("  markup versus content in the XML document. The percent tagginess of a ");
        System.err.println("  document is equal to the minimum amount of tag characters required for ");
        System.err.println("  elements, attributes, and processing instructions divided by the total");
        System.err.println("  amount of characters (characters, ignorable whitespace, and tag characters)");
        System.err.println("  in the document.");
        System.err.println();
        System.err.println("  Not all features are supported by different parsers.");

    } // printUsage()

}
