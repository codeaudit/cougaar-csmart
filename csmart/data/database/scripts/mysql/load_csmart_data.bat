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

REM Load the Configuration data contained in csmart/data/database/csv
REM This data supplies data for the cougaar.rc entry
REM org.cougaar.configuration.database

REM Note that MySQL must be installed on the local machine, and
REM Cougaar Install Path must be set

REM Make sure that COUGAAR_INSTALL_PATH is specified
IF NOT "%COUGAAR_INSTALL_PATH%" == "" GOTO L_2
REM Unable to find cougaar-install-path
ECHO COUGAAR_INSTALL_PATH not set!
GOTO L_END
:L_2

REM Check arguments. If got none, display usage
IF NOT "%3" == "" GOTO L_3
  ECHO Load only the data for controlling CSMART into the named database.
  ECHO Usage: load_csmart_data.bat [DB Username] [password] [MySQL database name]
GOTO L_END

:L_3

COPY %COUGAAR_INSTALL_PATH%\csmart\data\database\scripts\mysql\sql\csmart-db.drop-mysql-tables.sql %COUGAAR_INSTALL_PATH%\csmart\data\database\scripts\mysql\sql\dropTab.sql
COPY %COUGAAR_INSTALL_PATH%\csmart\data\database\scripts\mysql\sql\csmart-db.create-mysql-tables.sql %COUGAAR_INSTALL_PATH%\csmart\data\database\scripts\mysql\sql\creatTab.sql
COPY %COUGAAR_INSTALL_PATH%\csmart\data\database\scripts\mysql\sql\csmart-db.load-mysql-tables.sql %COUGAAR_INSTALL_PATH%\csmart\data\database\scripts\mysql\sql\loadTab.sql
COPY %COUGAAR_INSTALL_PATH%\csmart\data\database\scripts\mysql\sql\csmart-db.create-mysql-indexes.sql %COUGAAR_INSTALL_PATH%\csmart\data\database\scripts\mysql\sql\creatInd.sql

ECHO Dropping tables from database.
mysql -u%1 -p%2 %3 < %COUGAAR_INSTALL_PATH%\csmart\data\database\scripts\mysql\sql\dropTab.sql

ECHO Creating tables in database.
mysql -u%1 -p%2 %3 < %COUGAAR_INSTALL_PATH%\csmart\data\database\scripts\mysql\sql\creatTab.sql

ECHO Doing sed...

REM First write the basic script to a file, with the CIP
ECHO s/:cip/%COUGAAR_INSTALL_PATH%/g > cip.txt

REM Then double the backslashes
%COUGAAR_INSTALL_PATH%\csmart\data\database\scripts\sed.exe "s/\\/\\\\\\\\/g" cip.txt > script.txt

REM then do the real substitution
%COUGAAR_INSTALL_PATH%\csmart\data\database\scripts\sed.exe -f script.txt %COUGAAR_INSTALL_PATH%\csmart\data\database\scripts\mysql\sql\loadTab.sql > load_mysql_db_new.sql

DEL cip.txt
DEL script.txt
DEL %COUGAAR_INSTALL_PATH%\csmart\data\database\scripts\mysql\sql\dropTab.sql
DEL %COUGAAR_INSTALL_PATH%\csmart\data\database\scripts\mysql\sql\creatTab.sql
DEL %COUGAAR_INSTALL_PATH%\csmart\data\database\scripts\mysql\sql\loadTab.sql

FOR %%y in (%COUGAAR_INSTALL_PATH%\csmart\data\database\csv\*.csv) DO COPY %%y %%y.tmp

ECHO Loading '.csv' files to database.
mysql -u%1 -p%2 %3 < load_mysql_db_new.sql

ECHO Creating indexes in database tables.
mysql -u%1 -p%2 %3 < %COUGAAR_INSTALL_PATH%\csmart\data\database\scripts\mysql\sql\creatInd.sql

DEL load_mysql_db_new.sql
DEL %COUGAAR_INSTALL_PATH%\csmart\data\database\csv\*.tmp
DEL %COUGAAR_INSTALL_PATH%\csmart\data\database\scripts\mysql\sql\creatInd.sql

ECHO Done loading csmart config data.

:L_END
