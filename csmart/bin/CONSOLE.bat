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

REM Main script to run CONSOLE
REM Users may want to customize the location of MS Excel

REM Make sure that COUGAAR_INSTALL_PATH is specified
IF NOT "%COUGAAR_INSTALL_PATH%" == "" GOTO L_2
REM Unable to find cougaar-install-path
ECHO COUGAAR_INSTALL_PATH not set!
GOTO L_END
:L_2

REM Make sure that COUGAAR3RDPARTY is specified
IF NOT "%COUGAAR3RDPARTY%" == "" GOTO L_3
REM Unable to find "sys" path for 3rd-party jars
REM This is usually COUGAAR_INSTALL_PATH/sys
ECHO COUGAAR3RDPARTY not set! Defaulting to CIP\sys
SET COUGAAR3RDPARTY=%COUGAAR_INSTALL_PATH%\sys
:L_3

REM The following line is optional. Some output files are written to the working directory.
REM CD %TEMP%

REM You may use the optional environment variable COUGAAR_DEV_PATH
REM to point to custom developed code that is not in COUGAR_INSTALL_PATH/lib
REM or CIP/sys. This can be one or many semicolon separated 
REM directories/jars/zips, or left undefined

REM start the classpath with the optional COUGAAR_DEV_PATH
SET DEVPATH=
IF NOT "%COUGAAR_DEV_PATH%" == "" SET DEVPATH=-Dorg.cougaar.class.path="%COUGAAR_DEV_PATH%"

REM Start CONSOLE using Bootstrapper
SET MYCLASSPATH="%COUGAAR_INSTALL_PATH%\lib\bootstrap.jar"

SET MYMEMORY=-Xms100m -Xmx300m
SET MYPROPERTIES=-Dorg.cougaar.install.path="%COUGAAR_INSTALL_PATH%" -Dorg.cougaary.system.path="%COUGAAR3RDPARTY%"
SET MYCONFIGPATH=-Dorg.cougaar.config.path="%COUGAAR_INSTALL_PATH%/csmart/data/common/\;%COUGAAR_INSTALL_PATH%/configs/minitestconfig/\;"

@ECHO ON

java.exe %MYPROPERTIES% %MYMEMORY% %MYCONFIGPATH% %DEVPATH% -classpath %MYCLASSPATH% org.cougaar.bootstrap.Bootstrapper org.cougaar.tools.csmart.ui.console.CSMARTConsoleView

:L_END
