options (
  skip=1)
load
  infile 'V5_CFW_ORGTYPE_PLUGIN_GRP.csv'
into table V5_CFW_ORGTYPE_PLUGIN_GRP
  insert
  fields
  (CFW_ID                  char terminated by ',' optionally enclosed by '"',
   ORGTYPE_ID              char terminated by ',' optionally enclosed by '"',
   PLUGIN_GROUP_ID         char terminated by ',' optionally enclosed by '"'
  )
