options (
  skip=1)
load
  infile 'V3_LIB_ACTIVITY_TYPE_REF.csv'
into table V3_LIB_ACTIVITY_TYPE_REF
  insert
  fields
  (ACTIVITY_TYPE        char terminated by ',' optionally enclosed by '"',
   DESCRIPTION          char terminated by ',' optionally enclosed by '"'
  )
