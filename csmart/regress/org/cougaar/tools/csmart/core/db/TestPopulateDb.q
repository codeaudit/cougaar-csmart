database=${org.cougaar.configuration.database}
username=${org.cougaar.configuration.user}
password=${org.cougaar.configuration.password}

queryMaxExpt=\
 SELECT MAX(EXPT_ID) \
   FROM V4_EXPT_EXPERIMENT \
  WHERE EXPT_ID LIKE 'REGRESSION-____'

insertExpt=\
 INSERT INTO V4_EXPT_EXPERIMENT (EXPT_ID, DESCRIPTION) \
 VALUES (':expt_id', ':description')

queryMaxTrial=\
 SELECT MAX(TRIAL_ID) \
   FROM V4_EXPT_TRIAL \
  WHERE TRIAL_ID like 'REGRESSION-____'

insertTrial=\
 INSERT INTO V4_EXPT_TRIAL (TRIAL_ID, EXPT_ID, DESCRIPTION) \
 VALUES (':trial_id', ':expt_id', ':description')

queryMaxAssembly=\
 SELECT MAX(ASSEMBLY_ID) \
   FROM V4_ASB_ASSEMBLY \
  WHERE ASSEMBLY_TYPE = ':assembly_type'

insertAssembly=\
 INSERT INTO V4_ASB_ASSEMBLY (ASSEMBLY_ID, ASSEMBLY_TYPE, DESCRIPTION) \
 VALUES (':assembly_id', ':assembly_type', ':description')

insertTrialAssembly=\
 INSERT INTO V4_EXPT_TRIAL_ASSEMBLY (EXPT_ID, TRIAL_ID, ASSEMBLY_ID, DESCRIPTION) \
 VALUES (':expt_id', ':trial_id', ':assembly_id', ':description')

insertLibComponent=\
 INSERT INTO V4_LIB_COMPONENT \
   (COMPONENT_LIB_ID, COMPONENT_TYPE, COMPONENT_CLASS, INSERTION_POINT, DESCRIPTION) \
 VALUES \
   (':component_lib_id', ':component_type', ':component_class', ':insertion_point', ':description')

queryLibComponent=\
 SELECT COMPONENT_LIB_ID, COMPONENT_TYPE, COMPONENT_CLASS, INSERTION_POINT, DESCRIPTION \
   FROM V4_LIB_COMPONENT \
  WHERE COMPONENT_LIB_ID = ':component_lib_id'

deleteLibComponent=\
 DELETE FROM V4_LIB_COMPONENT \
  WHERE COMPONENT_LIB_ID = ':component_lib_id'

deleteAlibComponent=\
 DELETE FROM V4_ALIB_COMPONENT \
  WHERE COMPONENT_LIB_ID = ':component_lib_id'

queryTableWithTrialId=\
 SELECT TABLE_NAME FROM USER_TAB_COLUMNS \
  WHERE COLUMN_NAME = 'TRIAL_ID' \
    AND TABLE_NAME LIKE 'V4_%' \
    AND TABLE_NAME <> 'V4_EXPT_TRIAL'

queryTableWithAssemblyId=\
 SELECT TABLE_NAME FROM USER_TAB_COLUMNS \
  WHERE COLUMN_NAME = 'ASSEMBLY_ID' \
    AND TABLE_NAME LIKE 'V4_%' \
    AND TABLE_NAME <> 'V4_ASB_ASSEMBLY'

queryTableWithExptId=\
 SELECT TABLE_NAME FROM USER_TAB_COLUMNS \
  WHERE COLUMN_NAME = 'EXPT_ID' \
    AND TABLE_NAME LIKE 'V4_%' \
    AND TABLE_NAME <> 'V4_EXPT_EXPERIMENT'

deleteFromTableWithAssemblyId=\
 DELETE FROM :table \
  WHERE ASSEMBLY_ID = ':assembly_id'

deleteFromTableWithTrialId=\
 DELETE FROM :table \
  WHERE TRIAL_ID = ':trial_id'

deleteFromTableWithExptId=\
 DELETE FROM :table \
  WHERE EXPT_ID = ':expt_id'

queryAllTableColumns=\
 SELECT TABLE_NAME, COLUMN_NAME FROM USER_TAB_COLUMNS \
  WHERE TABLE_NAME LIKE 'V4_%' \
  ORDER BY TABLE_NAME

deleteFromTableInitial=DELETE FROM :table WHERE :column like '%:clean_type%'
deleteFromTableMore=\ OR :column like ':clean_type%'

deleteAssembly=\
 DELETE FROM V4_EXPT_TRIAL_ASSEMBLY \
  WHERE ASSEMBLY_ID = ':assembly_id'

deleteTrial=\
 DELETE FROM V4_EXPT_TRIAL \
  WHERE TRIAL_ID = ':trial_id'

deleteExpt=\
  DELETE FROM V4_EXPT_EXPERIMENT \
   WHERE EXPT_ID = ':expt_id'

deleteAllFromTableWithAssemblyId=\
 DELETE FROM :table \
  WHERE ASSEMBLY_ID like ':clean_type_%'

deleteAllFromTableWithTrialId=\
 DELETE FROM :table \
  WHERE TRIAL_ID like ':clean_type_%'

deleteAllFromTableWithExptId=\
 DELETE FROM :table \
  WHERE EXPT_ID like ':clean_type_%'

deleteAllAssembly=\
 DELETE FROM V4_EXPT_TRIAL_ASSEMBLY \
  WHERE ASSEMBLY_ID like ':clean_type_%'

deleteAllTrial=\
 DELETE FROM V4_EXPT_TRIAL \
  WHERE TRIAL_ID like ':clean_type_%'

deleteAllExpt=\
  DELETE FROM V4_EXPT_EXPERIMENT \
   WHERE EXPT_ID like ':clean_type_%'
