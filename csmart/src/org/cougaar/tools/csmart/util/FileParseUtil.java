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

import org.cougaar.tools.csmart.ui.viewer.CSMART;
import org.cougaar.util.ConfigFinder;
import org.cougaar.util.log.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.CharBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * FileParseUtil.java
 *
 *
 * Created: Wed Mar 13 09:45:18 2002
 *
 * @author <a href="mailto:bkrisler@bbn.com">Brian Krisler</a>
 * @version 1.0
 */
public class FileParseUtil {

  public FileParseUtil () {
  }

  /**
   * Searches the given file for the specified regular expression
   * pattern.  Returns true or false based on the result of the find.
   *
   * @param filename - The file to search for regex.
   * @param pattern - Regular Expression pattern to search for.
   * @return boolean indicating the result of the search.
   */
  public static final boolean containsPattern(String filename, String pattern) {
    Logger log = CSMART.createLogger("org.cougaar.tools.csmart.FileParseUtil");

//     if(log.isDebugEnabled()) {
//       log.debug("Using File: " + filename);
//     }

    // Create the CharBuffer for the file.
    FileInputStream iStream = null;
    try {
      File input = new File(filename);
      if (! input.exists()) {
	// Try the ConfigFinder
	// FIXME: On windows, maybe we need to use SocietyFinder?
	input = ConfigFinder.getInstance("csmart").locateFile(filename);
      }
      if (input != null && input.exists()) {
// 	if (log.isDebugEnabled()) {
// 	  log.debug("Found file: " + input.getPath());
// 	}
	iStream = new FileInputStream(input);
      } else {
	// Couldn't find the file at all. Return false;
	if (log.isDebugEnabled()) {
	  log.debug("Couldn't find file: " + filename);
	}
	return false;
      }
    } catch(IOException e) {
      if(log.isErrorEnabled()) {
        log.error("Exception finding file", e);
      }
    }

    FileChannel channel = iStream.getChannel();
    int length = 0;
    try {
      length = (int)channel.size();
    } catch(IOException e) {
      if(log.isErrorEnabled()) {
        log.error("Exception getting channel size", e);
      }
    }
    MappedByteBuffer buffer = null;
    try {
      buffer = channel.map(FileChannel.MapMode.READ_ONLY, 0, length);
    } catch(IOException e) {
      if(log.isErrorEnabled()) {
        log.error("Exception creating buffer", e);
      }
    }

    Charset charset = Charset.forName("ISO-8859-1");
    CharsetDecoder decoder = charset.newDecoder();
    CharBuffer cBuffer = null;
    try {
      cBuffer = decoder.decode(buffer);
    } catch(CharacterCodingException cce) {
      if(log.isErrorEnabled()) {
        log.error("Exception decoding buffer", cce);
      }
    }

    
    // Now search the CharBuffer using regex Patterns.
    Pattern ptrn = Pattern.compile(pattern);
    Matcher matcher = ptrn.matcher(cBuffer);

    return matcher.find();

  }

  /**
   * Tests the ini file to determine if it is a New ini.dat style, or
   * an older ini.dat style.  The old ini.dat style contains a 
   * [UniqueId] which does not exist in the newer style.  
   * <br>
   * There are other differences, but this difference is the first and
   * is the easiest to check existence of.
   *
   * @param filename - The file to determine if old or new style.
   */
  public static final boolean isOldStyleIni(String filename) {
    if(FileParseUtil.containsPattern(filename, "^\\[UniqueId") ||
       FileParseUtil.containsPattern(filename, "^\\[UIC") ||
       !FileParseUtil.containsPattern(filename, 
                                      "\\[Relationship\\]\\s*([^\\s#]+[^\\S\\n\\r]+){5}\\S*")) {

      return true;
    } else {
      return false;
    }
  }

  public static void main(String args[]) {
    String filename = args[0];
    if(FileParseUtil.isOldStyleIni(filename)) {
      System.out.println("Yes, " + filename + " is old Style");
    } else {
      System.out.println("No, " + filename + " is not old Style");
    }
  }
       
}// FileParseUtil
