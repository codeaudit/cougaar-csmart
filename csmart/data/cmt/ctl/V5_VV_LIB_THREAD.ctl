options (
  skip=1)
load
--  infile 'V5_LIB_THREAD.csv'
into table VV_LIB_THREAD
  insert
  fields
  (THREAD_ID               char terminated by ',' optionally enclosed by '"',
   DESCRIPTION             char terminated by ',' optionally enclosed by '"'
  )
