options (
  skip=1)
load
  infile 'V5_LIB_ORGTYPE_REF.csv'
into table V5_LIB_ORGTYPE_REF
  insert
  fields
  (ORGTYPE_ID              char terminated by ',' optionally enclosed by '"',
   DESCRIPTION             char terminated by ',' optionally enclosed by '"'
  )
