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


REM Sample script for running the Node Server.
REM
REM Be sure to edit the COUGAAR_INSTALL_PATH and NODE_PROPS_FILE 
REM properties below.
REM
REM Note that CSMART may writes configuration files to the current 
REM working directory.


REM Set the COUGAAR_INSTALL_PATH, which is the base directory for the
REM COUGAAR installation
REM  #SET COUGAAR_INSTALL_PATH=C:\opt\alp


REM Specify the host-specific properties file, which defines *all*
REM the Node installation-specific properties.
REM
REM The ".props" file must be modified to match your installation.  An
REM example is provided with this release:
REM
REM    %COUGAAR_INSTALL_PATH%\csmart\data\win-server-sample.props
REM
REM The above example includes documentation details.
REM
REM Some additional properties are set by CSMART at run time, such 
REM as the Node's name, but otherwise this ".props" file specifies 
REM the full configuration.
REM
REM Note that Server properties are *not* passed to the Node -- 
REM only the properties in the ".props" file are passed.  For
REM example, the classpath for the Node is read from the file.
REM
REM Also see the Server documentation ("server\doc\README") for 
REM further details.

SET NODE_PROPS_FILE=server.props


REM
REM The remaining settings should not require modifications
REM unless you are debugging or have a custom COUGAAR installation.
REM


REM Specify the optional properties for the server itself.
REM
REM All server properties start with "-Dorg.cougaar.tools.server."
REM and only modify the Server's behavior.  See the Server 
REM documentation ("server\doc\README") for details.
REM
REM #SET SERVERCONFIG = "-Dorg.cougaar.tools.server.verbose=true"

SET SERVERCONFIG=


REM Specify the classpath for loading the Server.
REM
REM Below we set the AppServer's classpath to:
REM  %COUGAAR_DEV_PATH%	if defined
REM  %COUGAAR_INSTALL_PATH%\lib\server.jar
REM  %COUGAAR_INSTALL_PATH%\lib\csmart.jar
REM
REM This is only useful to altering the Server's codebase,
REM *not* the Node's codebase.  The ".props" file must be
REM modified to alter the Node's configuration.

SET LIBPATHS=^
%COUGAAR_INSTALL_PATH%\lib\server.jar;^
%COUGAAR_INSTALL_PATH%\lib\csmart.jar

IF NOT ("%COUGAAR_DEV_PATH%" == "") SET LIBPATHS=%COUGAAR_DEV_PATH%;%LIBPATHS%

SET JAVA_ARGS=^
  -classpath %LIBPATHS%

REM start the server

@ECHO ON

java ^
  %JAVA_ARGS% ^
  %SERVERCONFIG% ^
  org.cougaar.tools.server.NodeServer ^
  %NODE_PROPS_FILE%

