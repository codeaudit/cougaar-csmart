options (
  skip=1)
load
  infile 'V3_ASB_ALPLOC.csv'
into table V3_ASB_ALPLOC
  insert
  fields
  (ASSEMBLY_ID             char terminated by ',' optionally enclosed by '"',
   ALPLOC_CODE             char terminated by ',' optionally enclosed by '"',
   LOCATION_NAME           char terminated by ',' optionally enclosed by '"',
   LATITUDE                char terminated by ',' optionally enclosed by '"',
   LONGITUDE               char terminated by ',' optionally enclosed by '"',
   INSTALLATION_TYPE_CODE  char terminated by ',' optionally enclosed by '"'
  )
