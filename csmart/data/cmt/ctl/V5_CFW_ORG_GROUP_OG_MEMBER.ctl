options (
  skip=1)
load
  infile 'V5_CFW_ORG_GROUP_OG_MEMBER.csv'
into table V5_CFW_ORG_GROUP_OG_MEMBER
  insert
  fields
  (CFW_ID                  char terminated by ',' optionally enclosed by '"',
   ORG_GROUP_ID            char terminated by ',' optionally enclosed by '"',
   MEMBER_ORG_GROUP_ID     char terminated by ',' optionally enclosed by '"'
  )
