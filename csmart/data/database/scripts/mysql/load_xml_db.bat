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

REM This script loads all data necessary to run from XML files into a single MySQL database.
REM In particular, it does not load the CSMART tables.
REM This does _not_ load data for the cougaar.rc entry org.cougaar.configuration.database.
REM It _does_ load data for these other entries:
REM org.cougaar.database
REM org.cougaar.oplan.database
REM org.cougaar.refconfig.database
REM blackjack.database
REM icis.database
REM fcs.database
REM All the above entries should point to the single DB name in your cougaar.rc file

REM Note that this script requires a "jar.exe" on your path, available with a JDK installation

REM Make sure that COUGAAR_INSTALL_PATH is specified
IF NOT "%COUGAAR_INSTALL_PATH%" == "" GOTO L_2
REM Unable to find cougaar-install-path
ECHO COUGAAR_INSTALL_PATH not set!
GOTO L_END

:L_2
REM Check arguments. If got none, display usage
IF NOT "%3" == "" GOTO L_3
    ECHO Load all data necessary to run from XML files (not CSMART) into single database.
    ECHO Usage: load_xml_db.bat [DB username] [password] [database]
    GOTO L_END

:L_3
REM  If dbms directory exists then load domain data and oplan data
REM  otherwise just load the xml and config databases

IF NOT EXIST %COUGAAR_INSTALL_PATH%\dbms\data\mysql\ GOTO L_4

ECHO Found domain data

REM The domain db (1ADDomainData including fdm tables)
IF EXIST %COUGAAR_INSTALL_PATH%\dbms\data\mysql\load_domain_data.bat GOTO L_5
ECHO Cannot find %COUGAAR_INSTALL_PATH%\dbms\data\mysql\load_domain_data.bat
GOTO L_END

:L_5
ECHO Loading domain database- %3
CALL %COUGAAR_INSTALL_PATH%\dbms\data\mysql\load_domain_data.bat %1 %2 %3 

IF NOT EXIST %COUGAAR_INSTALL_PATH%\fcsua\ GOTO L_7
REM The ua domain db
IF EXIST %COUGAAR_INSTALL_PATH%\fcsua\data\database\scripts\mysql\load_ua_domain_data.bat GOTO L_6
ECHO Cannot find %COUGAAR_INSTALL_PATH%\fcsua\data\database\scripts\mysql\load_ua_domain_data.bat
GOTO L_END

:L_6
ECHO Loading ua domain database- %3
CALL %COUGAAR_INSTALL_PATH%\fcsua\data\database\scripts\mysql\load_ua_domain_data.bat %1 %2 %3

:L_7
REM The oplan db
IF EXIST %COUGAAR_INSTALL_PATH%\csmart\data\database\scripts\mysql\load_oplan_data.bat GOTO L_8
ECHO Cannot find %COUGAAR_INSTALL_PATH%\csmart\data\database\scripts\mysql\load_oplan_data.bat
GOTO L_END

:L_8
ECHO Loading oplan database- %3
CALL %COUGAAR_INSTALL_PATH%\csmart\data\database\scripts\mysql\load_oplan_data.bat %1 %2 %3

ECHO Done loading domain data.

:L_4
REM Now the xml refconfig data
IF EXIST %COUGAAR_INSTALL_PATH%\csmart\data\database\scripts\mysql\load_ref_data.bat GOTO L_9
ECHO Cannot find %COUGAAR_INSTALL_PATH%\csmart\data\database\scripts\mysql\load_ref_data.bat
GOTO L_END

:L_9
ECHO   - Loading the xml refconfig database tables.
%COUGAAR_INSTALL_PATH%\csmart\data\database\scripts\mysql\load_ref_data.bat %1 %2 %3

ECHO Done

:L_END

