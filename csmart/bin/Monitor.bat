@ECHO OFF

REM "<copyright>"
REM " Copyright 2001,2002 BBNT Solutions, LLC"
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


REM Script to run the CSMART Society Monitor as a standalone

REM calls setlibpath.bat which sets the path to the required jar files.
CALL %COUGAAR_INSTALL_PATH%\bin\setlibpath.bat

REM start the classpath with the optional COUGAAR_DEV_PATH
SET DEVPATH=
IF NOT "%COUGAAR_DEV_PATH%" == "" SET DEVPATH=-Dorg.cougaar.class.path=%COUGAAR_DEV_PATH%
REM Use the Bootstrapper to find Jar files.

SET MYMEMORY=-Xms100m -Xmx300m
SET MYPROPERTIES=-Dorg.cougaar.install.path=%COUGAAR_INSTALL_PATH%
SET MYCONFIGPATH=-Dorg.cougaar.config.path="%COUGAAR_INSTALL_PATH%/csmart/data/common/\;"

@ECHO ON

java.exe %MYPROPERTIES% %MYMEMORY% %MYCONFIGPATH% %DEVPATH% -classpath %LIBPATHS% org.cougaar.core.node.Bootstrapper org.cougaar.tools.csmart.ui.monitor.viewer.CSMARTUL

