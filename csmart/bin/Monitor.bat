@ECHO OFF

REM Script to run the CSMART Society Monitor as a standalone

REM calls setlibpath.bat which sets the path to the required jar files.
CALL %COUGAAR_INSTALL_PATH%\bin\setlibpath.bat

REM Due to some static members, the Society Monitor cannot rely on the
REM Bootstrapper to find all required Jars. All the peculiar Society
REM Monitor required Jars are listed here for convenience.

REM add csmart.jar Before other items
SET LIBPATHS=%COUGAAR_INSTALL_PATH%\lib\csmart.jar;%LIBPATHS%

REM for CSMART environment, use the following
rem SET LIBPATHS=%LIBPATHS%;%COUGAAR_INSTALL_PATH%\lib\server.jar

REM Plus these third party jar files, which are in CIP\sys - Note the two that must be explicitly loaded
SET LIBPATHS=%LIBPATHS%;%COUGAAR_INSTALL_PATH%\sys\xerces.jar
rem SET LIBPATHS=%LIBPATHS%;%COUGAAR_INSTALL_PATH%\sys\jcchart451K.jar
SET LIBPATHS=%LIBPATHS%;%COUGAAR_INSTALL_PATH%\sys\grappa1_2_bbn.jar

SET MYMEMORY=-Xms100m -Xmx300m
SET MYPROPERTIES=-Dorg.cougaar.install.path=%COUGAAR_INSTALL_PATH%
SET MYCONFIGPATH=-Dorg.cougaar.config.path="%COUGAAR_INSTALL_PATH%/csmart/data/common/\;"

@ECHO ON

java.exe %MYPROPERTIES% %MYMEMORY% %MYCONFIGPATH% -classpath %LIBPATHS% org.cougaar.tools.csmart.ui.monitor.viewer.CSMARTUL

