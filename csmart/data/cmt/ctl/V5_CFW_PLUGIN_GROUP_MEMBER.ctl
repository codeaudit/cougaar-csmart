options (
  skip=1)
load
  infile 'V5_CFW_PLUGIN_GROUP_MEMBER.csv'
into table V5_CFW_PLUGIN_GROUP_MEMBER
  insert
  fields
  (CFW_ID                  char terminated by ',' optionally enclosed by '"',
   PLUGIN_GROUP_ID         char terminated by ',' optionally enclosed by '"',
   PLUGIN_CLASS            char terminated by ',' optionally enclosed by '"',
   PLUGIN_CLASS_ORDER      char terminated by ',' optionally enclosed by '"'
  )
