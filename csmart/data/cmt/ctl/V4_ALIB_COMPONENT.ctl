options (
  skip=1)
load
  infile 'V4_ALIB_COMPONENT.csv'
into table V4_ALIB_COMPONENT
  insert
  fields
  (COMPONENT_ALIB_ID       char terminated by ',' optionally enclosed by '"',
   COMPONENT_NAME          char terminated by ',' optionally enclosed by '"',
   COMPONENT_LIB_ID        char terminated by ',' optionally enclosed by '"',
   COMPONENT_TYPE          char terminated by ',' optionally enclosed by '"',
   CLONE_SET_ID            char terminated by ',' optionally enclosed by '"'
  )
