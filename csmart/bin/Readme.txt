csmart/bin/Readme.txt

This directory contains scripts for building and running CSMART.  For
ease of use, put this directory in your PATH environment.

For setup instructions, see csmart/bin/CSMART-Install.txt

Also, all scripts here assume that the environment variable
COUGAAR_INSTALL_PATH has been set appropriately, as in:
     COUGAAR_INSTALL_PATH=D:\Cougaar
     (where this directory is D:\Cougaar\csmart\bin)
In addition, COUGAAR3RDPARTY should refer to the directory where
the Cougaar support libraries have been installed (xerces, etc). For
example D:\Cougaar\sys

A platform-specific version of "dot.exe" from AT&T is required for
graph layout. New installations (post v0.3) come with both the Win and
Linux/Solaris versions of this file.

All files named CSMARTNode, CSMARTsetlibpath.bat and
CSMARTsetarguments.bat are to be copied into COUGAAR_INSTALL_PATH/bin
and renamed to remove the CSMART beginning. Or use as you see fit -
just be sure you are running with all of the required properties.

Also note: To get access to the CPU related metrics, you will need to
install the Mylib.dll/.so in your path: CIP/csmart/bin or JDK_HOME/bin
work well. This library can be installed from the scalability module
per its installation instructions (scalability/bin/Install.txt) 
but the society runs fine without it.


Some key parameters that you will want to locally edit:
ABC PlugIns use a custom logger, whose verbosity can be edited (see
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
CSMART.sh
CSMART.bat - The main script for the CSMART UI. Relies on the
        setarguments from above.
	NOTE: CSMART.bat specifies the location of MS Excel on your
	local machine, and should be editted to reflect your local
	installation.

build-all.bat
        Simple script for compiling CSMART sources on a Windows machine.

build.xml
	ANT1.3 build script. ANT is available at http://jakarta.apache.org/ant

dot.exe
dot-l386
        For the AT&T GraphViz package, for doing layout.  "dot.exe" is for
        DOS/WIN machines, "dot-l386" is for Linux (also works on Solaris).  
        Used by the CSMART PlanView UI. These come with a standard CSMART install 
        (from the cougaar.zip), but are also available from the
	csmart-3rdparty.zip file. The
	usual location is CIP/csmart/bin, alternatives like CIP/sys
	and CIP/bin also work. You can also get these files from the ABC website
        (http://ABCTestBed.bbn.com) or the AT&T GraphViz site.
	
Monitor
Monitor.bat - Scripts for running the CSMART Society Monitor tool
        standalone, to view the conents of an arbitrary Cougaar
        blackboard. Again, these rely on the arguments being set from the
        above .bat script.
        Be sure to include the required PSPs in your default.psps.xml

[Sample scripts for running the AppServer have been moved to the server module.]
