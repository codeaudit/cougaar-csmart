options (
  skip=1)
load
  infile 'V3_EXPT_EXPERIMENT.csv'
into table V3_EXPT_EXPERIMENT
  insert
  fields
  (EXPT_ID           char terminated by ',' optionally enclosed by '"',
   DESCRIPTION       char terminated by ',' optionally enclosed by '"'
  )
