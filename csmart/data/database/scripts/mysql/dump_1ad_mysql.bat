@ECHO OFF

REM <copyright>
REM Copyright 2001,2002 BBNT Solutions, LLC
REM under sponsorship of the Defense Advanced Research Projects Agency (DARPA).

REM This program is free software; you can redistribute it and/or modify
REM it under the terms of the Cougaar Open Source License as published by
REM DARPA on the Cougaar Open Source Website (www.cougaar.org).

REM THE COUGAAR SOFTWARE AND ANY DERIVATIVE SUPPLIED BY LICENSOR IS
REM PROVIDED 'AS IS' WITHOUT WARRANTIES OF ANY KIND, WHETHER EXPRESS OR
REM IMPLIED, INCLUDING (BUT NOT LIMITED TO) ALL IMPLIED WARRANTIES OF
REM MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE, AND WITHOUT
REM ANY WARRANTIES AS TO NON-INFRINGEMENT.  IN NO EVENT SHALL COPYRIGHT
REM HOLDER BE LIABLE FOR ANY DIRECT, SPECIAL, INDIRECT OR CONSEQUENTIAL
REM DAMAGES WHATSOEVER RESULTING FROM LOSS OF USE OF DATA OR PROFITS,
REM TORTIOUS CONDUCT, ARISING OUT OF OR IN CONNECTION WITH THE USE OR
REM PERFORMANCE OF THE COUGAAR SOFTWARE.
REM </copyright>

REM Make sure that COUGAAR_INSTALL_PATH is specified
IF NOT "%COUGAAR_INSTALL_PATH%" == "" GOTO L_2
REM Unable to find cougaar-install-path
ECHO COUGAAR_INSTALL_PATH not set!
GOTO L_END

:L_2

REM Check arguments. If got none, display usage
IF NOT "%3" == "" GOTO L_3
ECHO Usage: dump_1ad_mysql.bat [Config DB Username] [Password] [MySQL Config DB database name]
GOTO L_END

:L_3

REM Check to make sure csv directory exists already and if not create it
IF EXIST %COUGAAR_INSTALL_PATH%\csmart\data\database\csv GOTO L_4
MKDIR %COUGAAR_INSTALL_PATH%\csmart\data\database\csv
GOTO L_5

:L_4

REM Check to make sure that the '.csv' files do not already exist in directory
REM Use below file as test case.
IF NOT EXIST %COUGAAR_INSTALL_PATH%\csmart\data\database\csv\alib_component.csv  GOTO L_5
ECHO '.csv' files already exist in CIP\csmart\data\database\csv directory.
ECHO You must either delete them or move them to a new directory.
GOTO L_END

:L_5

MD %COUGAAR_INSTALL_PATH%\csmart\data\database\raw_data
REM First write the basic script to a file, with the CIP
ECHO s/:cip/%COUGAAR_INSTALL_PATH%/g > cip.txt

REM Then double the backslashes
%COUGAAR_INSTALL_PATH%\csmart\data\database\scripts\sed.exe "s/\\/\\\\\\\\/g" cip.txt > script.txt

REM then do the real substitution
%COUGAAR_INSTALL_PATH%\csmart\data\database\scripts\sed.exe -f script.txt %COUGAAR_INSTALL_PATH%\csmart\data\database\scripts\mysql\sql\dump_db.sql > dump_mysql_db_new.sql

DEL cip.txt
DEL script.txt

ECHO Dumping database to .csv files.
mysql -u%1 -p%2 %3 < dump_mysql_db_new.sql
DEL dump_mysql_db_new.sql

ECHO Concatenating files.
CALL concat_header_data.bat

DEL %COUGAAR_INSTALL_PATH%\csmart\data\database\raw_data\*.csv
RD %COUGAAR_INSTALL_PATH%\csmart\data\database\raw_data
ECHO Done.

:L_END



