options (
  skip=1)
load
--  infile 'V5_CFW_ALPLOC.csv'
into table VV_CFW_ALPLOC
  insert
  fields
  (CFW_ID                  char terminated by ',' optionally enclosed by '"',
   ALPLOC_CODE             char terminated by ',' optionally enclosed by '"',
   LOCATION_NAME           char terminated by ',' optionally enclosed by '"',
   LATITUDE                char terminated by ',' optionally enclosed by '"',
   LONGITUDE               char terminated by ',' optionally enclosed by '"',
   INSTALLATION_TYPE_CODE  char terminated by ',' optionally enclosed by '"'
  )
