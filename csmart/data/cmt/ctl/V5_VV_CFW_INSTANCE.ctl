options (
  skip=1)
load
--  infile 'V5_CFW_INSTANCE.csv'
into table VV_CFW_INSTANCE
  insert
  fields
  (CFW_ID                  char terminated by ',' optionally enclosed by '"',
   DESCRIPTION             char terminated by ',' optionally enclosed by '"'
  )
