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


REM Load the RefConfiguration data contained in csmart/data/database/ref-csv
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
IF NOT "%3" == "" GOTO L_3
ECHO Usage: load_ref_data.bat [RefConfig DB Username] [Password] [MySQL RefConfig database name]
GOTO L_END

:L_3


COPY %COUGAAR_INSTALL_PATH%\csmart\data\database\scripts\mysql\sql\load_ref_data.sql %COUGAAR_INSTALL_PATH%\csmart\data\database\scripts\mysql\sql\loadRef.sql

ECHO Doing sed...

REM First write the basic script to a file, with the CIP
ECHO s/:cip/%COUGAAR_INSTALL_PATH%/g > cip.txt

REM Then double the backslashes
%COUGAAR_INSTALL_PATH%\csmart\data\database\scripts\sed.exe "s/\\/\\\\\\\\/g" cip.txt > script.txt

REM then do the real substitution
%COUGAAR_INSTALL_PATH%\csmart\data\database\scripts\sed.exe -f script.txt %COUGAAR_INSTALL_PATH%\csmart\data\database\scripts\mysql\sql\loadRef.sql > loadRef_new.sql

DEL cip.txt
DEL script.txt
DEL %COUGAAR_INSTALL_PATH%\csmart\data\database\scripts\mysql\sql\loadRef.sql

REM Copy most files from the normal CSV dir. They are by definition the same

IF NOT EXIST %COUGAAR_INSTALL_PATH%\csmart\data\database\csv\alploc.csv GOTO L_4
COPY %COUGAAR_INSTALL_PATH%\csmart\data\database\csv\alploc.csv %COUGAAR_INSTALL_PATH%\csmart\data\database\ref-csv

IF EXIST %COUGAAR_INSTALL_PATH%\csmart\data\database\csv\fdm_transportable_item.csv COPY %COUGAAR_INSTALL_PATH%\csmart\data\database\csv\fdm_transportable_item.csv %COUGAAR_INSTALL_PATH%\csmart\data\database\ref-csv

IF EXIST %COUGAAR_INSTALL_PATH%\csmart\data\database\csv\fdm_transportable_item_detail.csv COPY %COUGAAR_INSTALL_PATH%\csmart\data\database\csv\fdm_transportable_item_detail.csv %COUGAAR_INSTALL_PATH%\csmart\data\database\ref-csv

IF EXIST %COUGAAR_INSTALL_PATH%\csmart\data\database\csv\fdm_unfrmd_srvc_occ_rnk_subcat.csv COPY %COUGAAR_INSTALL_PATH%\csmart\data\database\csv\fdm_unfrmd_srvc_occ_rnk_subcat.csv %COUGAAR_INSTALL_PATH%\csmart\data\database\ref-csv

IF EXIST %COUGAAR_INSTALL_PATH%\csmart\data\database\csv\fdm_unfrmd_srvc_occptn.csv COPY %COUGAAR_INSTALL_PATH%\csmart\data\database\csv\fdm_unfrmd_srvc_occptn.csv %COUGAAR_INSTALL_PATH%\csmart\data\database\ref-csv

IF EXIST %COUGAAR_INSTALL_PATH%\csmart\data\database\csv\fdm_unfrmd_srvc_rnk.csv COPY %COUGAAR_INSTALL_PATH%\csmart\data\database\csv\fdm_unfrmd_srvc_rnk.csv %COUGAAR_INSTALL_PATH%\csmart\data\database\ref-csv

IF EXIST %COUGAAR_INSTALL_PATH%\csmart\data\database\csv\fdm_unit.csv COPY %COUGAAR_INSTALL_PATH%\csmart\data\database\csv\fdm_unit.csv %COUGAAR_INSTALL_PATH%\csmart\data\database\ref-csv

IF EXIST %COUGAAR_INSTALL_PATH%\csmart\data\database\csv\fdm_unit_billet.csv COPY %COUGAAR_INSTALL_PATH%\csmart\data\database\csv\fdm_unit_billet.csv %COUGAAR_INSTALL_PATH%\csmart\data\database\ref-csv

IF EXIST %COUGAAR_INSTALL_PATH%\csmart\data\database\csv\fdm_unit_equipment.csv COPY %COUGAAR_INSTALL_PATH%\csmart\data\database\csv\fdm_unit_equipment.csv %COUGAAR_INSTALL_PATH%\csmart\data\database\ref-csv

IF EXIST %COUGAAR_INSTALL_PATH%\csmart\data\database\csv\geoloc.csv COPY %COUGAAR_INSTALL_PATH%\csmart\data\database\csv\geoloc.csv %COUGAAR_INSTALL_PATH%\csmart\data\database\ref-csv

IF EXIST %COUGAAR_INSTALL_PATH%\csmart\data\database\csv\lib_pg_attribute.csv COPY %COUGAAR_INSTALL_PATH%\csmart\data\database\csv\lib_pg_attribute.csv %COUGAAR_INSTALL_PATH%\csmart\data\database\ref-csv

IF EXIST %COUGAAR_INSTALL_PATH%\csmart\data\database\csv\lib_organization.csv COPY %COUGAAR_INSTALL_PATH%\csmart\data\database\csv\lib_organization.csv %COUGAAR_INSTALL_PATH%\csmart\data\database\ref-csv

IF EXIST %COUGAAR_INSTALL_PATH%\csmart\data\database\csv\oplan.csv COPY %COUGAAR_INSTALL_PATH%\csmart\data\database\csv\oplan.csv %COUGAAR_INSTALL_PATH%\csmart\data\database\ref-csv

IF EXIST %COUGAAR_INSTALL_PATH%\csmart\data\database\csv\oplan_agent_attr.csv COPY %COUGAAR_INSTALL_PATH%\csmart\data\database\csv\oplan_agent_attr.csv %COUGAAR_INSTALL_PATH%\csmart\data\database\ref-csv
ECHO Past all copy
GOTO L_5

:L_4
ECHO CSV files in csmart/data/database/csv missing for reference tables, and required!
GOTO L_END

:L_5
REM Done copying over files

FOR %%y in (%COUGAAR_INSTALL_PATH%\csmart\data\database\ref-csv\*.csv) DO COPY %%y %%y.tmp

ECHO Loading '.csv' files to database %3 in user %1
mysql -u%1 -p%2 %3 < loadRef_new.sql

DEL loadRef_new.sql
DEL %COUGAAR_INSTALL_PATH%\csmart\data\database\ref-csv\*.tmp
DEL %COUGAAR_INSTALL_PATH%\csmart\data\database\ref-csv\*loc.csv
DEL %COUGAAR_INSTALL_PATH%\csmart\data\database\ref-csv\fdm*
DEL %COUGAAR_INSTALL_PATH%\csmart\data\database\ref-csv\oplan*
DEL %COUGAAR_INSTALL_PATH%\csmart\data\database\ref-csv\lib*

ECHO Done.

:L_END

