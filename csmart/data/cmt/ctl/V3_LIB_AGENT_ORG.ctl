options (
  skip=1)
load
  infile 'V3_LIB_AGENT_ORG.csv'
into table V3_LIB_AGENT_ORG
  insert
  fields
  (COMPONENT_LIB_ID         char terminated by ',' optionally enclosed by '"',
   AGENT_ORG_PROTOTYPE      char terminated by ',' optionally enclosed by '"'
  )
