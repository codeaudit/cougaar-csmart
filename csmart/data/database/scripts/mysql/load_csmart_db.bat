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
