options (
  skip=1)
load
  infile 'V3_LIB_OPTEMPO_REF.csv'
into table V3_LIB_OPTEMPO_REF
  insert
  fields
  (OPTEMPO                 char terminated by ',' optionally enclosed by '"',
   DESCRIPTION             char terminated by ',' optionally enclosed by '"'
  )
