options (
  skip=1)
load
  infile 'V5_CFW_ORG_OG_RELATION.csv'
into table V5_CFW_ORG_OG_RELATION
  insert
  fields
  (CFW_ID                  char terminated by ',' optionally enclosed by '"',
   ROLE                    char terminated by ',' optionally enclosed by '"',
   ORG_ID                  char terminated by ',' optionally enclosed by '"',
   ORG_GROUP_ID            char terminated by ',' optionally enclosed by '"',
   START_DATE              char terminated by ',' optionally enclosed by '"',
   END_DATE                char terminated by ',' optionally enclosed by '"'
  )
