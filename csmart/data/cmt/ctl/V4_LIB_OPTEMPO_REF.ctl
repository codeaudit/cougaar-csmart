options (
  skip=1)
load
  infile 'V4_LIB_OPTEMPO_REF.csv'
into table V4_LIB_OPTEMPO_REF
  insert
  fields
  (OPTEMPO                 char terminated by ',' optionally enclosed by '"',
   DESCRIPTION             char terminated by ',' optionally enclosed by '"'
  )
