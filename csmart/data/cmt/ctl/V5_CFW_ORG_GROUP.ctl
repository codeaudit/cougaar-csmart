options (
  skip=1)
load
  infile 'V5_CFW_ORG_GROUP.csv'
into table V5_CFW_ORG_GROUP
  insert
  fields
  (CFW_ID                  char terminated by ',' optionally enclosed by '"',
   ORG_GROUP_ID            char terminated by ',' optionally enclosed by '"',
   DESCRIPTION             char terminated by ',' optionally enclosed by '"'
  )
