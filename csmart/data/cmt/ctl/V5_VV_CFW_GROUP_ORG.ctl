options (
  skip=1)
load
--  infile 'V5_CFW_GROUP_ORG.csv'
into table VV_CFW_GROUP_ORG
  insert
  fields
  (CFW_GROUP_ID            char terminated by ',' optionally enclosed by '"',
   ORG_ID                  char terminated by ',' optionally enclosed by '"'
  )
