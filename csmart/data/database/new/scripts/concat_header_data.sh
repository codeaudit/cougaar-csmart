
# <copyright>
#  Copyright 2003 BBNT Solutions, LLC
#  under sponsorship of the Defense Advanced Research Projects Agency (DARPA).
# 
#  This program is free software; you can redistribute it and/or modify
#  it under the terms of the Cougaar Open Source License as published by
#  DARPA on the Cougaar Open Source Website (www.cougaar.org).
# 
#  THE COUGAAR SOFTWARE AND ANY DERIVATIVE SUPPLIED BY LICENSOR IS
#  PROVIDED 'AS IS' WITHOUT WARRANTIES OF ANY KIND, WHETHER EXPRESS OR
#  IMPLIED, INCLUDING (BUT NOT LIMITED TO) ALL IMPLIED WARRANTIES OF
#  MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE, AND WITHOUT
#  ANY WARRANTIES AS TO NON-INFRINGEMENT.  IN NO EVENT SHALL COPYRIGHT
#  HOLDER BE LIABLE FOR ANY DIRECT, SPECIAL, INDIRECT OR CONSEQUENTIAL
#  DAMAGES WHATSOEVER RESULTING FROM LOSS OF USE OF DATA OR PROFITS,
#  TORTIOUS CONDUCT, ARISING OUT OF OR IN CONNECTION WITH THE USE OR
#  PERFORMANCE OF THE COUGAAR SOFTWARE.
# </copyright>

HEADER_PATH=${COUGAAR_INSTALL_PATH}/csmart/data/database/new/headers
DATA_PATH=${COUGAAR_INSTALL_PATH}/csmart/data/database/new/raw_data
CSV_PATH=${COUGAAR_INSTALL_PATH}/csmart/data/database/new/csv


cat $HEADER_PATH/org_pg_attr_header.csv $DATA_PATH/org_pg_attr_data.csv > $CSV_PATH/org_pg_attr.csv
cat $HEADER_PATH/org_relation_header.csv $DATA_PATH/org_relation_data.csv > $CSV_PATH/org_relation.csv
cat $HEADER_PATH/lib_pg_attribute_header.csv $DATA_PATH/lib_pg_attribute_data.csv > $CSV_PATH/lib_pg_attribute.csv

cat $HEADER_PATH/lib_organization_header.csv $DATA_PATH/lib_organization_data.csv > $CSV_PATH/lib_organization.csv
cat $HEADER_PATH/oplan_agent_attr_header.csv $DATA_PATH/oplan_agent_attr_data.csv > $CSV_PATH/oplan_agent_attr.csv
cat $HEADER_PATH/lib_oplan_header.csv $DATA_PATH/lib_oplan_data.csv > $CSV_PATH/lib_oplan.csv
cat $HEADER_PATH/alploc_header.csv $DATA_PATH/alploc_data.csv > $CSV_PATH/alploc.csv
