options (
  skip=1)
load
  infile 'V3_LIB_OPLAN_AGENT_ATTR_REF.csv'
into table V3_LIB_OPLAN_AGENT_ATTR_REF
  insert
  fields
  (ATTRIBUTE_NAME          char terminated by ',' optionally enclosed by '"',
   DESCRIPTION             char terminated by ',' optionally enclosed by '"'
  )
