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


REM This script loads all data necessary to run Cougaar from CSMART and the CSMART database.
REM It does not load databases necessary to run from XML files.
REM In particular, it does not load data for the
REM org.cougaar.refconfig.database cougaar.rc entry.
REM It will load, all into the single named database, data for cougaar.rc entries:
REM org.cougaar.database
REM org.cougaar.oplan.database
REM org.cougaar.configuration.database
REM blackjack.database
REM icis.database

REM Note that this script requires a "jar.exe" on your path, available with a JDK installation

REM Make sure that COUGAAR_INSTALL_PATH is specified
IF NOT "%COUGAAR_INSTALL_PATH%" == "" GOTO L_2
REM Unable to find cougaar-install-path
ECHO COUGAAR_INSTALL_PATH not set!
GOTO L_END

:L_2
REM Check arguments. If got none, display usage
IF NOT "%3" == "" GOTO L_3
    ECHO Load all data necessary to run from CSMART (not XML) into single database.
    ECHO Usage: load_csmart_db.bat [DB username] [password] [database]
GOTO L_END

:L_3
REM  If dbms directory exists then load domain data and oplan data
REM  otherwise just load the xml and config databases

REM Skip domain data loading if not avail
IF NOT EXIST %COUGAAR_INSTALL_PATH%\dbms\data\mysql\ GOTO L_4

ECHO Found domain data

REM The domain db (1ADDomainData including fdm tables)
IF EXIST %COUGAAR_INSTALL_PATH%\dbms\data\mysql\load_domain_data.bat GOTO L_5
ECHO Cannot find %COUGAAR_INSTALL_PATH%\dbms\data\mysql\load_domain_data.bat
GOTO L_END

:L_5
ECHO Loading domain database- %3
CALL %COUGAAR_INSTALL_PATH%\dbms\data\mysql\load_domain_data.bat %1 %2 %3 

REM The oplan db
IF EXIST %COUGAAR_INSTALL_PATH%\csmart\data\database\scripts\mysql\load_oplan_data.bat GOTO L_6
ECHO Cannot find %COUGAAR_INSTALL_PATH%\csmart\data\database\scripts\mysql\load_oplan_data.bat
GOTO L_END

:L_6
ECHO Loading oplan database- %3
CALL %COUGAAR_INSTALL_PATH%\csmart\data\database\scripts\mysql\load_oplan_data.bat %1 %2 %3

ECHO Done loading domain data.

:L_4

REM The config db (csmart)
IF EXIST %COUGAAR_INSTALL_PATH%\csmart\data\database\scripts\mysql\load_csmart_data.bat GOTO L_7
ECHO Cannot find %COUGAAR_INSTALL_PATH%\csmart\data\database\scripts\mysql\load_csmart_data.bat
GOTO L_END

:L_7
ECHO Loading config database- %3
CALL %COUGAAR_INSTALL_PATH%\csmart\data\database\scripts\mysql\load_csmart_data.bat %1 %2 %3

ECHO Done.

:L_END
