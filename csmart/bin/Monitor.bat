@ECHO OFF

REM Script to run the CSMART Society Monitor as a standalone

REM calls setlibpath.bat which sets the path to the required jar files.
CALL %COUGAAR_INSTALL_PATH%\bin\setlibpath.bat

REM Use the Bootstrapper to find Jar files.

REM Monitor required Jars are listed here for convenience.

REM add csmart.jar
rem SET LIBPATHS=%LIBPATHS%;%COUGAAR_INSTALL_PATH%\lib\csmart.jar

REM for CSMART environment, use the following
rem SET LIBPATHS=%LIBPATHS%;%COUGAAR_INSTALL_PATH%\lib\server.jar

REM Plus these third party jar files, which are in CIP\sys
rem SET LIBPATHS=%LIBPATHS%;%COUGAAR_INSTALL_PATH%\sys\xerces.jar
rem SET LIBPATHS=%LIBPATHS%;%COUGAAR_INSTALL_PATH%\sys\jcchart451K.jar
rem SET LIBPATHS=%LIBPATHS%;%COUGAAR_INSTALL_PATH%\sys\grappa1_2_bbn.jar

SET MYMEMORY=-Xms100m -Xmx300m
SET MYPROPERTIES=-Dorg.cougaar.install.path=%COUGAAR_INSTALL_PATH%
SET MYCONFIGPATH=-Dorg.cougaar.config.path="%COUGAAR_INSTALL_PATH%/csmart/data/common/\;"

@ECHO ON

java.exe %MYPROPERTIES% %MYMEMORY% %MYCONFIGPATH% -classpath %LIBPATHS% org.cougaar.core.society.Bootstrapper org.cougaar.tools.csmart.ui.monitor.viewer.CSMARTUL

