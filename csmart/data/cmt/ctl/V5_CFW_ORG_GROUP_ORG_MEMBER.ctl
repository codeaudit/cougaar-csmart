options (
  skip=1)
load
  infile 'V5_CFW_ORG_GROUP_ORG_MEMBER.csv'
into table V5_CFW_ORG_GROUP_ORG_MEMBER
  insert
  fields
  (CFW_ID                  char terminated by ',' optionally enclosed by '"',
   ORG_GROUP_ID            char terminated by ',' optionally enclosed by '"',
   ORG_ID                  char terminated by ',' optionally enclosed by '"'
  )
