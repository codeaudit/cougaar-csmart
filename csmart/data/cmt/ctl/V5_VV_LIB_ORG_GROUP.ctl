options (
  skip=1)
load
--  infile 'V5_LIB_ORG_GROUP.csv'
into table VV_LIB_ORG_GROUP
  insert
  fields
  (ORG_GROUP_ID            char terminated by ',' optionally enclosed by '"',
   DESCRIPTION             char terminated by ',' optionally enclosed by '"'
  )
