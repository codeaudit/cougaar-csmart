options (
  skip=1)
load
  infile 'V4_LIB_COMPONENT.csv'
into table V4_LIB_COMPONENT
  insert
  fields
  (COMPONENT_LIB_ID        char terminated by ',' optionally enclosed by '"',
   COMPONENT_TYPE          char terminated by ',' optionally enclosed by '"',
   COMPONENT_CLASS         char terminated by ',' optionally enclosed by '"',
   INSERTION_POINT         char terminated by ',' optionally enclosed by '"',
   DESCRIPTION             char terminated by ',' optionally enclosed by '"'
  )
