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

# Script to scrub pre-9.4.1 database exports. Table names
# have changed. Run this on any recipe exports before trying
# to import them into a 9.4.1+ database.


if [ "x$1" = "x" ]; then
  echo "Usage: v4_v6_conversion.sh [FilenameToConvert.sql] "
  exit
fi

mv $1 oldfile.sql

sed s/v4_//g oldfile.sql > midfile1.sql
sed s/V4_//g midfile1.sql > midfile2.sql
sed s/v6_//g midfile2.sql > midfile3.sql
sed s/V6_//g midfile3.sql > $1

rm oldfile.sql
rm midfile1.sql
rm midfile2.sql
rm midfile3.sql

echo "Done."
