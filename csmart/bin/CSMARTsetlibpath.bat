@ECHO OFF

REM CSMART version of CIP/bin/setlibpath.bat
REM Nothing special being done here: Just be sure to check
REM COUGAAR_DEV_PATH first, to support developers.

SET LIBPATHS=%COUGAAR_DEV_PATH%
SET LIBPATHS=%LIBPATHS%;%COUGAAR_INSTALL_PATH%\lib\core.jar

SET COUGAAR3RDPARTY=

