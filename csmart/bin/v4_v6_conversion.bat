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


REM Script to scrub pre-9.4.1 database exports. Table names
REM have changed. Run this on any recipe exports before trying
REM to import them into a 9.4.1+ database.


REM Make sure that COUGAAR_INSTALL_PATH is specified
IF NOT "%COUGAAR_INSTALL_PATH%" == "" GOTO L_2
REM Unable to find cougaar-install-path
ECHO COUGAAR_INSTALL_PATH not set!
GOTO L_END
:L_2

REM Check arguments. If got none, display usage
IF NOT "%1" == "" GOTO L_3
ECHO Usage: v4_v6_conversion.bat [FilenameToConvert.sql]
GOTO L_END

:L_3


MV %1 oldfile.sql

%COUGAAR_INSTALL_PATH%\csmart\data\database\scripts\sed.exe s/v4_//g oldfile.sql > midfile1.sql
%COUGAAR_INSTALL_PATH%\csmart\data\database\scripts\sed.exe s/V4_//g midfile1.sql > midfile2.sql
%COUGAAR_INSTALL_PATH%\csmart\data\database\scripts\sed.exe s/v6_//g midfile2.sql > midfile3.sql
%COUGAAR_INSTALL_PATH%\csmart\data\database\scripts\sed.exe s/V6_//g midfile3.sql > %1

rm oldfile.sql
rm midfile1.sql
rm midfile2.sql
rm midfile3.sql


ECHO Done.

:L_END
