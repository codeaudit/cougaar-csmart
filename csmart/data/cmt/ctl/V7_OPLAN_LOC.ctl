options (
  skip=1)
load
--  infile 'V7_OPLAN_LOC.csv'
into table V7_OPLAN_LOC
  insert
  fields
  (ORG_GROUP_ID            char terminated by ',' optionally enclosed by '"',
   LOCATION_CODE           char terminated by ',' optionally enclosed by '"',
   START_CDAY              char terminated by ',' optionally enclosed by '"',
   END_CDAY                char terminated by ',' optionally enclosed by '"'
  )
