options (
  skip=1)
load
--  infile 'V5_CFW_OPLAN_ACTIVITY.csv'
into table VV_CFW_OPLAN_ACTIVITY
  insert
  fields
  (CFW_ID                  char terminated by ',' optionally enclosed by '"',
   OPLAN_ID                char terminated by ',' optionally enclosed by '"',
   ORG_GROUP_ID            char terminated by ',' optionally enclosed by '"',
   START_CDAY              char terminated by ',' optionally enclosed by '"',
   END_CDAY                char terminated by ',' optionally enclosed by '"',
   OPTEMPO                 char terminated by ',' optionally enclosed by '"',
   ACTIVITY_TYPE           char terminated by ',' optionally enclosed by '"'
  )
