options (
  skip=1)
load
  infile 'V3_ASB_OPLAN_AGENT_ATTR.csv'
into table V3_ASB_OPLAN_AGENT_ATTR
  insert
  fields
  (ASSEMBLY_ID       char terminated by ',' optionally enclosed by '"',
   OPLAN_ID          char terminated by ',' optionally enclosed by '"',
   COMPONENT_ID      char terminated by ',' optionally enclosed by '"',
   START_CDAY        char terminated by ',' optionally enclosed by '"',
   END_CDAY          char terminated by ',' optionally enclosed by '"',
   ATTRIBUTE_NAME    char terminated by ',' optionally enclosed by '"',
   ATTRIBUTE_VALUE   char terminated by ',' optionally enclosed by '"'
  )
