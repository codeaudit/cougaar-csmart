options (
  skip=1)
load
--  infile 'V5_CFW_OPLAN_LOC.csv'
into table VV_CFW_OPLAN_LOC
  insert
  fields
  (CFW_ID                  char terminated by ',' optionally enclosed by '"',
   OPLAN_ID                char terminated by ',' optionally enclosed by '"',
   ORG_GROUP_ID            char terminated by ',' optionally enclosed by '"',
   START_CDAY              char terminated by ',' optionally enclosed by '"',
   END_CDAY                char terminated by ',' optionally enclosed by '"',
   LOCATION_CODE           char terminated by ',' optionally enclosed by '"'
  )
