options (
  skip=1)
load
  infile 'V5_CFW_CONTEXT_PLUGIN_ARG.csv'
into table V5_CFW_CONTEXT_PLUGIN_ARG
  insert
  fields
  (CFW_ID                  char terminated by ',' optionally enclosed by '"',
   ORG_CONTEXT             char terminated by ',' optionally enclosed by '"',
   PLUGIN_ARG_ID           char terminated by ',' optionally enclosed by '"'
  )
