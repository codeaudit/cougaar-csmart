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


REM Load the Configuration data contained in csmart/data/database/csv
REM This data comes with the distribution.
REM You may also move the original data aside, and "dump" the data from a database
REM for sharing, or editing and reloading
REM Note that MySQL must be installed on the local machine, and
REM Cougaar Install Path must be set


REM Make sure that COUGAAR_INSTALL_PATH is specified
IF NOT "%COUGAAR_INSTALL_PATH%" == "" GOTO L_2
REM Unable to find cougaar-install-path
ECHO COUGAAR_INSTALL_PATH not set!
GOTO L_END
:L_2

REM Check arguments. If got none, display usage
IF NOT "%4" == "" GOTO L_3
ECHO Usage: load_1ad_mysql.bat [DB Username] [Password] [Domain database name][Config database name]
GOTO L_END

:L_3

unzip -d %COUGAAR_INSTALL_PATH%\dbms\data\mysql %COUGAAR_INSTALL_PATH%\dbms\data\mysql\1ADDomainData.zip
mysql -u%1 -p%2 %3 < %COUGAAR_INSTALL_PATH%\dbms\data\mysql\1ADDomainData.sql
DEL %COUGAAR_INSTALL_PATH%\dbms\data\mysql\1ADDomainData.sql

unzip -d %COUGAAR_INSTALL_PATH%\dbms\data\mysql %COUGAAR_INSTALL_PATH%\dbms\data\mysql\army_spares.ZIP
mysql -u%1 -p%2 %3 < %COUGAAR_INSTALL_PATH%\dbms\data\mysql\army.txt
DEL %COUGAAR_INSTALL_PATH%\dbms\data\mysql\army.txt

mysql -u%1 -p%2 %3 < %COUGAAR_INSTALL_PATH%\dbms\data\mysql\spares_index.txt


mysql -u%1 -p%2 %4 < %COUGAAR_INSTALL_PATH%\csmart\data\database\scripts\mysql\sql\drop_v4_v6.sql

COPY %COUGAAR_INSTALL_PATH%\csmart\data\database\scripts\mysql\sql\csmart-db.drop-mysql-tables.sql %COUGAAR_INSTALL_PATH%\csmart\data\database\scripts\mysql\sql\dropTab.sql
COPY %COUGAAR_INSTALL_PATH%\csmart\data\database\scripts\mysql\sql\csmart-db.create-mysql-tables.sql %COUGAAR_INSTALL_PATH%\csmart\data\database\scripts\mysql\sql\creatTab.sql
COPY %COUGAAR_INSTALL_PATH%\csmart\data\database\scripts\mysql\sql\csmart-db.load-mysql-tables.sql %COUGAAR_INSTALL_PATH%\csmart\data\database\scripts\mysql\sql\loadTab.sql
COPY %COUGAAR_INSTALL_PATH%\csmart\data\database\scripts\mysql\sql\csmart-db.create-mysql-indexes.sql %COUGAAR_INSTALL_PATH%\csmart\data\database\scripts\mysql\sql\creatInd.sql


ECHO Dropping tables from database.
mysql -u%1 -p%2 %4 < %COUGAAR_INSTALL_PATH%\csmart\data\database\scripts\mysql\sql\dropTab.sql
ECHO Creating tables in database.
mysql -u%1 -p%2 %4 < %COUGAAR_INSTALL_PATH%\csmart\data\database\scripts\mysql\sql\creatTab.sql

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

ECHO Loading '.csv' files to database %4 in user %1
mysql -u%1 -p%2 %4 < load_mysql_db_new.sql

ECHO Creating indexes in database tables.
mysql -u%1 -p%2 %4 < %COUGAAR_INSTALL_PATH%\csmart\data\database\scripts\mysql\sql\creatInd.sql

DEL load_mysql_db_new.sql
DEL %COUGAAR_INSTALL_PATH%\csmart\data\database\csv\*.tmp
DEL %COUGAAR_INSTALL_PATH%\csmart\data\database\scripts\mysql\sql\creatInd.sql

ECHO Done.

:L_END