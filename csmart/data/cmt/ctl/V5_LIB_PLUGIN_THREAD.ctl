options (
  skip=1)
load
  infile 'V5_LIB_PLUGIN_THREAD.csv'
into table V5_LIB_PLUGIN_THREAD
  insert
  fields
  (PLUGIN_CLASS            char terminated by ',' optionally enclosed by '"',
   THREAD_ID               char terminated by ',' optionally enclosed by '"',
   DESCRIPTION             char terminated by ',' optionally enclosed by '"'
  )
