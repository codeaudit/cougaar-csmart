options (
  skip=1)
load
--  infile 'V5_CFW_ORG_ORGTYPE.csv'
into table VV_CFW_ORG_ORGTYPE
  insert
  fields
  (CFW_ID                  char terminated by ',' optionally enclosed by '"',
   ORG_ID                  char terminated by ',' optionally enclosed by '"',
   ORGTYPE_ID              char terminated by ',' optionally enclosed by '"'
  )
