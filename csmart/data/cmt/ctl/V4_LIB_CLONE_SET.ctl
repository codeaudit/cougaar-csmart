options (
  skip=1)
load
  infile 'V4_LIB_CLONE_SET.csv'
into table V4_LIB_CLONE_SET
  insert
  fields
  (CLONE_SET_ID            char terminated by ',' optionally enclosed by '"'
  )
