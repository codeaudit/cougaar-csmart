options (
  skip=1)
load
  infile 'V5_LIB_ROLE_REF.csv'
into table V5_LIB_ROLE_REF
  insert
  fields
  (ROLE                    char terminated by ',' optionally enclosed by '"',
   DESCRIPTION             char terminated by ',' optionally enclosed by '"'
  )
