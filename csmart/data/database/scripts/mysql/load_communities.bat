@ECHO OFF

REM "<copyright>"
REM " Copyright 2001,2002 BBNT Solutions, LLC"
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

REM Load the 9.2 format community data contained in csmart\data\database\csv
REM This data must be hand generated there,
REM like the existing community_*.csv files, but without
REM the assembly_id column.
REM Move the original data aside, and create your own files,
REM or use the community editor from CSMART, or create a 
REM communities.xml file and load it in from CSMART

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
ECHO Usage: load_communities.bat [Config DB Username] [Password] [MySQL Config DB database name][Assembly ID]
ECHO          -- Deletes all rows for given assembly_id, then loads 2 community csv
ECHO             files to db under the given assembly_id.  If no Assembly ID is provided,
ECHO             COMM-DEFAULT_CONFIG is used as the assembly_id.  Will only load csv files 
ECHO             that do not have an ASSEMBLY_ID column.
GOTO L_END
:L_3

REM Check for presence of community csv files.
IF EXIST %COUGAAR_INSTALL_PATH%\csmart\data\database\scripts\mysql\community_attribute.csv GOTO L_4
ECHO Cannot find %COUGAAR_INSTALL_PATH%\csmart\data\database\scripts\mysql\community_attribute.csv
ECHO You must place the community csv files you are trying to load in the
ECHO %COUGAAR_INSTALL_PATH%\csmart\data\database\scripts\mysql directory.
GOTO L_END
:L_4

IF EXIST %COUGAAR_INSTALL_PATH%\csmart\data\database\scripts\mysql\community_entity_attribute.csv GOTO L_5
ECHO Cannot find %COUGAAR_INSTALL_PATH%\csmart\data\database\scripts\mysql\community_entity_attribute.csv
ECHO You must place the community csv files you are trying to load in the 
ECHO %COUGAAR_INSTALL_PATH%\csmart\data\database\scripts\mysql directory.
GOTO L_END

:L_5

IF NOT "%4" == "" GOTO L_6 
SET asbly_id=COMM-DEFAULT_CONFIG
GOTO L_7
:L_6
SET asbly_id=%4

:L_7

REM First write the basic script to a file, with the CIP
ECHO s/:cip/%COUGAAR_INSTALL_PATH%/g > cip.txt

REM Then double the backslashes
%COUGAAR_INSTALL_PATH%\csmart\data\database\scripts\sed.exe "s/\\/\\\\\\\\/g" cip.txt > script.txt

REM then do the real substitution
%COUGAAR_INSTALL_PATH%\csmart\data\database\scripts\sed.exe -f script.txt %COUGAAR_INSTALL_PATH%\csmart\data\database\scripts\mysql\sql\load_comm.sql > load_comm_db_new.sql

DEL cip.txt
DEL script.txt
REM copy the .csv files to a .csv.tmp version as expected by sql load script
FOR %%y in (%COUGAAR_INSTALL_PATH%\csmart\data\database\scripts\mysql\*.csv) DO COPY %%y %%y.tmp

REM Loading only community csv files with no assembly id
ECHO Loading '.csv' files to database %3.
mysql -u%1 -p%2 %3 < load_comm_db_new.sql

DEL load_comm_db_new.sql
DEL %COUGAAR_INSTALL_PATH%\csmart\data\database\scripts\mysql\*.tmp

REM Replace ':asb_id' in sql script with given assembly_id and update database
%COUGAAR_INSTALL_PATH%\csmart\data\database\scripts\sed.exe s/:asb_id/%asbly_id%/g %COUGAAR_INSTALL_PATH%\csmart\data\database\scripts\mysql\sql\update_comm.sql > update_comm_new.sql

REM run the update script to set the assembly id to the given id
mysql -u%1 -p%2 %3 < update_comm_new.sql
DEL update_comm_new.sql

ECHO Done.

:L_END
