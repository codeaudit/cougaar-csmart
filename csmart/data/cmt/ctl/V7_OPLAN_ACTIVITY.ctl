options (
  skip=1)
load
--  infile 'V7_OPLAN_ACTIVITY.csv'
into table V7_OPLAN_ACTIVITY
  insert
  fields
  (ORG_GROUP_ID            char terminated by ',' optionally enclosed by '"',
   ACTIVITY_TYPE           char terminated by ',' optionally enclosed by '"',
   OPTEMPO                 char terminated by ',' optionally enclosed by '"',
   START_CDAY              char terminated by ',' optionally enclosed by '"',
   END_CDAY                char terminated by ',' optionally enclosed by '"'
  )
