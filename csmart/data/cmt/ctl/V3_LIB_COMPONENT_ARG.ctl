options (
  skip=1)
load
  infile 'V3_LIB_COMPONENT_ARG.csv'
into table V3_LIB_COMPONENT_ARG
  insert
  fields
  (COMPONENT_LIB_ID        char terminated by ',' optionally enclosed by '"',
   ARGUMENT                char terminated by ',' optionally enclosed by '"'
  )
