options (
  skip=1)
load
  infile 'V3_ASB_COMPONENT_NODE.csv'
into table V3_ASB_COMPONENT_NODE
  insert
  fields
  (ASSEMBLY_ID           char terminated by ',' optionally enclosed by '"',
   COMPONENT_ID          char terminated by ',' optionally enclosed by '"',
   NODE_ID               char terminated by ',' optionally enclosed by '"'
  )
