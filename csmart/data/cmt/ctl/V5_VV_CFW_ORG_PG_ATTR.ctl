options (
  skip=1)
load
--  infile 'V5_CFW_ORG_PG_ATTR.csv'
into table VV_CFW_ORG_PG_ATTR
  insert
  fields
  (CFW_ID                  char terminated by ',' optionally enclosed by '"',
   ORG_ID                  char terminated by ',' optionally enclosed by '"',
   PG_ATTRIBUTE_LIB_ID     char terminated by ',' optionally enclosed by '"',
   ATTRIBUTE_VALUE         char terminated by ',' optionally enclosed by '"',
   ATTRIBUTE_ORDER         char terminated by ',' optionally enclosed by '"',
   START_DATE              char terminated by ',' optionally enclosed by '"',
   END_DATE                char terminated by ',' optionally enclosed by '"'
  )
