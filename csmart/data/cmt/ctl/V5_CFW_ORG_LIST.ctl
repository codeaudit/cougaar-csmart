options (
  skip=1)
load
  infile 'V5_CFW_ORG_LIST.csv'
into table V5_CFW_ORG_LIST
  insert
  fields
  (CFW_ID                  char terminated by ',' optionally enclosed by '"',
   ORG_ID                  char terminated by ',' optionally enclosed by '"'
  )
