options (
  skip=1)
load
  infile 'V5_CFW_GROUP_MEMBER.csv'
into table V5_CFW_GROUP_MEMBER
  insert
  fields
  (CFW_GROUP_ID            char terminated by ',' optionally enclosed by '"',
   CFW_ID                  char terminated by ',' optionally enclosed by '"'
  )
