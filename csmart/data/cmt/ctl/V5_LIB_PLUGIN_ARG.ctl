options (
  skip=1)
load
  infile 'V5_LIB_PLUGIN_ARG.csv'
into table V5_LIB_PLUGIN_ARG
  insert
  fields
  (PLUGIN_ARG_ID           char terminated by ',' optionally enclosed by '"',
   PLUGIN_CLASS            char terminated by ',' optionally enclosed by '"',
   ARGUMENT                char terminated by ',' optionally enclosed by '"',
   ARGUMENT_ORDER          char terminated by ',' optionally enclosed by '"',
   ARGUMENT_TYPE           char terminated by ',' optionally enclosed by '"'
  )
