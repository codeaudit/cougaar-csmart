options (
  skip=1)
load
  infile 'V3_EXPT_TRIAL.csv'
into table V3_EXPT_TRIAL
  insert
  fields
  (EXPT_ID           char terminated by ',' optionally enclosed by '"',
   TRIAL_ID          char terminated by ',' optionally enclosed by '"',
   DESCRIPTION       char terminated by ',' optionally enclosed by '"'
  )
