options (
  skip=1)
load
  infile 'V4_LIB_AGENT_ORG.csv'
into table V4_LIB_AGENT_ORG
  insert
  fields
  (COMPONENT_LIB_ID         char terminated by ',' optionally enclosed by '"',
   AGENT_LIB_NAME           char terminated by ',' optionally enclosed by '"',
   AGENT_ORG_CLASS          char terminated by ',' optionally enclosed by '"'
  )
