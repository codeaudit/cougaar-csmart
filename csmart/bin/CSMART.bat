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


REM Make sure that COUGAAR_INSTALL_PATH is specified
IF NOT "%COUGAAR_INSTALL_PATH%" == "" GOTO L_2
REM Unable to find cougaar-install-path
ECHO COUGAAR_INSTALL_PATH not set!
GOTO L_END
:L_2

REM Make sure that COUGAAR3RDPARTY is specified
IF NOT "%COUGAAR3RDPARTY%" == "" GOTO L_3
REM Unable to find "sys" path for 3rd-party jars
ECHO COUGAAR3RDPARTY not set!
GOTO L_END
:L_3

REM The following line is optional. Some output files are written to the working directory.
REM CD %TEMP%

REM start the classpath with the optional COUGAAR_DEV_PATH
SET LIBPATHS=
IF NOT "%COUGAAR_DEV_PATH%" == "" SET LIBPATHS=%COUGAAR_DEV_PATH%;

REM Add CSMART jar explicitly to get started
SET LIBPATHS=%LIBPATHS%%COUGAAR_INSTALL_PATH%\lib\csmart.jar

REM The AppServer jar must also be specified
SET LIBPATHS=%LIBPATHS%;%COUGAAR_INSTALL_PATH%\lib\server.jar

REM For now CSMART needs "core.jar" for the Bootstrapper and some
REM  utility classes.  This dependency should be removed in a future
REM  release of CSMART!
SET LIBPATHS=%LIBPATHS%;%COUGAAR_INSTALL_PATH%\lib\core.jar

REM Plus these third party jar files, which are in COUGAAR3RDPARTY
SET LIBPATHS=%LIBPATHS%;%COUGAAR3RDPARTY%\xerces.jar
SET LIBPATHS=%LIBPATHS%;%COUGAAR3RDPARTY%\jcchart451K.jar
SET LIBPATHS=%LIBPATHS%;%COUGAAR3RDPARTY%\grappa1_2_bbn.jar
SET LIBPATHS=%LIBPATHS%;%COUGAAR3RDPARTY%\oracle12.zip
SET LIBPATHS=%LIBPATHS%;%COUGAAR3RDPARTY%\silk.jar

SET MYMEMORY=-Xms100m -Xmx300m
SET MYPROPERTIES=-Dorg.cougaar.install.path=%COUGAAR_INSTALL_PATH%
SET MYCONFIGPATH=-Dorg.cougaar.config.path="%COUGAAR_INSTALL_PATH%/csmart/data/common/\;"

REM Edit the following line to reflect your local installation
REM This is used by the Performance Analyzer
SET MYEXCEL=-Dexcel="C:\Program Files\Microsoft Office\Office\excel.exe"

@ECHO ON

java.exe %MYPROPERTIES% %MYMEMORY% %MYCONFIGPATH% %MYEXCEL% -classpath %LIBPATHS% org.cougaar.tools.csmart.ui.viewer.CSMART

:L_END
