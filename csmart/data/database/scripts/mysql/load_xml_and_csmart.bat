@ECHO OFF

REM "<copyright>"
REM " "
REM " Copyright 2001-2004 BBNT Solutions, LLC"
REM " under sponsorship of the Defense Advanced Research Projects"
REM " Agency (DARPA)."
REM ""
REM " You can redistribute this software and/or modify it under the"
REM " terms of the Cougaar Open Source License as published on the"
REM " Cougaar Open Source Website (www.cougaar.org)."
REM ""
REM " THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS"
REM " "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT"
REM " LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR"
REM " A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT"
REM " OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,"
REM " SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT"
REM " LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,"
REM " DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY"
REM " THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT"
REM " (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE"
REM " OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE."
REM " "
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
