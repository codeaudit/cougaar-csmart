options (
  skip=1)
load
  infile 'V3_ASB_NODE.csv'
into table V3_ASB_NODE
  insert
  fields
  (ASSEMBLY_ID                   char terminated by ',' optionally enclosed by '"',
   NODE_ID                       char terminated by ',' optionally enclosed by '"',
   NODE_NAME                     char terminated by ',' optionally enclosed by '"',
   DESCRIPTION                   char terminated by ',' optionally enclosed by '"'
  )
