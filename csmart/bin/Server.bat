@ECHO OFF

REM Sample script for running the Node Server, for use with CSMART
REM Be sure to edit the MYPROPFILE property below

REM The following line is optional. The server writes configuration
REM    files to its working directory.
REM CD %TEMP%

CALL %COUGAAR_INSTALL_PATH%\bin\setlibpath.bat
CALL %COUGAAR_INSTALL_PATH%\bin\setarguments.bat

SET LIBPATHS=%LIBPATHS%;%COUGAAR_INSTALL_PATH%\lib\csmart.jar
SET LIBPATHS=%LIBPATHS%;%COUGAAR_INSTALL_PATH%\lib\server.jar

REM Set the location of your site-specific properties file
REM Use the sample included in the data directory, editing it
REM to reflect your local setting of COUGAAR_INSTALL_PATH, etc
SET MYPROPFILE=%COUGAAR_INSTALL_PATH%\csmart\data\win-server-sample.props

@ECHO ON

java %MYPROPERTIES% %MYMEMORY% -classpath %LIBPATHS% org.cougaar.tools.server.NodeServer %MYPROPFILE%
