@echo OFF

REM 
REM <copyright>
REM  
REM  Copyright 2001-2004 BBNT Solutions, LLC
REM  under sponsorship of the Defense Advanced Research Projects
REM  Agency (DARPA).
REM 
REM  You can redistribute this software and/or modify it under the
REM  terms of the Cougaar Open Source License as published on the
REM  Cougaar Open Source Website (www.cougaar.org).
REM 
REM  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
REM  "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
REM  LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
REM  A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
REM  OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
REM  SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
REM  LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
REM  DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
REM  THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
REM  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
REM  OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
REM  
REM </copyright>

REM Delete the temporary database created when copying
REM or exporting an experiment or recipe
if [%3] == [] (
  echo Usage: delete-temp-db.bat [Config DB Username] [Password] [MySQL Config DB database name] [Optional: MySQL DB host name]
  GOTO L_END
)

if [%4] == [] (
  mysql -f -e "drop database tempcopy;" -u %1 -p%2 %3
) else (
  mysql -f -e "drop database tempcopy;" -u %1 -p%2 -h %4 %3
)

echo Temporary database removed.

:L_END
