options (
  skip=1)
load
  infile 'V3_ASB_AGENT_PG_ATTR.csv'
into table V3_ASB_AGENT_PG_ATTR
  insert
  fields
  (ASSEMBLY_ID             char terminated by ',' optionally enclosed by '"',
   COMPONENT_ID            char terminated by ',' optionally enclosed by '"',
   PG_ATTRIBUTE_LIB_ID     char terminated by ',' optionally enclosed by '"',
   ATTRIBUTE_VALUE         char terminated by ',' optionally enclosed by '"',
   ATTRIBUTE_ORDER         char terminated by ',' optionally enclosed by '"',
   START_DATE              char terminated by ',' optionally enclosed by '"',
   END_DATE                char terminated by ',' optionally enclosed by '"'
  )
