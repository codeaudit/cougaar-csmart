options (
  skip=1)
load
  infile 'V3_ASB_AGENT_RELATION.csv'
into table V3_ASB_AGENT_RELATION
  insert
  fields
  (ASSEMBLY_ID                   char terminated by ',' optionally enclosed by '"',
   ROLE                          char terminated by ',' optionally enclosed by '"',
   SUPPORTING_COMPONENT_ID       char terminated by ',' optionally enclosed by '"',
   SUPPORTED_COMPONENT_ID        char terminated by ',' optionally enclosed by '"',
   START_DATE                    char terminated by ',' optionally enclosed by '"',
   END_DATE                      char terminated by ',' optionally enclosed by '"'
  )
