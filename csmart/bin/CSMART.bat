@ECHO OFF

REM "<copyright>"
REM " Copyright 2001-2003 BBNT Solutions, LLC"
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
REM This is usually COUGAAR_INSTALL_PATH/sys
ECHO COUGAAR3RDPARTY not set! Defaulting to CIP\sys
SET COUGAAR3RDPARTY=%COUGAAR_INSTALL_PATH%\sys
:L_3

REM The following line is optional. Some output files are written to the working directory.
REM CD %TEMP%

REM You may use the optional environment variable COUGAAR_DEV_PATH
REM to point to custom developed code that is not in COUGAR_INSTALL_PATH/lib
REM or CIP/sys. This can be one or many semicolon separated 
REM directories/jars/zips, or left undefined

REM start the classpath with the optional COUGAAR_DEV_PATH
SET DEVPATH=
IF NOT "%COUGAAR_DEV_PATH%" == "" SET DEVPATH=-Dorg.cougaar.class.path=%COUGAAR_DEV_PATH%

REM Start CSMART using Bootstrapper
SET MYCLASSPATH=%COUGAAR_INSTALL_PATH%\lib\bootstrap.jar

SET MYMEMORY=-Xms100m -Xmx300m
SET MYPROPERTIES=-Dorg.cougaar.install.path=%COUGAAR_INSTALL_PATH%
SET MYCONFIGPATH=-Dorg.cougaar.config.path="%COUGAAR_INSTALL_PATH%/csmart/data/common/\;%COUGAAR_INSTALL_PATH%/configs/minitestconfig/\;"

REM Edit the following line to reflect your local installation
REM This is used by the Performance Analyzer
SET MYEXCEL=-Dorg.cougaar.tools.csmart.excelpath="C:\Program Files\Microsoft Office\Office\excel.exe"

REM It is possible to disable use of the workspace file: Your work
REM will only be saved to the database when you explicitly save,
REM and will not be restored from the workspace file. You will have to
REM reload all experiments from the database every time you restart.
REM To do so, uncomment the following line.
REM SET MYPROPERTIES=-Dorg.cougaar.tools.csmart.doWorkspace=false %MYPROPERTIES%

REM By default, CSMART only permits Recipe target queries to look at
REM the basic society definition, the communities, and the Agent,
REM Nodes, and Hosts. In particular, you should not depend on the
REM particular Plugins, Binder, or parameters within Agents. These items
REM may be changed by a recipe, and those changes will not be available to 
REM later recipes in deciding whether the recipe is applicable in that
REM case. If however, you have a recipe that needs this added
REM complexity (for example, wants to look at Agent relationships), then
REM un-comment the following line.
REM SET MYPROPERTIES=-Dorg.cougaar.tools.csmart.allowComplexRecipeQueries=true %MYPROPERTIES%

@ECHO ON

java.exe %MYPROPERTIES% %MYMEMORY% %MYCONFIGPATH% %MYEXCEL% %DEVPATH% -classpath %MYCLASSPATH% org.cougaar.bootstrap.Bootstrapper org.cougaar.tools.csmart.ui.viewer.CSMART

:L_END
