@echo OFF

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


REM CSMART version of COUGAAR_INSTALL_PATH/bin/setarguments.bat
REM Use this _only_ if running ABC Nodes stand-alone.

REM Rename this as CIP/bin/setarguments.bat, or merge the differences
REM from here into your existing script.
REM Differences are:
REM Be sure to search csmart/data/common in looking for config
REM   files, first! This ensures that the CSMART
REM   LDMDomains.ini is used, to load both the csmart and glm domains.
REM   This is included to also load CSMART UI properties.

REM Domains are now usually defined by the config file LDMDomains.ini
REM But you may still use properties if you wish.
REM SET MYDOMAINS=-Dorg.cougaar.domain.alp=org.cougaar.glm.GLMDomain
SET MYDOMAINS=
SET MYCLASSES=org.cougaar.bootstrap.Bootstrapper org.cougaar.core.node.Node

REM You may use the optional environment variable COUGAAR_DEV_PATH
REM to point to custom developed code that is not in COUGAR_INSTALL_PATH/lib
REM or CIP/sys. This can be one or many semicolon separated 
REM directories/jars/zips, or left undefined

REM To point to configs at a web server, include the following:
REM -Dorg.cougaar.config.path="http://<full URL>/\;"
REM IMPORTANT: You must include the semi-colon, escaped with a backslash
REM in order to include the configs/common directory under
REM your CSMART installation directory
REM Also: Be sure that the directory from which your society
REM configuration is coming is listed _first_, and certainly before any
REM common directories.
SET MYCONFIG="%COUGAAR_INSTALL_PATH%/csmart/data/common/\;"

set MYPROPERTIES=-Xbootclasspath/p:%COUGAAR_INSTALL_PATH%\lib\javaiopatch.jar -Dorg.cougaar.system.path=%COUGAAR3RDPARTY% -Dorg.cougaar.install.path=%COUGAAR_INSTALL_PATH% -Duser.timezone=GMT -Dorg.cougaar.core.agent.startTime=08/10/2005 -Dorg.cougaar.class.path=%COUGAAR_DEV_PATH% -Dorg.cougaar.workspace=%COUGAAR_WORKSPACE%

SET MYPROPERTIES=%MYPROPERTIES% -Dorg.cougaar.config.path=%MYCONFIG% %MYDOMAINS% -Dorg.cougaar.planning.ldm.lps.ComplainingLP.level=0

REM To collect statistics on Message Transport, the following is required
REM This may only work with RMI Transport
SET MYPROPERTIES=%MYPROPERTIES% -Dorg.cougaar.message.transport.aspects=org.cougaar.core.mts.StatisticsAspect

SET MYMEMORY=-Xms100m -Xmx300m
