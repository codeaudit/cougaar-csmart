#!/bin/sh

# 
# <copyright>
# Copyright 2001,2002 BBNT Solutions, LLC
# under sponsorship of the Defense Advanced Research Projects Agency (DARPA).

# This program is free software; you can redistribute it and/or modify
# it under the terms of the Cougaar Open Source License as published by
# DARPA on the Cougaar Open Source Website (www.cougaar.org).

# THE COUGAAR SOFTWARE AND ANY DERIVATIVE SUPPLIED BY LICENSOR IS
# PROVIDED 'AS IS' WITHOUT WARRANTIES OF ANY KIND, WHETHER EXPRESS OR
# IMPLIED, INCLUDING (BUT NOT LIMITED TO) ALL IMPLIED WARRANTIES OF
# MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE, AND WITHOUT
# ANY WARRANTIES AS TO NON-INFRINGEMENT.  IN NO EVENT SHALL COPYRIGHT
# HOLDER BE LIABLE FOR ANY DIRECT, SPECIAL, INDIRECT OR CONSEQUENTIAL
# DAMAGES WHATSOEVER RESULTING FROM LOSS OF USE OF DATA OR PROFITS,
# TORTIOUS CONDUCT, ARISING OUT OF OR IN CONNECTION WITH THE USE OR
# PERFORMANCE OF THE COUGAAR SOFTWARE.
# </copyright>

if [ "x$1" = "x" ]; then
  echo "Usage: v4_v6_conversion.sh [FilenameToConvert.sql] "
  exit
fi

mv $1 oldfile.sql

sed s/v4_// oldfile.sql > midfile.sql
sed s/v6_// midfile.sql > $1

rm oldfile.sql
rm midfile.sql

echo "Done."
