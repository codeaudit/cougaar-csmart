options (
  skip=1)
load
  infile 'V5_LIB_ROLE_THREAD.csv'
into table V5_LIB_ROLE_THREAD
  insert
  fields
  (ROLE                    char terminated by ',' optionally enclosed by '"',
   THREAD_ID               char terminated by ',' optionally enclosed by '"',
   DESCRIPTION             char terminated by ',' optionally enclosed by '"'
  )
