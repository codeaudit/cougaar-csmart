options (
  skip=1)
load
--  infile 'V5_LIB_ORGANIZATION.csv'
into table VV_LIB_ORGANIZATION
  insert
  fields
  (ORG_ID                  char terminated by ',' optionally enclosed by '"',
   ORG_NAME                char terminated by ',' optionally enclosed by '"',
   UIC                     char terminated by ',' optionally enclosed by '"'
  )
