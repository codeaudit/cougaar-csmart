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
