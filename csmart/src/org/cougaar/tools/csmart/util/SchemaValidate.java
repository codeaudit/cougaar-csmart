/*
 * <copyright>
 *  
 *  Copyright 2004 BBNT Solutions, LLC
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
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.SAXParseException;

import java.io.IOException;

//  A Valdating DOM Application
//  with registered Error Handlers
public class SchemaValidate implements ErrorHandler {

    // Constructor
    public SchemaValidate (String xmlFile) {
        //  Create a Xerces DOM Parser
        DOMParser parser = new DOMParser();

        //  Turn Validation on
        try {
            parser.setFeature
            ("http://xml.org/sax/features/validation", true);
            parser.setFeature
            ("http://apache.org/xml/features/validation/schema",true);
            parser.setFeature
            ("http://apache.org/xml/features/validation/schema-full-checking",true);

        } catch (SAXNotRecognizedException e) {
            System.err.println (e);
        } catch (SAXNotSupportedException e) {
            System.err.println (e);
        }

        //  Register Error Handler
        parser.setErrorHandler (this);

        //  Parse the Document
        //  and traverse the DOM
        try {
            parser.parse(xmlFile);
//            Document document = parser.getDocument();
//            traverse (document);
        } catch (SAXException e) {
            System.err.println (e);
        } catch (IOException e) {
            System.err.println (e);
        } catch (Exception e) {
            System.err.println (e);
        }

    }

    //  Traverse DOM Tree.  Print out Element Names
    private void traverse (Node node) {
        int type = node.getNodeType();
        if (type == Node.ELEMENT_NODE)
            System.out.println (node.getNodeName());
        NodeList children = node.getChildNodes();
        if (children != null) {
            for (int i=0; i< children.getLength(); i++)
                traverse (children.item(i));
        }
    }

    //  Warning Event Handler
    public void warning (SAXParseException e)
        throws SAXException {
        System.err.println ("Warning:  "+e);
    }

    //  Error Event Handler
    public void error (SAXParseException e)
        throws SAXException {
        System.err.println ("Error:  "+e);
    }

    //  Fatal Error Event Handler
    public void fatalError (SAXParseException e)
        throws SAXException {
        System.err.println ("Fatal Error:  "+e);
    }

    // Main Method
    public static void main (String[] args) {
        SchemaValidate validatingDOM =
        	new SchemaValidate (args[0]);
    }
}
