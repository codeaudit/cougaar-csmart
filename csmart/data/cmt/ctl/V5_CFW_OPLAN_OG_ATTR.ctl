options (
  skip=1)
load
  infile 'V5_CFW_OPLAN_OG_ATTR.csv'
into table V5_CFW_OPLAN_OG_ATTR
  insert
  fields
  (CFW_ID                  char terminated by ',' optionally enclosed by '"',
   OPLAN_ID                char terminated by ',' optionally enclosed by '"',
   ORG_GROUP_ID            char terminated by ',' optionally enclosed by '"',
   START_CDAY              char terminated by ',' optionally enclosed by '"',
   ATTRIBUTE_NAME          char terminated by ',' optionally enclosed by '"',
   END_CDAY                char terminated by ',' optionally enclosed by '"',
   ATTRIBUTE_VALUE         char terminated by ',' optionally enclosed by '"'
  )
