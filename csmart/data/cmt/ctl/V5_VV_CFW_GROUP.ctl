options (
  skip=1)
load
--  infile 'V5_CFW_GROUP.csv'
into table VV_CFW_GROUP
  insert
  fields
  (CFW_GROUP_ID            char terminated by ',' optionally enclosed by '"',
   DESCRIPTION             char terminated by ',' optionally enclosed by '"'
  )
