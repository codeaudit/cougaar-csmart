options (
  skip=1)
load
--  infile 'V7_TPFDD.csv'
into table V7_TPFDD
  insert
  fields
  (RLN                     char terminated by ',' optionally enclosed by '"',
   SERVICE_CODE            char terminated by ',' optionally enclosed by '"',
   UIC                     char terminated by ',' optionally enclosed by '"',
   ULC                     char terminated by ',' optionally enclosed by '"',
   UTC                     char terminated by ',' optionally enclosed by '"',
   UNIT_NAME               char terminated by ',' optionally enclosed by '"',
   DESCRIPTION             char terminated by ',' optionally enclosed by '"',
   NUM_PAX                 char terminated by ',' optionally enclosed by '"',
   ORIGIN_GEOLOC           char terminated by ',' optionally enclosed by '"',
   ORIGIN_NAME             char terminated by ',' optionally enclosed by '"',
   POE_GEOLOC              char terminated by ',' optionally enclosed by '"',
   POE_NAME                char terminated by ',' optionally enclosed by '"',
   POD_GEOLOC              char terminated by ',' optionally enclosed by '"',
   POD_NAME                char terminated by ',' optionally enclosed by '"',
   DEST_GEOLOC             char terminated by ',' optionally enclosed by '"',
   DEST_NAME               char terminated by ',' optionally enclosed by '"',
   RLD_ORIGIN              char terminated by ',' optionally enclosed by '"',
   ALD_POE                 char terminated by ',' optionally enclosed by '"',
   EAD_POD                 char terminated by ',' optionally enclosed by '"',
   LAD_POD                 char terminated by ',' optionally enclosed by '"',
   RDD_DEST                char terminated by ',' optionally enclosed by '"'
  )
