options (
  skip=1)
load
  infile 'V5_LIB_PLUGIN_GROUP.csv'
into table V5_LIB_PLUGIN_GROUP
  insert
  fields
  (PLUGIN_GROUP_ID         char terminated by ',' optionally enclosed by '"',
   PLUGIN_GROUP_ORDER      char terminated by ',' optionally enclosed by '"',
   DESCRIPTION             char terminated by ',' optionally enclosed by '"'
  )
