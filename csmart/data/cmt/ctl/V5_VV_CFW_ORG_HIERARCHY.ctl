options (
  skip=1)
load
--  infile 'V5_CFW_ORG_HIERARCHY.csv'
into table VV_CFW_ORG_HIERARCHY
  insert
  fields
  (CFW_ID                  char terminated by ',' optionally enclosed by '"',
   ORG_ID                  char terminated by ',' optionally enclosed by '"',
   SUPERIOR_ORG_ID         char terminated by ',' optionally enclosed by '"'
  )
