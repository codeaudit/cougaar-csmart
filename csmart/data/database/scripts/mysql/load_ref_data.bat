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


REM Load the RefConfiguration data contained in
REM csmart/data/database/ref-csv for use when running from XML files
REM This fills in data for the cougaar.rc entry
REM org.cougaar.refconfig.database

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

IF EXIST %COUGAAR_INSTALL_PATH%\csmart\data\database\csv\lib_pg_attribute.csv COPY %COUGAAR_INSTALL_PATH%\csmart\data\database\csv\lib_pg_attribute.csv %COUGAAR_INSTALL_PATH%\csmart\data\database\ref-csv

IF EXIST %COUGAAR_INSTALL_PATH%\csmart\data\database\csv\lib_organization.csv COPY %COUGAAR_INSTALL_PATH%\csmart\data\database\csv\lib_organization.csv %COUGAAR_INSTALL_PATH%\csmart\data\database\ref-csv

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
DEL %COUGAAR_INSTALL_PATH%\csmart\data\database\ref-csv\lib*

ECHO Done.

:L_END

