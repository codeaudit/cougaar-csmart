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

REM This script loads all data necessary for running from either XML or CSMART
REM into the single named database. All of the following cougaar.rc entries
REM are covered, and all should point to the single named database as provided in the argument.
REM org.cougaar.database
REM org.cougaar.oplan.database
REM org.cougaar.configuration.database
REM org.cougaar.refconfig.database
REM blackjack.database
REM icis.database
REM fcs.database

REM Note that this script requires a "jar.exe" on your path, available with a JDK installation

REM Make sure that COUGAAR_INSTALL_PATH is specified
IF NOT "%COUGAAR_INSTALL_PATH%" == "" GOTO L_2
REM Unable to find cougaar-install-path
ECHO COUGAAR_INSTALL_PATH not set!
GOTO L_END

:L_2
REM Check arguments. If got none, display usage
IF NOT "%3" == "" GOTO L_3
    ECHO Load all data necessary for running from CSMART or XML into a single database.
    ECHO Usage: load_xml_and_csmart.sh [DB username] [password] [database]
    GOTO L_END

:L_3

IF EXIST %COUGAAR_INSTALL_PATH%\csmart\data\database\scripts\mysql\load_csmart_db.bat GOTO L_4
ECHO Missing Load script %COUGAAR_INSTALL_PATH%\csmart\data\database\scripts\mysql\load_csmart_db.bat
GOTO L_END

:L_4
Echo Using load_csmart_db.bat script to load Domain and CSMART data
CALL %COUGAAR_INSTALL_PATH%\csmart\data\database\scripts\mysql\load_csmart_db.bat %1 %2 %3

REM Now the xml refconfig data
IF EXIST %COUGAAR_INSTALL_PATH%\csmart\data\database\scripts\mysql\load_ref_data.bat GOTO L_5
ECHO Cannot find %COUGAAR_INSTALL_PATH%\csmart\data\database\scripts\mysql\load_ref_data.bat
GOTO L_END

:L_5
ECHO   - Loading the xml refconfig database tables.
CALL %COUGAAR_INSTALL_PATH%\csmart\data\database\scripts\mysql\load_ref_data.bat %1 %2 %3

REM Now the ua domain data
IF NOT EXIST %COUGAAR_INSTALL_PATH%\fcsua\ GOTO L_END
IF EXIST %COUGAAR_INSTALL_PATH%\fcsua\data\database\scripts\mysql\load_ua_domain_data.bat GOTO L_7
ECHO Cannot find %COUGAAR_INSTALL_PATH%\fcsua\data\database\scripts\mysql\load_ua_domain_data.bat
GOTO L_END

:L_7
ECHO   - Loading the ua domain database tables.
CALL %COUGAAR_INSTALL_PATH%\fcsua\data\database\scripts\mysql\load_ua_domain_data.bat %1 %2 %3

:L_END
