options (
  skip=1)
load
--  infile 'V5_LIB_PLUGIN_REF.csv'
into table VV_LIB_PLUGIN_REF
  insert
  fields
  (PLUGIN_CLASS            char terminated by ',' optionally enclosed by '"',
   DESCRIPTION             char terminated by ',' optionally enclosed by '"'
  )
