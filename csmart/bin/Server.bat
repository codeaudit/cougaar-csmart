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


REM Sample script for running the Node Server, for use with CSMART
REM Be sure to edit the MYPROPFILE property below

REM The following line is optional. The server writes configuration
REM    files to its working directory.
REM CD %TEMP%

CALL %COUGAAR_INSTALL_PATH%\bin\setlibpath.bat
CALL %COUGAAR_INSTALL_PATH%\bin\setarguments.bat

SET LIBPATHS=%LIBPATHS%;%COUGAAR_INSTALL_PATH%\lib\csmart.jar
SET LIBPATHS=%LIBPATHS%;%COUGAAR_INSTALL_PATH%\lib\server.jar

REM Set the location of your site-specific properties file
REM Use the sample included in the data directory, editing it
REM to reflect your local setting of COUGAAR_INSTALL_PATH, etc
SET MYPROPFILE=%COUGAAR_INSTALL_PATH%\csmart\data\win-server-sample.props

@ECHO ON

java %MYPROPERTIES% %MYMEMORY% -classpath %LIBPATHS% org.cougaar.tools.server.NodeServer %MYPROPFILE%
