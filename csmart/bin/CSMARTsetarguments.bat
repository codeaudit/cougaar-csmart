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
REM Rename this as CIP/bin/setarguments.bat, or merge the differences
REM from here into your existing script.
REM Differences are:
REM 1) Set the property csmart.log.severity
REM 2) Be sure to search csmart/data/common in looking for config
REM   files, first! This ensures that the CSMART default.psps.xml is
REM   used, to load the CSMART PSPs, and that the CSMART
REM   LDMDomains.ini is used, to load both the csmart and glm domains.
REM   This is included to also load CSMART UI properties.

REM Users may want to customize the MYLOG level set below

REM Domains are now usually defined by the config file LDMDomains.ini
REM But you may still use properties if you wish.
REM SET MYDOMAINS=-Dorg.cougaar.domain.alp=org.cougaar.glm.GLMDomain
SET MYDOMAINS=
SET MYCLASSES=org.cougaar.core.node.Node

REM Set the log level for the CSMART logging facilitiy
REM Options in increasing order of verbosity are: SEVERE, PROBLEM, DEBUG, VERBOSE, or VERY_VERBOSE
REM The log file will be COUGAAR_INSTALL_PATH/csmart/<date>.log
REM Note that the file COUGAAR_INSTALL_PATH/csmart/data/debug.properties
REM controls which modules will write to the log file
SET MYLOG=DEBUG

REM To point to configs at a web server, include the following:
REM -Dorg.cougaar.config.path="http://<full URL>/\;"
REM IMPORTANT: You must include the semi-colon, escaped with a backslash
REM in order to include the configs/common directory under
REM your CSMART installation directory
REM Also: Be sure that the directory from which your society
REM configuration is coming is listed _first_, and certainly before any
REM common directories.
SET MYCONFIG="%COUGAAR_INSTALL_PATH%/csmart/data/common/\;"

SET MYPROPERTIES=%MYDOMAINS% -Dorg.cougaar.system.path=%COUGAAR3RDPARTY% -Dorg.cougaar.install.path=%COUGAAR_INSTALL_PATH% -Duser.timezone=GMT -Dorg.cougaar.planning.ldm.lps.ComplainingLP.level=0

SET MYPROPERTIES=%MYPROPERTIES% -Dorg.cougaar.config.path=%MYCONFIG% -Dcsmart.log.severity=%MYLOG% -Dorg.cougaar.class.path=%LIBPATHS%

REM To collect statistics on Message Transport, the following is required
REM This may only work with RMI Transport
SET MYPROPERTIES=%MYPROPERTIES% -Dorg.cougaar.message.transport.aspects=org.cougaar.core.mts.StatisticsAspect

SET MYMEMORY=-Xms100m -Xmx300m
