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
package org.cougaar.tools.csmart.util;

import org.apache.xerces.parsers.DOMParser;
import org.cougaar.tools.csmart.ui.viewer.CSMART;
import org.cougaar.util.ConfigFinder;
import org.cougaar.util.log.Logger;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.FileNotFoundException;

/**
 * XMLUtils.java
 *
 * Basic utilities for parsing and writing XML files.
 *
 * Created: Wed Jun  5 10:41:48 2002
 *
 * @author <a href="mailto:bkrisler@bbn.com">Brian Krisler</a>
 * @version 1.0
 */
public class XMLUtils {
  private static Logger log = CSMART.createLogger("org.coguaar.tools.csmart.util.XMLUtils");

  public XMLUtils() {
  }

  /**
   * Loads and parses an XML file into a <code>Document</code>
   *
   * @param filename Name of file to load.
   * @return a <code>Document</code> value, null on error or nonexistent file
   */
  public static Document loadXMLFile(String filename) {
    if (filename == null || filename.equals(""))
      return null;

    // Try to open the file in an input stream. Note it may not exist!
    InputStream is = null;
    try {
      is = ConfigFinder.getInstance("csmart").open(filename);
    } catch (IOException ioe) {
      if (log.isWarnEnabled()) {
        log.warn("Could not open " + filename + " for reading: " + ioe);
      }
      return null;
    }
    if (is == null) {
      if (log.isWarnEnabled()) {
        log.warn("Could not open " + filename + " for reading.");
      }
      return null;
    }

    return loadXMLFile(is, filename);
  }

  /**
   * Loads and parses and XML file into a <code>Document</code>
   *
   * @param file - Handle to xml file.
   * @return a <code>Document</code> value, null on error
   */
  public static Document loadXMLFile(File file) throws FileNotFoundException {
    if (file == null || !file.canRead() || file.isDirectory())
      return null;

    return loadXMLFile(new FileInputStream(file), file.getName());
  }

  private static Document loadXMLFile(InputStream stream, String name) {
    try {
      DOMParser parser = new DOMParser();
      parser.parse(new InputSource(stream));
      return parser.getDocument();
    } catch (org.xml.sax.SAXParseException spe) {
      if (log.isErrorEnabled()) {
        log.error("Parse exception Parsing file: " + name, spe);
      }
    } catch (org.xml.sax.SAXException se) {
      if (log.isErrorEnabled()) {
        log.error("SAX exception Parsing file: " + name, se);
      }
    } catch (Exception e) {
      if (log.isErrorEnabled()) {
        log.error("Exception Parsing file: " + name, e);
      }
    }

    return null;
  }

  /**
   * Writes the contents of the <code>Node</code> to the specified
   * file, in XML format.
   *
   * @param configDir - Directory to write new xml file.
   * @param node - Document Node to dump to xml file.
   * @param name - Name of the new xml file.
   * @see org.w3c.dom.Node
   * @exception IOException if an error occurs
   */
  public static void writeXMLFile(File configDir, Node node, String name)
      throws IOException {

    Logger log = CSMART.createLogger("org.cougaar.tools.csmart.util.XMLUtils");

    if (!name.endsWith(".xml")) {
      name = name + ".xml";
    }
    PrintWriter writer = new PrintWriter(new FileWriter(new File(configDir, name)));
    try {
      writeNode(writer, node, 0);
    } catch (Exception e) {
      if (log.isErrorEnabled()) {
        log.error("Error writing config file: " + e);
      }
    } finally {
      writer.close();
    }

  }

  private static void writeNode(PrintWriter writer, Node node, int indent) {
    StringBuffer ibuff = new StringBuffer();
    for (int i = 0; i < indent; i++) {
      ibuff.append(" ");
    }
    int type = node.getNodeType();
    switch (type) {
      case Node.DOCUMENT_NODE:
        writer.println("<?xml version=\"1.0\" encoding=\"" +
                       "UTF-8" + "\"?>");
        indent = -2;
        break;
      case Node.ELEMENT_NODE:
        writer.print(ibuff.substring(0) + '<' + node.getNodeName());
        NamedNodeMap nnm = node.getAttributes();
        if (nnm != null) {
          int len = nnm.getLength();
          Attr attr;
          for (int i = 0; i < len; i++) {
            attr = (Attr) nnm.item(i);
            writer.print(' '
                         + attr.getNodeName()
                         + "=\""
                         + attr.getNodeValue()
                         + '"');
          }
        }
        writer.println('>');
        break;

      case Node.ENTITY_REFERENCE_NODE:
        writer.print('&' + node.getNodeName() + ';');
        break;
      case Node.CDATA_SECTION_NODE:
        writer.print("<![CDATA["
                     + node.getNodeValue()
                     + "]]>");
        break;
      case Node.TEXT_NODE:
        writer.print(ibuff.substring(0) + node.getNodeValue());
        break;
      case Node.PROCESSING_INSTRUCTION_NODE:
        writer.print(ibuff.substring(0) + "<?"
                     + node.getNodeName());
        String data = node.getNodeValue();
        if (data != null && data.length() > 0) {
          writer.print(' ');
          writer.print(data);
        }
        writer.println("?>");
        break;

    }//end of switch


    //recurse
    for (Node child = node.getFirstChild(); child != null;
         child = child.getNextSibling()) {
      writeNode(writer, child, indent + 2);
    }

    //without this the ending tags will miss
    if (type == Node.ELEMENT_NODE) {
      writer.println(ibuff.substring(0) + "</" + node.getNodeName() + ">");
    }
  }

}// XMLUtils
