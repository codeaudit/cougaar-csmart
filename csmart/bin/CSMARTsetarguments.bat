@echo OFF

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
REM SET MYDOMAINS=-Dorg.cougaar.domain.alp=org.cougaar.domain.glm.GLMDomain
SET MYDOMAINS=
SET MYCLASSES=org.cougaar.core.society.Node

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

SET MYPROPERTIES=%MYDOMAINS% -Dorg.cougaar.system.path=%COUGAAR3RDPARTY% -Dorg.cougaar.install.path=%COUGAAR_INSTALL_PATH% -Duser.timezone=GMT -Dorg.cougaar.domain.planning.ldm.lps.ComplainingLP.level=0

SET MYPROPERTIES=%MYPROPERTIES% -Dorg.cougaar.config.path=%MYCONFIG% -Dcsmart.log.severity=%MYLOG%
SET MYMEMORY=-Xms100m -Xmx300m

