csmart/bin/Readme.txt

This directory contains scripts for building and running CSMART.  For
ease of use, put this directory in your PATH environment.

For setup instructions, see csmart/bin/CSMART-Install.txt

Also, all scripts here assume that the environment variable
COUGAAR_INSTALL_PATH has been set appropriately, as in:
     COUGAAR_INSTALL_PATH=D:\Cougaar
     (where this directory is D:\Cougaar\csmart\bin)

A platform-specific version of "dot.exe" from AT&T is required for
graph layout. See csmart/data/CSMART-Install.txt for information on
getting this file, and install location.

All files named CSMARTNode, CSMARTsetlibpath.bat and
CSMARTsetarguments.bat are to be copied into COUGAAR_INSTALL_PATH/bin
and renamed to remove the CSMART beginning. Or use as you see fit -
just be sure you are running with all of the required properties.

Also note: To get access to the CPU related metrics, you will need to
install the Mylib.dll/.so in your path: CIP/csmart/bin or JDK_HOME/bin
work well. This library can be built from the scalability module
source, but the society runs fine without it.


Some key parameters that you will want to locally edit:
ABC PlugIns use a custom logger, whose verbosity can be editted (see
setarguments.bat)
Excel location is specified in CMSART / CSMART.bat


Contents:

CSMARTNode
        A Unix script for running an CSMART society.  Sets up the
        libpath and csmart.log.severity and other properites.
        
CSMARTsetarguments.bat
        Set up java command line arguments on a Windows box. Edit this
        to ensure your log level and config path are what you want.
        See above for final location and name.

CSMARTsetlibpath.bat
        Set up java command line paths on a Windows box.  Should not
        need to be editted, once properly located. Note that this is
        really just the basic LIBPATH from core.
	
CSMART
CSMART.bat - The main script for the CSMART UI. Relies on the
setarguments from above.
	NOTE: CSMART.bat specifies the location of MS Excel on your
	local machine, and should be editted to reflect your local
	installation.

build-all.bat
        Simple script for compiling CSMART sources on a Windows machine.

build.xml
	ANT build script. ANT is available at http://jakarta.apache.org/ant

dot.exe
dot-l386
        For the AT&T GraphViz package, for doing layout.  "dot.exe" is for
        DOS/WIN machines, "dot-l386" is for Linux.  Used by the CSMART
	EventGraph UI. These are also available from the
	csmart-3rdparty.zip file, or from cougaar.zip. The
	usual location is CIP/csmart/bin, alternatives like CIP/sys
	and CIP/bin also work. You can also get these files from the ABC website
        (http://ABCTestBed.bbn.com) or the AT&T GraphViz site.
	
Monitor
Monitor.bat - Scripts for running the CSMART Society Monitor tool
standalone, to view the conents of an arbitrary Cougaar
blackboard. Again, these rely on the arguments being set from the
above .bat script.
Be sure to include the required PSPs in your default.psps.xml

Server
Server.bat - Sample scripts for running the Node server (the server
module), including parameters required for CSMART societies.
	You will need to edit these to point to your local host properties file.

