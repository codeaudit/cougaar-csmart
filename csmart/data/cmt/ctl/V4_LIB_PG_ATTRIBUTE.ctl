options (
  skip=1)
load
  infile 'V4_LIB_PG_ATTRIBUTE.csv'
into table V4_LIB_PG_ATTRIBUTE
  insert
  fields
  (PG_ATTRIBUTE_LIB_ID      char terminated by ',' optionally enclosed by '"',
   PG_NAME                  char terminated by ',' optionally enclosed by '"',
   ATTRIBUTE_NAME           char terminated by ',' optionally enclosed by '"',
   ATTRIBUTE_TYPE           char terminated by ',' optionally enclosed by '"',
   AGGREGATE_TYPE           char terminated by ',' optionally enclosed by '"'
  )
