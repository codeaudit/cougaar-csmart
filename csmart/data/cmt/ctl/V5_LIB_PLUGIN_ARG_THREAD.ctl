options (
  skip=1)
load
  infile 'V5_LIB_PLUGIN_ARG_THREAD.csv'
into table V5_LIB_PLUGIN_ARG_THREAD
  insert
  fields
  (PLUGIN_ARG_ID           char terminated by ',' optionally enclosed by '"',
   THREAD_ID               char terminated by ',' optionally enclosed by '"'
  )
