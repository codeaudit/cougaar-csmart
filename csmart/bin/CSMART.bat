@ECHO OFF

REM "<copyright>"
REM " Copyright 2001 BBNT Solutions, LLC"
REM " under sponsorship of the Defense Advanced Research Projects Agency (DARPA)."
REM ""
REM " This program is free software; you can redistribute it and/or modify"
REM " it under the terms of the Cougaar Open Source License as published by"
REM " DARPA on the Cougaar Open Source Website (www.cougaar.org)."
REM ""
REM " THE COUGAAR SOFTWARE AND ANY DERIVATIVE SUPPLIED BY LICENSOR IS"
REM " PROVIDED 'AS IS' WITHOUT WARRANTIES OF ANY KIND, WHETHER EXPRESS OR"
REM " IMPLIED, INCLUDING (BUT NOT LIMITED TO) ALL IMPLIED WARRANTIES OF"
REM " MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE, AND WITHOUT"
REM " ANY WARRANTIES AS TO NON-INFRINGEMENT.  IN NO EVENT SHALL COPYRIGHT"
REM " HOLDER BE LIABLE FOR ANY DIRECT, SPECIAL, INDIRECT OR CONSEQUENTIAL"
REM " DAMAGES WHATSOEVER RESULTING FROM LOSS OF USE OF DATA OR PROFITS,"
REM " TORTIOUS CONDUCT, ARISING OUT OF OR IN CONNECTION WITH THE USE OR"
REM " PERFORMANCE OF THE COUGAAR SOFTWARE."
REM "</copyright>"


REM Main script to run CSMART
REM Users may want to customize the location of MS Excel

REM The following line is optional. Some output files are written to the working directory.
REM CD %TEMP%

REM calls setlibpath.bat which sets the path to the required jar files.
CALL %COUGAAR_INSTALL_PATH%\bin\setlibpath.bat

REM Next section lists classes that must be in CLASSPATH
REM Note however that the Cougaar Bootstrapper will usually find
REM them. The listed Jars however are less common,
REM and so are listed here as a convenience.
REM Note that the CSMART Jar file must be specified (as well as core),
REM to get started.
REM To not use the Bootstrapper, set org.cougaar.useBootstrapper=false

REM add csmart.jar, explicitly
SET LIBPATHS=%LIBPATHS%;%COUGAAR_INSTALL_PATH%\lib\csmart.jar

REM for CSMART environment, use the following
rem SET LIBPATHS=%LIBPATHS%;%COUGAAR_INSTALL_PATH%\lib\server.jar

REM Plus these third party jar files, which are in CIP\sys
rem SET LIBPATHS=%LIBPATHS%;%COUGAAR_INSTALL_PATH%\sys\xerces.jar
rem SET LIBPATHS=%LIBPATHS%;%COUGAAR_INSTALL_PATH%\sys\jcchart451K.jar
rem SET LIBPATHS=%LIBPATHS%;%COUGAAR_INSTALL_PATH%\sys\grappa1_2_bbn.jar

SET MYMEMORY=-Xms100m -Xmx300m
SET MYPROPERTIES=-Dorg.cougaar.install.path=%COUGAAR_INSTALL_PATH%
SET MYCONFIGPATH=-Dorg.cougaar.config.path="%COUGAAR_INSTALL_PATH%/csmart/data/common/\;"

REM Edit the following line to reflect your local installation
REM This is used by the Performance Analyzer
SET MYEXCEL=-Dexcel="C:\Program Files\Microsoft Office\Office\excel.exe"

@ECHO ON

java.exe %MYPROPERTIES% %MYMEMORY% %MYCONFIGPATH% %MYEXCEL% -classpath %LIBPATHS% org.cougaar.tools.csmart.ui.viewer.CSMART

