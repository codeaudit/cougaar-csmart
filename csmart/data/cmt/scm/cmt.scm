

(import "java.sql.DriverManager")
(load "elf/jdbc.scm")
(load "elf/basic.scm")
(load "build/compile.scm")
(import "java.util.Date")
(import "org.cougaar.tools.csmart.ui.console.CMT")


;; for changing between the CFW_ORG_GROUP table and the LIB_ORG_GROUP
;;(set! cog-type "CFW_")
;;(set! cfw-prefix "V5_")
(set! cfw_group_id "SMALL-3ID-CFW-GRP")
(set! cog-type "LIB_")
;;(set! cfw-prefix "V6_")
(set! cfw-prefix "V7_")
;;(set! cfw_group_id "SMALL-3ID-TRANS-CFW-GRP")
;;(set! cfw_group_id "TINY-1AD-TRANS-STUB-CFW-GRP")
;;(set! cfw_group_id "TINY-1AD-TRANS-CFW-GRP")
;;(set! cfw_group_id "SMALL-1AD-TRANS-CFW-GRP")
(set! cfw_group_id "1AD-TRANS-CFW-GRP")

(set! asb-prefix "V4_")
(set! refDBConnection #null)

(set! all-threads '(STRATEGIC-TRANS THEATER-TRANS CLASS-1 CLASS-3 CLASS-4 CLASS-5 CLASS-8 CLASS-9))
(set! 135threads '(STRATEGIC-TRANS THEATER-TRANS CLASS-1 CLASS-3 CLASS-5))
(set! 1thread '(STRATEGIC-TRANS THEATER-TRANS CLASS-1))
(set! 3thread '(STRATEGIC-TRANS THEATER-TRANS CLASS-3))
(set! 5thread  '(STRATEGIC-TRANS THEATER-TRANS CLASS-5))

 ;; temporary hack for testing
;;(set! cfw_group_id "3ID-CFW-GRP-A")

(set! assembly_description "test assembly")
(set! threads all-threads)
;;(set! threads 3thread)
(set! version "")
(set! clones ())
;;(set! aid (create-cmt-asb assembly_description cfw_group_id threads version clones))
;;(createCSMARTExperiment expt_id cfw_group_id aid)

(define (createBaseExperiment  expt_name cfw_group_id)
  (CMT.setTraceQueries #t)
  (CMT.createBaseExperiment expt_name cfw_group_id)
  (CMT.setTraceQueries #f)
  )


;; for BBN Eiger access
(define (use-eiger user password)
  (set! refDBDriver   "oracle.jdbc.driver.OracleDriver")
  (set! refDBConnURL  "jdbc:oracle:thin:@eiger.alpine.bbn.com:1521:alp")
  (set! refDBUser     user)
  (set! refDBPasswd   password)
  (initialize)
  )

(define (use-database database user password)
  (set! refDBDriver   "oracle.jdbc.driver.OracleDriver")
  (set! refDBConnURL  database)
  (set! refDBUser     user)
  (set! refDBPasswd   password)
  (initialize)
  )

(define (use-mysql database user password)
  (set! refDBDriver   "org.gjt.mm.mysql.Driver")
  (set! refDBConnURL  database)
  (set! refDBUser     user)
  (set! refDBPasswd   password)
  (initialize)
  )

(define (initialize)
  ;; Register the Oracle driver. 
  ;;(DriverManager.registerDriver (OracleDriver.))
  (Class.forName refDBDriver)
  ;; Open a connection to the reference database
  (if (eq? refDBConnection #null)
      (set! refDBConnection (DriverManager.getConnection refDBConnURL refDBUser refDBPasswd))
  #t))

(define (setDBConnection conn)
  (println (list 'setDBConnection conn))
  (set! refDBConnection conn)
)

(define (createStatement conn)
  (tryCatch
   (.createStatement conn)
   (lambda(excep)
     (let ((b (.getBaseException excep)))
       (if (instanceof b java.sql.SQLException.class)
	   ;; If you get an SQL exception, wait 15 seconds and try again.
	   (begin
	     (display (string-append  "Exception " b " occurred\n"))
	     (display "Reconnecting to database! ...")
	     (Thread.sleep 15000L)
	     (initialize)
	     (createStatement conn)
	     (display "\n"))
	   (throw excep))))))

(define (getrefDBConnection)
  refDBConnection)

(define (result-set-jdbc query)
  (let* ((stmt (createStatement (getrefDBConnection)))
	 (rs (.executeQuery stmt query)))
    (cond
     ((not (eq? rs #null))
      rs)
     (else
      (.close stmt)
      #null))))

(define (println msg)
  (.println System.out$ msg)
  #t)

(define (with-query-jdbc query ex)
  (let((q query))
    ;;(println (string-append "(with-query-jdbc " query))
    (let* ((stmt (createStatement (getrefDBConnection)))
	   (rs (.executeQuery stmt query))
	   (answer (ex rs)))
      (cond
       ((not (eq? rs #null))
	(.close stmt)
	(.close rs))
       (else
	(.close stmt)
	;;(println "Closing JDBC result")
	))
      answer)))

(define (query-1-result query how name)
  (with-query-jdbc
   query
   (lambda (result)
     (if (.next result) (how result name)
	 ""))))

(define (query-1-int query name)
  (let
      ((res
	(query-1-result query .getInt name)))
    (if
     (.equals "" res)
     #null
     res)))

(define (query-1-string query name)
  (query-1-result query .getString name))


;;;
;;; view query
;;;
(import "javax.swing.JTable")
(import "javax.swing.JFrame")
(import "javax.swing.JScrollPane")
(import "java.util.Vector")
(import "java.awt.BorderLayout")

(define (dbinit args)
  ;; Main program for dbinit script.
  (if (or (eq? args #null)
	  (< (vector-length args) 1)
	  (not (dbinit-action (argify args 0))))
      (dbinit-usage))
  (System.exit 0))


(define (maybe-load v)
  (let ((file (File. v)))
    (if (.exists file)
	(begin (load file)
	       #t)
	(begin
	  (display `(file ,v does not exist!))
	  (newline)
	  #f))))  

(define (vectorize data)
  (let ((v (Vector.)))
    (iterate data (lambda (x) (.add v x)))
    v))


(define (frame name contents)
  (let ((f (JFrame. name)))
    (.add (.getContentPane f) contents)
    (.pack f)
    (.show f)
    f))

(define (view-query con query)
  (let* ((result (collect-query con query))
	 (columns (vectorize (car result)))
	 (data (vectorize (map vectorize (cdr result))))
	 (table (JTable. data columns)))
    (frame query (JScrollPane. table))))


(register-driver 'oracle)

;;; This should come from and InstantDB1.properties
;;; Oracle1.properties, but i don't know how to read them.
(define (db-connection) (DriverManager.getConnection -url -user -password))

(define (string+ ss)
  ;; Given a nested list, ss, return a string suitable for sending to
  ;; a JDBC query.
  (define (string+0 ss)
    (if (null? ss) ""
	(string+1 (.toString (car ss)) (cdr ss))))

  (define (string+1 head tail)
    (define (space? left right)
      (or (.endsWith left ",")
	  (not (or (.endsWith left "(")
		   (.startsWith right ")")
		   (.startsWith right "(")
		   (.startsWith right ",")))))
    (if (null? tail) head
	(let ((h (.toString (car tail)))
	      (tail (cdr tail)))
	  (if (space? head h)
	      (string+1 (string-append head " " h) tail)
	      (string+1 (string-append head h) tail)))))
  (string+0 (flatten ss)))


(define [] vector-ref)
(define ([0] x) ([] x 0))

(define (db-update con query)
  ;; Do a insert, update, returns number of rows affected.
  ;; Do create table, ... returns 0.
  (let* ((stmt (.createStatement con))
	 (result (.executeUpdate stmt query)))
    (.close stmt)
    result))
	 
;;; Convention: database procedures that end in + do string+.
;;; Without + they just take a string.
(define (db-update+ con query) (db-update con (string+ query)))

(define (collect-query+ con . args) (collect-query con (string+ args)))

;;; Oracle can represent dates, but Oracle SQL doesn't have an
;;; external representation for dates.  A date must be converted to
;;; and from a string in an application specific format.

;;; We do it this way because this application only care about the
;;; day.  Oracle doen't seem to care about more than 4 digit years, yet.

;;; Convert an object into a string that can be passed to an insert
;;; statement.
(define (toSql x) (if (eq? x #null) "null" (toSql0 x)))
(define-method (toSql0 (x Object)) (.toString x))
(define-method (toSql0 (s String)) (sqlQuote s))
(define-method (toSql0 (s Symbol)) (if (eqv? s #null) "null" (.toString s)))
      
(define-method (toSql0 (date java.util.Date))
  (string-append "to_date('"
		 (+ 1 (.getMonth date)) "/"
		 (.getDate date) "/"
		 (+ 1900 (.getYear date)) "','MM/DD/YYYY')"))

(define (sqlQuote s)
  (define (quoteDouble s)
    (cond
     ((null? s) '())
     (else
      (if (eqv? (car s) #\') (cons #\' (cons #\' (quoteDouble (cdr s))))
	  (cons (car s) (quoteDouble (cdr s)))))))
  (let
      ((ss (if (null? s) s (.toString s))))
    (string-append
     "'" (list->string (quoteDouble (string->list ss))) "'"))
  )
;;;
;;; table := `(,name ,fields)
;;;
(define table-name car)
(define table-fields cadr)

(define (table-drop table) `(drop table ,(table-name table)))

(define (table-field-toString f)
  (define (table-field-toString0 x fs)
    (if (space? fs) (string-append x " " (table-field-toString fs))
	(string-append x (table-field-toString fs))))
  (define (space? fs) (not (or (null? fs) (pair? (car fs)))))
  (if (null? f) ""
      (table-field-toString0 (car f) (cdr f))))

(define (table-create table)
  (let ((n (table-name table))
	(fs (table-fields table)))
    `(create table ,n "(" ,@(separate "," (map table-field-toString fs)) ")")))

(define (table-insert table-name values)
  `(insert into ,table-name values "("
	   ,@(separate "," (map toSql values)) ")"))

(define (sqlList values)
  (string+ `("("
	     ,@(separate "," (map sqlQuote values)) ")"
	     )
	   )
  )

(define (db-initialize db-type)
  (display (string+ `(initializing ,db-type "...")))
  (load "scm/db-schema.scm")
  (load "scm/db-data.scm")
  (let ((con (db-connection)))
    (for-each (lambda (t)
		(tryCatch
		 (db-update+ con (table-drop t))
		 (lambda (e) (display (string+ `(ignoring ,e)))))
		(db-update+ con (table-create t)))
	      *db-tables*)
    (for-each (lambda (d) (db-update+ con (table-insert (car d) (cdr d))))
	      *db-data*)
    (.close con))
  (display "done\n"))

(define (file-read file)
  (call-with-input-file file (lambda (s) (read s))))

(define (argify args i)
  ;; Example:
  ;; > (argify #("-db" "instantdb" "-dump") 0)
  ;; (-db "instantdb" -dump)
  (if (< i (vector-length args))
      (let ((arg (vector-ref args i)))
	(cons 
	 (if (.startsWith arg "-") (string->symbol arg)
	     arg)
	 (argify args (+ i 1))))
      ()))

(define (arg-keyword? x)
  (and (symbol? x) (.startsWith (symbol->string x) "-")))

(define (find-arg args name default)
  (define (arg-value items)
    (cond ((null? (cdr items)) #t)
	  ((arg-keyword? (cadr items)) #t)
	  (else (cadr items))))
  (let ((items (member name args)))
    (if items (arg-value items) default)))

(define (org_components assembly_id cfw_group_id)
  
  (string-append 
   "select " (sqlQuote  assembly_id) ","
   (sqlQuote  assembly_id)
   "|| org_name ,null,org_name, org.org_id,'AGENT',0 from " cfw-prefix "cfw_group_org go, " cfw-prefix "lib_organization org  where cfw_group_id=" 
   (sqlQuote  cfw_group_id)
   "               and org.org_id=go.org_id"
   )
  )


  

(define (view_org_components assembly_id cfw_group_id)
  (view-query  
   (getrefDBConnection)
   (get-asb-org-data-sql assembly_id cfw_group_id)
   ))


;; ASSEMBLY_ID				  VARCHAR2(50)
;; COMPONENT_ID			 NOT NULL VARCHAR2(150)
;; COMPONENT_NAME 			  VARCHAR2(100)
;; PARENT_COMPONENT_ID			  VARCHAR2(100)
;; COMPONENT_LIB_ID			  VARCHAR2(100)
;; COMPONENT_CATEGORY			  VARCHAR2(50)
;; INSERTION_ORDER			  VARCHAR2(50)











;; must be run before inserting hierarchy to fix foreign key constraints

(define (clear-all-cmt-assemblies)
  (dbu (string-append "delete from " asb-prefix "asb_component_hierarchy"))
  (dbu (string-append "delete from " asb-prefix "asb_agent_pg_attr"))
  (dbu (string-append "delete from " asb-prefix "asb_agent_relation"))
  (dbu (string-append "delete from " asb-prefix "asb_component_arg"))
  (dbu (string-append "delete from " asb-prefix "alib_component"))
  (dbu (string-append "delete from " asb-prefix "asb_assembly"))
  )

(define (clear-cmt-assembly cfw_g_id threads version)
  (set! assembly_id (get-assembly-id cfw_g_id threads version ()))
  (clear-cmt-asb  assembly_id))

(define (clear-cmt-asb  assembly_id)
  (let
      ((unused-asbs
	(query-set
	 (string-append
	  "select assembly_id from "
	  asb-prefix "asb_assembly aa"
	  ;;"   where aa.assembly_type='CMT'"
	  "   where aa.assembly_id not in"
	  "   (select assembly_id from "
	  asb-prefix "expt_trial_assembly)"
	  )
	 "assembly_id")))
    (cond
     ((.contains unused-asbs assembly_id)
      (clearCMTasb assembly_id)
      )))
  )

(define (clearCMTasb assembly_id)
  (let
      ((x assembly_id))
    ;; don't start deletion if the assembly is in use by an experiment
    (print (list (string-append "delete from " asb-prefix "asb_component_hierarchy where assembly_id = " (sqlQuote assembly_id))
		 (dbu (string-append "delete from " asb-prefix "asb_component_hierarchy where assembly_id = " (sqlQuote assembly_id)))))
    (print (list (string-append "delete from " asb-prefix "asb_agent where assembly_id = " (sqlQuote assembly_id))
		 (dbu (string-append "delete from " asb-prefix "asb_agent where assembly_id = " (sqlQuote assembly_id)))))
    (print (list (string-append "delete from " asb-prefix "asb_agent_pg_attr where assembly_id = " (sqlQuote assembly_id))
		 (dbu (string-append "delete from " asb-prefix "asb_agent_pg_attr where assembly_id = " (sqlQuote assembly_id)))))
    (print (list (string-append "delete from " asb-prefix "asb_agent_relation where assembly_id = " (sqlQuote assembly_id))
		 (dbu (string-append "delete from " asb-prefix "asb_agent_relation where assembly_id = " (sqlQuote assembly_id)))))
    (print (list (string-append "delete from " asb-prefix "asb_component_arg where assembly_id = " (sqlQuote assembly_id))
		 (dbu (string-append "delete from " asb-prefix "asb_component_arg where assembly_id = " (sqlQuote assembly_id)))))
    (print (list (string-append "delete from " asb-prefix "asb_oplan_agent_attr where assembly_id = " (sqlQuote assembly_id))
		 (dbu (string-append "delete from " asb-prefix "asb_oplan_agent_attr where assembly_id = " (sqlQuote assembly_id)))))
    (print (list (string-append "delete from " asb-prefix "asb_oplan where assembly_id = " (sqlQuote assembly_id))
		 (dbu (string-append "delete from " asb-prefix "asb_oplan where assembly_id = " (sqlQuote assembly_id)))))
    (print (list (string-append "delete from " asb-prefix "asb_assembly where assembly_id = " (sqlQuote assembly_id))
		 (dbu (string-append "delete from " asb-prefix "asb_assembly where assembly_id = " (sqlQuote assembly_id)))))
    ))

(define (clear-unused-cmt-assemblies)
  (map*
   clear-cmt-asb
   (query-set
    (string-append
     "select assembly_id from "
     asb-prefix "asb_assembly aa"
;;     "   where aa.assembly_type='CMT'"
     "   where aa.assembly_id not in"
     "   (select assembly_id from "
     asb-prefix "expt_trial_assembly)"
     )
    "assembly_id")
   ))

(define (use-threads tlist)
  (let
      ((tl (cons "BASE" tlist)))
    (sqlList tl)
    )
  )

(define (tabbrev thread)
  (cond
   ((eq? thread 'BASE) 'B)
   ((eq? thread 'STRATEGIC-TRANS) 'S)
   ((eq? thread 'THEATER-TRANS)'T)
   ((eq? thread 'CLASS-1)1 )
   ((eq? thread 'CLASS-3)3 )
   ((eq? thread 'CLASS-4)4 )
   ((eq? thread 'CLASS-5)5 )
   ((eq? thread 'CLASS-8)8 )
   ((eq? thread 'CLASS-9)9)


   ((.equals "STRATEGIC-TRANS" thread) 'S)
   ((.equals  "THEATER-TRANS" thread)'T)
   ((.equals "CLASS-1"  thread)1 )
   ((.equals "CLASS-3"  thread)3 )
   ((.equals "CLASS-4"  thread)4 )
   ((.equals "CLASS-5"  thread)5 )
   ((.equals "CLASS-8"  thread)8 )
   ((.equals "CLASS-9"  thread)9)))



(define (get-assembly-id cfw_g_id threads version clones)
  (set! assembly_id (string-append "CMT-" cfw_g_id
				   version
				   "{" (apply string-append (map* tabbrev threads)) "}"
				   (apply string-append (map* (lambda(clone) (second clone))clones )))))


(define (create-cmt-asb assembly_description cfw_g_id threads version clones)
  (clear-unused-cmt-assemblies)
  ;; intentionally setting a global variable for debugging purposes
  ;;  (print "clear-cmt-assembly started")
  ;;  (print (time (clear-cmt-assembly cfw_g_id threads version) 1))
  ;;  (print "clear-cmt-assembly completed")
  (set! assembly_id (get-assembly-id cfw_g_id threads version clones))
  (cond
   (
    (not
     (= 0 (dbu (string-append
		"update " asb-prefix "asb_assembly set assembly_id = assembly_id where assembly_id = "
		(sqlQuote assembly_id)))))
    (println `(Not creating new assembly for ,assembly_id which is already in the DB))
    )
   (else
    (set! cfw_group_id cfw_g_id)
    (set! threads (use-threads threads))
    (newline)
    (cond
     ((= 0 (dbu (string-append
		 "update " asb-prefix "asb_assembly set assembly_id = assembly_id where assembly_id = "
		 (sqlQuote assembly_id))))
      (dbu (string-append
	    "insert into " asb-prefix "asb_assembly "
	    "(ASSEMBLY_ID,ASSEMBLY_TYPE,DESCRIPTION)"
	    "values ("
	    (sqlQuote assembly_id)","
	    "'CMT',"
	    (sqlQuote assembly_description)")"))
      (print (string-append "inserted assembly_id " assembly_id " into " asb-prefix "ASB_ASSEMBLY table"))))

    (newline)
    (print "add-agent-alib-components started")
    (add-agent-alib-components cfw_g_id threads clones)
    (print "add-agent-alib-components completed")

    (newline)
    (print "add-base-asb-agents started")
    (print (time (add-base-asb-agents  cfw_group_id assembly_id)1))
    (print "add-base-asb-agents completed")

    (newline)
    (print "add-cloned-asb-agents started")
    (for-each (lambda (clone-inst)
		(add-cloned-asb-agents 
		 (first clone-inst)
		 (second clone-inst) assembly_id)
		)
	      clones)
    (print "add-cloned-asb-agents completed")

    (newline)
    ;; this must occur AFTER the asb_agents table is filled in, to allow for handling multiplicity
    (print "add-new-plugin-alib-components started")
    (add-new-plugin-alib-components cfw_group_id assembly_id threads)
    (print "add-new-plugin-alib-components completed")
  
    (newline)
    (print "add-plugin-asb-component-hierarchy started")
    (print (time (add-plugin-asb-component-hierarchy assembly_id cfw_group_id threads) 1))
    (print "add-plugin-asb-component-hierarchy completed")
  
    (newline)
    (print "add-agent-name-component-arg started")
    (print (time (add-agent-name-component-arg assembly_id cfw_group_id threads) 1))
    (print "add-agent-name-component-arg completed")
    (newline)
    (print "add-asb-agent-pg-attr started")
    (print (time (add-asb-agent-pg-attr assembly_id cfw_group_id threads) 1))
    (print "add-asb-agent-pg-attr completed")
    (newline)
    (print "add-all-asb-agent-relations started")
    (add-all-asb-agent-relations assembly_id cfw_group_id threads)
    (print "add-all-asb-agent-relations completed")
    (newline)
    (print "add-all-asb-agent-hierarchy-relations started")
    (add-all-asb-agent-hierarchy-relations assembly_id cfw_group_id threads)
    (print "add-all-asb-agent-hierarchy-relations completed")
    (add-plugin-args assembly_id cfw_group_id threads)
    (newline)
    (print "add-asb-oplan-agent-attr started")
    (add-asb-oplan-agent-attr assembly_id cfw_group_id threads "('093FF')")
    (print "add-asb-oplan-agent-attr completed")
    (newline)
    ))
  assembly_id
  )

(define (add-base-asb-agents cfw_group_id assembly_id)
  (dbu 
   (string-append
    "insert into " asb-prefix "asb_agent "
    "(ASSEMBLY_ID, COMPONENT_ALIB_ID, COMPONENT_LIB_ID, CLONE_SET_ID, COMPONENT_NAME)"
    "select distinct " (sqlQuote assembly_id) " as ASSEMBLY_ID,"
    "go.org_id as COMPONENT_ALIB_ID,"
    "go.org_id as COMPONENT_LIB_ID,"
    "0 as  CLONE_SET_ID,"
    "ac.component_name as COMPONENT_NAME"
    "   from " cfw-prefix "cfw_group_org go,"
    "   " asb-prefix "alib_component ac"
    "   where go.cfw_group_id=" (sqlQuote  cfw_group_id)
    "   and ac.component_alib_id=go.org_id"
    "   and not exists (select component_alib_id from " asb-prefix "asb_agent aa"
    "   where aa.component_alib_id=go.org_id"
    "   and aa.assembly_id="(sqlQuote assembly_id)")")))


(define (add-cloned-asb-agents org_group_id n assembly_id)
  (print (list 'add-cloned-asb-agents org_group_id n assembly_id))
  (print (time
	  (dbu
	   (string-append
	    "insert into " asb-prefix "asb_agent "
	    "(ASSEMBLY_ID, COMPONENT_ALIB_ID, COMPONENT_LIB_ID, CLONE_SET_ID, COMPONENT_NAME)"
	    "select distinct " (sqlQuote assembly_id) " as ASSEMBLY_ID,"
	    "(cs.clone_set_id || '-' || ogom.org_id) as COMPONENT_ALIB_ID,"
	    "ogom.org_id as COMPONENT_LIB_ID,"
	    "cs.clone_set_id as  CLONE_SET_ID,"
	    "ac.component_name as COMPONENT_NAME"
	    "   from "    cfw-prefix "cfw_org_group_org_member ogom,"
	    "   " asb-prefix "alib_component ac,"
	    "   " asb-prefix "lib_clone_set cs"
	    "   where ogom.org_group_id="(sqlQuote org_group_id)
	    "   and ac.component_alib_id=(cs.clone_set_id || '-' || ogom.org_id)"
	    "   and cs.clone_set_id>0 and cs.clone_set_id<"n
	    "   and not exists (select component_alib_id from " asb-prefix "asb_agent aa"
	    "   where aa.component_alib_id=(cs.clone_set_id || '-' || ogom.org_id)"
	    "   and aa.assembly_id="(sqlQuote assembly_id)")"))
	  1)
	 ))

(define (add-agent-alib-components cfw_g_id threads clones)
  (print (time (add-new-base-agent-alib-components cfw_group_id threads)1))
  (for-each (lambda (clone-inst)
	      (print (time
		      (add-new-cloned-agent-alib-components 
		       (first clone-inst)
		       (second clone-inst)
		       threads)
		     1)))
	    clones
	    )
  )

(define (get-base-agent-alib-component-sql cfw_group_id threads)
  (string-append 
   "select distinct go.org_id as COMPONENT_ALIB_ID,"
   ;;"org.org_name as COMPONENT_NAME,"   not according to Ray -- the name is the Agent name -- same as the component_alib_id
   "org.org_id as COMPONENT_NAME,"
   "org.org_id as COMPONENT_LIB_ID,"
   "'agent' as COMPONENT_TYPE,"
   "0 as  CLONE_SET_ID"
   " from " cfw-prefix "cfw_group_org go,"
   " " cfw-prefix "lib_organization org"
   "   where go.cfw_group_id=" (sqlQuote  cfw_group_id)
   "   and go.org_id =org.org_id"
   "   and go.org_id not in (select component_alib_id from " asb-prefix "alib_component)"
   ;;"   order by go.org_id,(pl.plugin_class_order+(5 * pg.plugin_group_order))"
   )
  )

(define (add-new-base-agent-alib-components cfw_group_id threads)
  (dbu
   (string-append 
    "insert into " asb-prefix "alib_component "
    "(COMPONENT_ALIB_ID, COMPONENT_NAME , COMPONENT_LIB_ID, COMPONENT_TYPE , CLONE_SET_ID)"
    (get-base-agent-alib-component-sql cfw_group_id threads))))


(define (get-cloned-agent-alib-component-sql org_group_id n threads)
  (string-append
   "select distinct "
   "(clone_set_id || '-' || ogom.org_id) as COMPONENT_ALIB_ID,"
   "(clone_set_id || '-' || ogom.org_id) as COMPONENT_NAME,"
   "ogom.org_id as COMPONENT_LIB_ID,"
   "'agent' as COMPONENT_TYPE,"
   "clone_set_id as  CLONE_SET_ID"
   "   from " cfw-prefix "cfw_org_group_org_member ogom,"
   "   " asb-prefix "lib_clone_set cs"
   "   where ogom.org_group_id="(sqlQuote org_group_id)
   "   and cs.clone_set_id>0 and cs.clone_set_id<"n
   "   and (clone_set_id || '-' || ogom.org_id) not in (select component_alib_id from " asb-prefix "alib_component)")
  )

(define (add-new-cloned-agent-alib-components org_group_id n threads)
  (print(list 'add-new-cloned-agent-alib-components org_group_id n threads))
  (dbu
   (string-append 
    "insert into " asb-prefix "alib_component "
    "(COMPONENT_ALIB_ID, COMPONENT_NAME , COMPONENT_LIB_ID, COMPONENT_TYPE , CLONE_SET_ID)"
    (get-cloned-agent-alib-component-sql org_group_id n threads))))

(define (get-plugin-alib-component-sql cfw_group_id assembly_id threads)
  ;; go through the agents in this assembly to fill in agent/plugin components in the alib
  (string-append 
   "select distinct"
   "(aa.component_alib_id || '|' ||  pl.plugin_class) as COMPONENT_ALIB_ID,"
   "(aa.component_alib_id || '|' ||  pl.plugin_class) as COMPONENT_NAME,"
   "'plugin'  || '|' || pl.plugin_class AS component_lib_id,"
   "'plugin' AS COMPONENT_TYPE,"
   "0 as  CLONE_SET_ID"
   " from " asb-prefix "ASB_AGENT aa,"
   cfw-prefix "cfw_group_member gm,"
   cfw-prefix "cfw_org_orgtype ot,"
   cfw-prefix "cfw_orgtype_plugin_grp pg,"
   cfw-prefix "cfw_plugin_group_member pl,"
   cfw-prefix "lib_plugin_thread pth"
   "   where gm.cfw_group_id=" (sqlQuote  cfw_group_id)
   "   and aa.assembly_id=" (sqlQuote assembly_id)
   "   and aa.component_lib_id=ot.org_id"
   "   and gm.cfw_id=ot.cfw_id"
   "   and ot.orgtype_id=pg.orgtype_id"
   "   and gm.cfw_id=pg.cfw_id"
   "   and pg.plugin_group_id = pl.plugin_group_id"
   "   and gm.cfw_id=pg.cfw_id"
   ;; safety "   and ('plugin'  || '|' || pl.plugin_class) in (select component_lib_id from " asb-prefix "lib_component)"
   "   and pth.plugin_class=pl.plugin_class"
   "   and pth.thread_id in " threads
   "   and (aa.component_alib_id || '|' ||  pl.plugin_class) not in (select component_alib_id from " asb-prefix "alib_component)"
   )
  )  

(define (add-new-plugin-alib-components cfw_group_id assembly_id threads)
  (dbu
   (string-append 
    "insert into " asb-prefix "alib_component "
    "(COMPONENT_ALIB_ID, COMPONENT_NAME , COMPONENT_LIB_ID, COMPONENT_TYPE , CLONE_SET_ID)"
    (get-plugin-alib-component-sql cfw_group_id assembly_id threads))))


;;select '3ID-135-CMT'as ASSEMBLY_ID,'3ID-135-CMT'|| '|' ||  pl.plugin_class AS COMPONENT_ID,
;;pl.plugin_class as COMPONENT_NAME,
;;null as PARENT_COMPONENT_ID,
;;org.org_id as COMPONENT_LIB_ID,
;;'plugin' as COMONENT_CATEGOR,
;;1 as INSERTION_ORDER  from " cfw-prefix "cfw_group_org go,
;; " cfw-prefix "lib_organization org,
;; " cfw-prefix "cfw_org_orgtype ot,
;; " cfw-prefix "cfw_orgtype_plugin_grp pg,
;; " cfw-prefix "cfw_plugin_group_member pl
;;   where cfw_group_id='3ID-CFW-GRP-A'
;;   and org.org_id=go.org_id
;;    and org.org_id=ot.org_id
;;    and ot.orgtype_id=pg.orgtype_id
;;    and pg.plugin_group_id = pl.plugin_group_id order by org.org_id;
;;
;;
(define (get-plugin-asb-component-hierarchy-sql assembly_id cfw_group_id threads)
  (string-append 
   "select distinct"
   (sqlQuote  assembly_id) "as ASSEMBLY_ID,"
   "(aa.component_alib_id || '|' ||  pl.plugin_class) as COMPONENT_ALIB_ID,"
   "aa.component_alib_id as PARENT_COMPONENT_ALIB_ID,"
   "(pl.plugin_class_order+(1000* pg.plugin_group_order)) as INSERTION_ORDER"

   " from " asb-prefix "ASB_AGENT aa,"
   cfw-prefix "cfw_group_member gm,"
   cfw-prefix "cfw_org_orgtype ot,"
   cfw-prefix "cfw_orgtype_plugin_grp opg,"
   cfw-prefix "lib_plugin_group pg,"
   cfw-prefix "cfw_plugin_group_member pl,"
   cfw-prefix "lib_plugin_thread pth"
   "   where gm.cfw_group_id=" (sqlQuote  cfw_group_id)
   "   and aa.assembly_id=" (sqlQuote assembly_id)
   "   and aa.component_lib_id=ot.org_id"

   "   and gm.cfw_id=ot.cfw_id"
   "   and ot.orgtype_id=opg.orgtype_id"

   "   and pg.plugin_group_id = pl.plugin_group_id"
   "   and pg.plugin_group_id = opg.plugin_group_id"
   "   and gm.cfw_id=opg.cfw_id"
   "   and gm.cfw_id=pl.cfw_id"
   ;; safety "   and ('plugin'  || '|' || pl.plugin_class) in (select component_lib_id from " asb-prefix "lib_component)"
   "   and pth.plugin_class=pl.plugin_class"
   "   and pth.thread_id in " threads
   "   and not exists "
   "   (select component_alib_id from " asb-prefix "asb_component_hierarchy ach"
   "    where ach.assembly_id="(sqlQuote  assembly_id)
   "   and ach.component_alib_id=(aa.component_alib_id || '|' ||  pl.plugin_class)"
   "   and ach.parent_component_alib_id=aa.component_alib_id)"
   )
)


(define (add-plugin-asb-component-hierarchy assembly_id cfw_group_id threads)
  (dbu
   (string-append 
    "insert into " asb-prefix "asb_component_hierarchy "
    "(ASSEMBLY_ID, COMPONENT_ALIB_ID, PARENT_COMPONENT_ALIB_ID, INSERTION_ORDER)"
    (get-plugin-asb-component-hierarchy-sql assembly_id cfw_group_id threads))))

(define (get-asb-agent-pg-attr-sql assembly_id cfw_group_id threads)
  (string-append 
   "select distinct " (sqlQuote assembly_id) " as ASSEMBLY_ID,"
   "aa.component_alib_id as COMPONENT_ALIB_ID,"
   "pga.pg_attribute_lib_id as PG_ATTRIBUTE_LIB_ID,"
   "pga.attribute_value as ATTRIBUTE_VALUE,"
   "pga.attribute_order as ATTRIBUTE_ORDER,"
   "pga.start_date as START_DATE,"
   "pga.end_date as END_DATE"

   "   from "
   asb-prefix "ASB_AGENT aa,"
   "   " cfw-prefix "cfw_group_member gm,"
   "   " cfw-prefix "cfw_org_pg_attr pga,"
   ;; lpga is for filtering out dirty data -- fix this soon RJB
   "   " asb-prefix "lib_pg_attribute lpga"

   "   where aa.assembly_id=" (sqlQuote assembly_id)
   "   and gm.cfw_group_id=" (sqlQuote  cfw_group_id)

   "   and aa.component_lib_ID=pga.org_id"
   "   and gm.cfw_id =pga.cfw_id"
   "   and lpga.pg_attribute_lib_id=pga.pg_attribute_lib_id"

   "   and not exists "
   "   (select assembly_id from " asb-prefix "asb_agent_pg_attr px"
   "     where px.assembly_id="(sqlQuote assembly_id)
   "     and px.component_alib_id=aa.component_alib_id"
   "     and px.pg_attribute_lib_id=pga.pg_attribute_lib_id"
   "     and px.start_date=pga.start_date)"
   )
  )

(define (add-asb-agent-pg-attr assembly_id cfw_group_id threads)
  (dbu
   (string-append 
    "insert into " asb-prefix "asb_agent_pg_attr "
    "(ASSEMBLY_ID, COMPONENT_ALIB_ID, PG_ATTRIBUTE_LIB_ID, ATTRIBUTE_VALUE, ATTRIBUTE_ORDER, START_DATE, END_DATE)"
    (get-asb-agent-pg-attr-sql assembly_id cfw_group_id threads))))

(define (get-asb-agent-relation-to-cloneset-sql assembly_id cfw_group_id threads)
  (string-append 
   "select distinct " (sqlQuote assembly_id) " as ASSEMBLY_ID,"
   "orgrel.role as ROLE,"
   "supporting_org.component_alib_id as SUPPORTING_COMPONENT_ALIB_ID,"
   "supported_org.component_alib_id as SUPPORTED_COMPONENT_ALIB_ID,"
   "orgrel.start_date as START_DATE,"
   "orgrel.end_date as END_DATE"
   "   from"
   "   " asb-prefix "asb_agent supported_org,"
   "   " asb-prefix "asb_agent supporting_org,"
   "   " cfw-prefix "cfw_org_og_relation orgrel,"
   "   " cfw-prefix "cfw_org_group_org_member ogom"

   "   where"
   "   orgrel.cfw_id in (select cfw_id from   " cfw-prefix "cfw_group_member where cfw_group_id="(sqlQuote  cfw_group_id)")"
   "   and supported_org.assembly_id="(sqlQuote assembly_id)
   "   and supporting_org.assembly_id="(sqlQuote assembly_id)
   "   and supporting_org.component_lib_id=orgrel.org_id"
   "   and ogom.cfw_id in (select cfw_id from   " cfw-prefix "cfw_group_member where cfw_group_id="(sqlQuote  cfw_group_id)")"
   "   and ogom.org_group_id = orgrel.org_group_id"
   "   and supported_org.component_lib_id=ogom.org_id"
   "   and supporting_org.clone_set_id=supported_org.clone_set_id"
   ;; no SELF-SUPPORT RELATIONS ALLOWED
   "   and supporting_org.component_alib_id<>supported_org.component_alib_id"
   "   and orgrel.role <> 'Subordinate'"
   "   and orgrel.role <> 'Superior'"
   "   and not exists "
   "   (select assembly_id from " asb-prefix "asb_agent_relation ar"
   "     where ar.assembly_id="(sqlQuote assembly_id)
;;   "     and ar.supporting_component_alib_id=orgrel.org_id"
   "     and ar.supported_component_alib_id =supported_org.component_alib_id"
   "     and ar.role=orgrel.role"
   "     and ar.start_date=orgrel.start_date)"
   )
  )

(define (add-asb-agent-relation-to-cloneset assembly_id cfw_group_id threads)
  (dbu
   (string-append 
    "insert into " asb-prefix "asb_agent_relation "
    "(ASSEMBLY_ID, ROLE, SUPPORTING_COMPONENT_ALIB_ID, SUPPORTED_COMPONENT_ALIB_ID, START_DATE, END_DATE)"
    (get-asb-agent-relation-to-cloneset-sql assembly_id cfw_group_id threads))))

(define (get-asb-agent-relation-to-base-sql assembly_id cfw_group_id threads)
  (string-append 
   "select distinct " (sqlQuote assembly_id) " as ASSEMBLY_ID,"
   "orgrel.role as ROLE,"
   "supporting_org.component_alib_id as SUPPORTING_COMPONENT_ALIB_ID,"
   "supported_org.component_alib_id as SUPPORTED_COMPONENT_ALIB_ID,"
   "orgrel.start_date as START_DATE,"
   "orgrel.end_date as END_DATE"
   "   from"
   "   " asb-prefix "asb_agent supported_org,"
   "   " asb-prefix "asb_agent supporting_org,"
   "   " cfw-prefix "cfw_org_og_relation orgrel,"
   "   " cfw-prefix "cfw_org_group_org_member ogom"

   "   where"
   "   orgrel.cfw_id in (select cfw_id from   " cfw-prefix "cfw_group_member where cfw_group_id="(sqlQuote  cfw_group_id)")"
   "   and supported_org.assembly_id="(sqlQuote assembly_id)
   "   and supporting_org.assembly_id="(sqlQuote assembly_id)
   "   and supporting_org.component_lib_id=orgrel.org_id"
   "   and ogom.cfw_id in (select cfw_id from   " cfw-prefix "cfw_group_member where cfw_group_id="(sqlQuote  cfw_group_id)")"
   "   and ogom.org_group_id = orgrel.org_group_id"
   "   and supported_org.component_lib_id=ogom.org_id"
   "   and supporting_org.clone_set_id=0"
   ;; no SELF-SUPPORT RELATIONS ALLOWED
   "   and supporting_org.component_alib_id<>supported_org.component_alib_id"
   "   and orgrel.role <> 'Subordinate'"
   "   and orgrel.role <> 'Superior'"
   "   and not exists "
   "   (select assembly_id from " asb-prefix "asb_agent_relation ar"
   "     where ar.assembly_id="(sqlQuote assembly_id)
;;   "     and ar.supporting_component_alib_id=orgrel.org_id"
   "     and ar.supported_component_alib_id =supported_org.component_alib_id"
   "     and ar.role=orgrel.role"
   "     and ar.start_date=orgrel.start_date)"
   )
  )

(define (add-asb-agent-relation-to-base assembly_id cfw_group_id threads)
  (dbu
   (string-append 
    "insert into " asb-prefix "asb_agent_relation "
    "(ASSEMBLY_ID, ROLE, SUPPORTING_COMPONENT_ALIB_ID, SUPPORTED_COMPONENT_ALIB_ID, START_DATE, END_DATE)"
    (get-asb-agent-relation-to-base-sql assembly_id cfw_group_id threads))))

(define (add-all-asb-agent-relations assembly_id cfw_group_id threads)
  ;; add the relations to the agents in the same cloneset
  (print (time (add-asb-agent-relation-to-cloneset assembly_id cfw_group_id threads)1))
  ;;  only if there are no relations of a given type within the cloneset,
  ;;   add relations from supported org (in the cloneset) to the org in cloneset 0
  (print (time (add-asb-agent-relation-to-base assembly_id cfw_group_id threads) 1)))


(define (get-asb-agent-hierarchy-relation-to-cloneset-sql assembly_id cfw_group_id threads)
  (string-append 
   "select distinct " (sqlQuote assembly_id) " as ASSEMBLY_ID,"
   "'Subordinate' as ROLE,"
   "supporting_org.component_alib_id as SUPPORTING_COMPONENT_ALIB_ID,"
   "supported_org.component_alib_id as SUPPORTED_COMPONENT_ALIB_ID,"
   "to_date('1-JAN-2001') as START_DATE,"
   "null as END_DATE"

   "   from"
   "   " asb-prefix "asb_agent supported_org,"
   "   " asb-prefix "asb_agent supporting_org,"
   "   " cfw-prefix "cfw_org_hierarchy oh"


   "   where"
   "   oh.cfw_id in (select cfw_id from   " cfw-prefix "cfw_group_member where cfw_group_id="(sqlQuote  cfw_group_id)")"
   "   and supported_org.assembly_id="(sqlQuote assembly_id)
   "   and supporting_org.assembly_id="(sqlQuote assembly_id)
   "   and oh.org_id=supporting_org.component_lib_id"
   "   and oh.superior_org_id=supported_org.component_lib_id"
   "   and supporting_org.clone_set_id=supported_org.clone_set_id"
   "   and not exists"
   "   (select assembly_id from " asb-prefix "asb_agent_relation ar"
   "     where ar.assembly_id="(sqlQuote assembly_id)
   "     and ar.supporting_component_alib_id=supporting_org.component_alib_id"
;;   "     and ar.supported_component_alib_id =oh.superior_org_id"
   "     and ar.role='Subordinate'"
   "     and ar.start_date=to_date('1-JAN-2001'))"
   )
  )

(define (add-asb-agent-hierarchy-relation-to-cloneset assembly_id cfw_group_id threads)
  (dbu
   (string-append 
    "insert into " asb-prefix "asb_agent_relation "
    "(ASSEMBLY_ID, ROLE, SUPPORTING_COMPONENT_ALIB_ID, SUPPORTED_COMPONENT_ALIB_ID, START_DATE, END_DATE)"
    (get-asb-agent-hierarchy-relation-to-cloneset-sql assembly_id cfw_group_id threads))))

(define (get-asb-agent-hierarchy-relation-to-base-sql assembly_id cfw_group_id threads)
  (string-append 
   "select distinct " (sqlQuote assembly_id) " as ASSEMBLY_ID,"
   "'Subordinate' as ROLE,"
   "supporting_org.component_alib_id as SUPPORTING_COMPONENT_ALIB_ID,"
   "supported_org.component_alib_id as SUPPORTED_COMPONENT_ALIB_ID,"
   "to_date('1-JAN-2001') as START_DATE,"
   "null as END_DATE"

   "   from"
   "   " asb-prefix "asb_agent supported_org,"
   "   " asb-prefix "asb_agent supporting_org,"
   "   " cfw-prefix "cfw_org_hierarchy oh"


   "   where"
   "   oh.cfw_id in (select cfw_id from   " cfw-prefix "cfw_group_member where cfw_group_id="(sqlQuote  cfw_group_id)")"
   "   and supported_org.assembly_id="(sqlQuote assembly_id)
   "   and supporting_org.assembly_id="(sqlQuote assembly_id)
   "   and oh.org_id=supporting_org.component_lib_id"
   "   and oh.superior_org_id=supported_org.component_lib_id"
   "   and supported_org.clone_set_id=0"
   "   and not exists"
   "   (select assembly_id from " asb-prefix "asb_agent_relation ar"
   "     where ar.assembly_id="(sqlQuote assembly_id)
   "     and ar.supporting_component_alib_id=supporting_org.component_alib_id"
   ;;   "     and ar.supported_component_alib_id =oh.superior_org_id"
   "     and ar.role='Subordinate'"
   "     and ar.start_date=to_date('1-JAN-2001'))"
   )
  )


(define (add-asb-agent-hierarchy-relation-to-base assembly_id cfw_group_id threads)
  (dbu
   (string-append 
    "insert into " asb-prefix "asb_agent_relation "
    "(ASSEMBLY_ID, ROLE, SUPPORTING_COMPONENT_ALIB_ID, SUPPORTED_COMPONENT_ALIB_ID, START_DATE, END_DATE)"
    (get-asb-agent-hierarchy-relation-to-base-sql assembly_id cfw_group_id threads))))


(define (add-all-asb-agent-hierarchy-relations assembly_id cfw_group_id threads)
  (print (time (add-asb-agent-hierarchy-relation-to-cloneset assembly_id cfw_group_id threads)1))
  (print (time (add-asb-agent-hierarchy-relation-to-base assembly_id cfw_group_id threads)1))
)




(define (get-asb-agent-hierarchy-relation-sql assembly_id cfw_group_id threads)
  (string-append 
   "select distinct " (sqlQuote assembly_id) " as ASSEMBLY_ID,"
   "'Subordinate' as ROLE,"
   "oh.org_id as SUPPORTING_COMPONENT_ALIB_ID,"
   "oh.superior_org_id as SUPPORTED_COMPONENT_ALIB_ID,"
   "to_date('1-JAN-2001') as START_DATE,"
   "null as END_DATE"

   "   from"
   "   " cfw-prefix "cfw_group_org go,"
   "   " cfw-prefix "cfw_group_org go_child,"
   "   " cfw-prefix "cfw_group_member gm,"
   "   " cfw-prefix "cfw_org_hierarchy oh"

   "   where"
   "   go.cfw_group_id=" (sqlQuote  cfw_group_id)
   "   and go_child.cfw_group_id=" (sqlQuote  cfw_group_id)
   "   and gm.cfw_group_id=" (sqlQuote  cfw_group_id)

   "   and gm.cfw_id = oh.cfw_id"
   "   and oh.org_id=go_child.org_id"
   "   and oh.superior_org_id=go.org_id"
   "   and not exists"
   "   (select assembly_id from " asb-prefix "asb_agent_relation ar"
   "     where ar.assembly_id="(sqlQuote assembly_id)
   "     and ar.supporting_component_alib_id=oh.org_id"
   "     and ar.supported_component_alib_id =oh.superior_org_id"
   "     and ar.role='Subordinate'"
   "     and ar.start_date=to_date('1-JAN-2001'))"
   )
  )

(define (add-asb-agent-hierarchy-relation assembly_id cfw_group_id threads)
  (dbu
   (string-append 
    "insert into " asb-prefix "asb_agent_relation "
    "(ASSEMBLY_ID, ROLE, SUPPORTING_COMPONENT_ALIB_ID, SUPPORTED_COMPONENT_ALIB_ID, START_DATE, END_DATE)"
    (get-asb-agent-hierarchy-relation-sql assembly_id cfw_group_id threads))))



(define   (add-plugin-args assembly_id cfw_group_id  threads)
  (print "")
  (print "add-plugin-agent-asb-component-arg started")
  (print (time (add-plugin-agent-asb-component-arg assembly_id cfw_group_id threads)1))
  (print "add-plugin-agent-asb-component-arg completed")
  (print "")
  (print "add-plugin-orgtype-asb-component-arg started")
  (print (time (add-plugin-orgtype-asb-component-arg assembly_id cfw_group_id threads)1))
  (print "add-plugin-orgtype-asb-component-arg completed")
  (print "")
  (print "add-plugin-all-asb-component-arg started")
  (print (time (add-plugin-all-asb-component-arg assembly_id cfw_group_id threads)1))
  (print "add-plugin-all-asb-component-arg completed")
  )

(define (get-agent-name-component-arg-sql assembly_id cfw_group_id threads)
  (string-append 
   "select distinct " (sqlQuote assembly_id) " as ASSEMBLY_ID,"
   "aa.component_alib_id as COMPONENT_ALIB_ID,"
   "aa.component_alib_id as ARGUMENT,"
   "0 as ARGUMENT_ORDER"

   " from"
   "   " asb-prefix "asb_agent aa"

   "   where"
   "   aa.assembly_id=" (sqlQuote assembly_id)

   "   and not exists ("
   "   select assembly_id from " asb-prefix "asb_component_arg aca"
   "   where"
   "   assembly_id="(sqlQuote assembly_id)
   "   and aca.component_alib_id=aa.component_alib_id"
   "   and aca.argument=aa.component_alib_id"
   ")"
   )
  )

(define (add-agent-name-component-arg assembly_id cfw_group_id threads)
  (dbu
   (string-append 
    "insert into " asb-prefix "asb_component_arg "
    "(ASSEMBLY_ID, COMPONENT_ALIB_ID, ARGUMENT, ARGUMENT_ORDER)"
    (get-agent-name-component-arg-sql assembly_id cfw_group_id threads))))



(define (get-plugin-agent-asb-component-arg-sql assembly_id cfw_group_id threads)
  (string-append 
   "select distinct " (sqlQuote assembly_id) " as ASSEMBLY_ID,"
   "ch.component_alib_id as COMPONENT_ALIB_ID,"
   "pa.argument as ARGUMENT,"
   "pa.argument_order as ARGUMENT_ORDER"

   " from"
   "   " asb-prefix "asb_component_hierarchy ch,"
   "   " asb-prefix "alib_component plugin_alib,"
   "   " asb-prefix "asb_agent org_agent,"
   "   " cfw-prefix "cfw_context_plugin_arg cpa,"
   "   " cfw-prefix "lib_plugin_arg pa,"
   "   " cfw-prefix "lib_plugin_arg_thread pat,"
   "   " cfw-prefix "cfw_group_member gm"

   "   where"
   "   gm.cfw_group_id=" (sqlQuote  cfw_group_id)
   "   and ch.assembly_id="(sqlQuote assembly_id)
   "   and plugin_alib.component_type='plugin'"
   "   and ch.parent_component_alib_id=org_agent.component_alib_id"
   "   and ch.component_alib_id=plugin_alib.component_alib_id"
   "   and cpa.cfw_id=gm.cfw_id"
   "   and pa.argument is not null"

   "   and cpa.org_context = org_agent.component_lib_id"
   "   and pa.plugin_arg_id=cpa.plugin_arg_id"
   "   and ('plugin' || '|' || pa.plugin_class)=plugin_alib.component_lib_id"
   "   and pa.plugin_arg_id=pat.plugin_arg_id"
   "   and pat.thread_id in " threads
   "   and not exists ("
   "   select assembly_id from " asb-prefix "asb_component_arg aca"
   "   where"
   "   assembly_id="(sqlQuote assembly_id)
   "   and aca.component_alib_id=ch.component_alib_id"
   ")"
   )
  )

(define (add-plugin-agent-asb-component-arg assembly_id cfw_group_id threads)
  (dbu
   (string-append 
    "insert into " asb-prefix "asb_component_arg "
    "(ASSEMBLY_ID, COMPONENT_ALIB_ID, ARGUMENT, ARGUMENT_ORDER)"
    (get-plugin-agent-asb-component-arg-sql assembly_id cfw_group_id threads))))


(define (get-plugin-orgtype-asb-component-arg-sql assembly_id cfw_group_id threads)
  (string-append 
   "select distinct " (sqlQuote assembly_id) " as ASSEMBLY_ID,"
   "ch.component_alib_id as COMPONENT_ALIB_ID,"
   "pa.argument as ARGUMENT,"
   "pa.argument_order as ARGUMENT_ORDER"

   " from"
   "   " asb-prefix "asb_component_hierarchy ch,"
   "   " asb-prefix "alib_component plugin_alib,"
   "   " asb-prefix "asb_agent org_agent,"
   "   " cfw-prefix "cfw_context_plugin_arg cpa,"
   "   " cfw-prefix "cfw_org_orgtype ot,"
   "   " cfw-prefix "lib_plugin_arg pa,"
   "   " cfw-prefix "lib_plugin_arg_thread pat,"
   "   " cfw-prefix "cfw_group_member gm"

   "   where"
   "   gm.cfw_group_id=" (sqlQuote  cfw_group_id)
   "   and ch.assembly_id="(sqlQuote assembly_id)
   "   and ch.parent_component_alib_id=org_agent.component_alib_id"
   "   and ch.component_alib_id=plugin_alib.component_alib_id"
   "   and cpa.cfw_id=gm.cfw_id"
   "   and ot.cfw_id=gm.cfw_id"
   "   and ot.org_id=org_agent.component_lib_id"
   "   and pa.argument is not null"

   "   and cpa.org_context = ot.orgtype_id"
   "   and pa.plugin_arg_id=cpa.plugin_arg_id"
   "   and ('plugin' || '|' || pa.plugin_class)=plugin_alib.component_lib_id"
   "   and pa.plugin_arg_id=pat.plugin_arg_id"
   "   and pat.thread_id in " threads
   "   and not exists ("
   "   select assembly_id from " asb-prefix "asb_component_arg aca"
   "   where"
   "   assembly_id="(sqlQuote assembly_id)
   "   and aca.component_alib_id=ch.component_alib_id"
   "   and aca.argument_order=pa.argument_order"
   ")"
   )
  )


(define (add-plugin-orgtype-asb-component-arg assembly_id cfw_group_id threads)
  (dbu
   (string-append 
    "insert into " asb-prefix "asb_component_arg "
    "(ASSEMBLY_ID, COMPONENT_ALIB_ID, ARGUMENT, ARGUMENT_ORDER)"
    (get-plugin-orgtype-asb-component-arg-sql assembly_id cfw_group_id threads))))

(define (get-plugin-all-asb-component-arg-sql assembly_id cfw_group_id threads)
  (string-append 
   "select distinct " (sqlQuote assembly_id) " as ASSEMBLY_ID,"
   "ch.component_alib_id as COMPONENT_ALIB_ID,"
   "pa.argument as ARGUMENT,"
   "pa.argument_order as ARGUMENT_ORDER"

   " from"
   "   " asb-prefix "asb_component_hierarchy ch,"
   "   " asb-prefix "alib_component plugin_alib,"
   "   " asb-prefix "asb_agent org_agent,"
   "   " cfw-prefix "cfw_context_plugin_arg cpa,"
   "   " cfw-prefix "lib_plugin_arg pa,"
   "   " cfw-prefix "lib_plugin_arg_thread pat,"
   "   " cfw-prefix "cfw_group_member gm"

   "   where"
   "   gm.cfw_group_id=" (sqlQuote  cfw_group_id)
   "   and ch.assembly_id="(sqlQuote assembly_id)
   "   and ch.parent_component_alib_id=org_agent.component_alib_id"
   "   and ch.component_alib_id=plugin_alib.component_alib_id"
   "   and cpa.cfw_id=gm.cfw_id"
   "   and pa.argument is not null"

   "   and cpa.org_context = 'ALL'"
   "   and pa.plugin_arg_id=cpa.plugin_arg_id"
   "   and ('plugin' || '|' || pa.plugin_class)=plugin_alib.component_lib_id"
   "   and pa.plugin_arg_id=pat.plugin_arg_id"
   "   and pat.thread_id in " threads
   "   and not exists ("
   "   select assembly_id from " asb-prefix "asb_component_arg aca"
   "   where"
   "   assembly_id="(sqlQuote assembly_id)
   "   and aca.component_alib_id=ch.component_alib_id"
   ")"
   )
  )


(define (add-plugin-all-asb-component-arg assembly_id cfw_group_id threads)
  (dbu
   (string-append 
    "insert into " asb-prefix "asb_component_arg "
    "(ASSEMBLY_ID, COMPONENT_ALIB_ID, ARGUMENT, ARGUMENT_ORDER)"
    (get-plugin-all-asb-component-arg-sql assembly_id cfw_group_id threads))))


;; OPLAN

(define (get-asb-oplan-agent-attr-sql assembly_id cfw_group_id threads oplan_ids)
  (string-append 
   "select distinct " (sqlQuote assembly_id) " as ASSEMBLY_ID,"
   "ooa.oplan_id as OPLAN_ID,"
   "aa.component_alib_id as COMPONENT_ALIB_ID,"
   "aa.component_alib_id as COMPONENT_ID,"
   "ooa.start_cday as START_CDAY,"
   "ooa.attribute_name as ATTRIBUTE_NAME,"
   "ooa.end_cday as END_CDAY,"
   "ooa.attribute_value as ATTRIBUTE_VALUE"

   "   from"
   "   " asb-prefix "asb_agent aa,"
   "   " cfw-prefix "cfw_group_member gm,"
   "   " cfw-prefix "cfw_oplan_og_attr ooa,"
   "   " cfw-prefix "cfw_org_group_org_member ogom"

   "   where"
   "   gm.cfw_group_id=" (sqlQuote  cfw_group_id)
   "   and aa.assembly_id="(sqlQuote assembly_id)
   "   and ooa.cfw_id=gm.cfw_id"
   "   and ogom.cfw_id=gm.cfw_id"
   "   and ogom.org_id=aa.component_lib_id"
   "   and ooa.org_group_id=ogom.org_group_id"
   "   and ooa.oplan_id in "  oplan_ids

 
   "   and not exists "
   "   (select assembly_id from " asb-prefix "asb_oplan_agent_attr ar"
   "     where ar.assembly_id="(sqlQuote assembly_id)
   "     and ar.oplan_id=ooa.oplan_id"
   "     and ar.component_alib_id=aa.component_alib_id"
   "     and ar.component_id=aa.component_alib_id"
   "     and ar.start_cday=ooa.start_cday"
   "     and ar.attribute_name=ooa.attribute_name)"
   )
  )



(define (add-asb-oplans assembly_id cfw_group_id threads oplan_ids)
  (dbu
   (string-append
    "insert into " asb-prefix "asb_oplan"
    "(ASSEMBLY_ID, OPLAN_ID, OPERATION_NAME , PRIORITY, C0_DATE)"
    " select distinct " (sqlQuote assembly_id) " as ASSEMBLY_ID,"
    "op.oplan_id as OPLAN_ID,"
    "op.operation_name as OPERATION_NAME,"
    "op.priority as PRIORITY,"
    "op.c0_date as C0_DATE"
    " from " cfw-prefix "cfw_oplan op,"
    "   " cfw-prefix "cfw_group_member gm"
    "   where gm.cfw_id=op.cfw_id"
    "   and op.oplan_id in " oplan_ids
    "   and not exists "
    "   (select oplan_id from " asb-prefix "asb_oplan"
    "     where assembly_id="(sqlQuote assembly_id)
    "      and oplan_id in "  oplan_ids
    ")")))


(define (add-asb-oplan-agent-attr assembly_id cfw_group_id threads oplan_ids)
  (add-asb-oplans  assembly_id cfw_group_id threads oplan_ids)
  (dbu
   (string-append 
    "insert into " asb-prefix "asb_oplan_agent_attr "
    "(ASSEMBLY_ID, OPLAN_ID, COMPONENT_ALIB_ID, COMPONENT_ID, START_CDAY, ATTRIBUTE_NAME , END_CDAY, ATTRIBUTE_VALUE)"
    (get-asb-oplan-agent-attr-sql assembly_id cfw_group_id threads oplan_ids))))


;; testing code
(define (get-plugin-component-lib-id-sql assembly_id cfw_group_id threads)
  (string-append 
   "select distinct 'plugin'  || '|' || pl.plugin_class AS component_lib_id"
   " from " cfw-prefix "cfw_group_org go,"
   cfw-prefix "cfw_org_orgtype ot,"
   cfw-prefix "cfw_group_member gm,"
   cfw-prefix "cfw_orgtype_plugin_grp pg,"
   cfw-prefix "cfw_plugin_group_member pl,"
   cfw-prefix "lib_plugin_thread pth"
   "   where go.cfw_group_id=" (sqlQuote  cfw_group_id)
   "   and gm.cfw_group_id=" (sqlQuote  cfw_group_id)

   "   and go.org_id =ot.org_id"
   "   and gm.cfw_id=ot.cfw_id"

   "   and ot.orgtype_id=pg.orgtype_id"
   "   and gm.cfw_id=pg.cfw_id"

   "   and pg.plugin_group_id = pl.plugin_group_id"
   "   and gm.cfw_id=pg.cfw_id"
   "   and ('plugin'  || '|' || pl.plugin_class) not in (select component_lib_id from " asb-prefix "lib_component)"
   "   and pth.plugin_class=pl.plugin_class"
   "   and pth.thread_id in " threads
   " order by 'plugin'  || '|' || pl.plugin_class"
   )
  )



(define (voc)
  (let
      ((assembly_id "3ID-135-CMT")
       (cfw_group_id "3ID-CFW-GRP-A"))
    (view_org_components assembly_id cfw_group_id)))

(define (vpc)
  (let
      ((assembly_id "3ID-135-CMT")
       (cfw_group_id "3ID-CFW-GRP-A"))
    (view_plugin_components assembly_id cfw_group_id)))
    



(define (vq query)
  (view-query    
   (getrefDBConnection)
   query))

(define (cq query)
  (collect-query    
   (getrefDBConnection)
   query))

(define (dbu query)
  (newline)
  (println (string-append "dbu:" query))
  (db-update
   (getrefDBConnection)
   query)
  )

(define (missing-asb-component-hierarchy-plugin-sql assembly_id cfw_group_id)
  (string-append 
   "select distinct " (sqlQuote  assembly_id) "as ASSEMBLY_ID,"
   "go.org_id || '|' ||  pl.plugin_class AS COMPONENT_ALIB_ID,"
   "go.org_id as PARENT_COMPONENT_ALIB_ID,"
   "(pl.plugin_class_order+(1000* pg.plugin_group_order)) as INSERTION_ORDER"

   " from " cfw-prefix "cfw_group_org go,"
   cfw-prefix "cfw_org_orgtype ot,"
   cfw-prefix "cfw_group_member gm,"
   cfw-prefix "cfw_orgtype_plugin_grp opg,"
   cfw-prefix "lib_plugin_group pg,"
   cfw-prefix "cfw_plugin_group_member pl,"
   "   where go.cfw_group_id=" (sqlQuote  cfw_group_id)
   "   and gm.cfw_group_id=" (sqlQuote  cfw_group_id)

   "   and go.org_id =ot.org_id"
   "   and gm.cfw_id=ot.cfw_id"

   "   and ot.orgtype_id=opg.orgtype_id"
   "   and gm.cfw_id=opg.cfw_id"
   "   and gm.cfw_id=pl.cfw_id"

   "   and pg.plugin_group_id = pl.plugin_group_id"
   "   and pg.plugin_group_id = opg.plugin_group_id"
   "   and (go.org_id || '|' ||  pl.plugin_class) not in (select component_alib_id from " asb-prefix "alib_component)"
   "   and ('plugin'  || '|' || pl.plugin_class) in (select component_lib_id from " asb-prefix "lib_component)"
   ;;"   order by go.org_id,(pl.plugin_class_order+(5 * pg.plugin_group_order))"
   )
  )



;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;  CSMART INTERFACE ;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(define (getExperimentNames)
  (let
      ((ht (Hashtable.)))
    (define (do-entry rs)
      (cond
       ((.next rs)
	(.put ht (.getString rs "DESCRIPTION")(.getString rs "EXPT_ID"))
	(do-entry rs))))
    (with-query-jdbc (string-append
		      "select  expt_id,description from " asb-prefix "EXPT_EXPERIMENT")
		     do-entry)
    ht))


(define (getTrialNames experiment_id)
  (let
      ((ht (Hashtable.)))
    (define (do-entry rs)
      (cond
       ((.next rs)
	(if (not (eq? #null (.getString rs "DESCRIPTION")))
	    (.put ht (.getString rs "DESCRIPTION")(.getString rs "TRIAL_ID")))
	(do-entry rs))))
    (with-query-jdbc (string-append 
		      "select  trial_id,description from " asb-prefix "EXPT_TRIAL where expt_id=" (sqlQuote experiment_id))
		     do-entry
		     )
    ht))


(define (getUniqueTrialName experiment_id)
  (query-1-string (string-append 
		   "select  trial_id from " asb-prefix "EXPT_TRIAL where expt_id=" (sqlQuote experiment_id))
		  "TRIAL_ID"
		  )
  )

(define (removeTrial experiment_id trial_id)
  (let((x ()))
    (dbu (string-append "delete from  V4_EXPT_TRIAL_ASSEMBLY   where trial_id="(sqlQuote trial_id)))
    (dbu (string-append "delete from  V4_EXPT_TRIAL   where trial_id="(sqlQuote trial_id)))
    ))


;;describe V4_EXPT_TRIAL
;;
;; Name				 Null?	  Type
;; ------------------------------- -------- ----
;; TRIAL_ID			 NOT NULL VARCHAR2(50)
;; EXPT_ID				  VARCHAR2(50)
;; DESCRIPTION				  VARCHAR2(100)
;; NAME					  VARCHAR2(50)


(define (addTrialName experiment_id trial_name)
  (let
      ((trial_id (string-append experiment_id "." trial_name)))
    (cond
     ;; don't insert twice
     ((= 0 (dbu (string-append
		 "update " asb-prefix "EXPT_TRIAL set expt_id = expt_id  where trial_id = "
		 (sqlQuote trial_id))))
      (dbu (string-append 
	    "insert into " asb-prefix
	    "EXPT_TRIAL "
	    "(TRIAL_ID, EXPT_ID, DESCRIPTION, NAME)"
	    "values (" (sqlQuote trial_id)","(sqlQuote experiment_id)","(sqlQuote trial_name)","(sqlQuote trial_name)")"))))
    trial_id))


;;describe V4_EXPT_TRIAL_ASSEMBLY
;;
;; Name				 Null?	  Type
;; ------------------------------- -------- ----
;; EXPT_ID			 NOT NULL VARCHAR2(50)
;; TRIAL_ID			 NOT NULL VARCHAR2(50)
;; ASSEMBLY_ID			 NOT NULL VARCHAR2(50)
;; DESCRIPTION				  VARCHAR2(200)
;;

(define (createCSMARTExperiment expt_id cfw_group_id assembly_id)
  (createExperiment expt_id cfw_group_id)
  (set! trial (addTrialName expt_id "TRIAL"))
  (addAssembly expt_id "TRIAL" assembly_id))



(define (addAssembly experiment_id trial_name assembly_id)
  (let
      ((trial_id (addTrialName experiment_id trial_name)))
    (dbu (string-append 
	  "insert into " asb-prefix "EXPT_TRIAL_ASSEMBLY "
	  "(EXPT_ID,TRIAL_ID,ASSEMBLY_ID,DESCRIPTION)"
	  "   values ("(sqlQuote experiment_id)"," (sqlQuote trial_id)","(sqlQuote assembly_id)","(sqlQuote trial_name)")"))
    trial_id))

(define (getSocietyTemplateForExperiment experiment_id)
  (let
      ((st #null))
    (define (do-entry rs)
      (cond
       ((.next rs)
	(set! st (.getString rs "CFW_GROUP_ID"))
	)))
    (with-query-jdbc (string-append 
		      "select  CFW_GROUP_ID from " asb-prefix "EXPT_EXPERIMENT where expt_id=" (sqlQuote experiment_id))
		     do-entry
		     )
    st)
  )

;;describe V4_EXPT_EXPERIMENT
;; Name				 Null?	  Type
;; ------------------------------- -------- ----
;; EXPT_ID			 NOT NULL VARCHAR2(50)
;; DESCRIPTION				  VARCHAR2(200)
;; NAME					  VARCHAR2(50)
;; CFW_GROUP_ID				  VARCHAR2(50)
;;

(define (createExperiment experiment_id cfw_group_id)
  (cond
     ;; don't insert twice
     ((= 0 (dbu (string-append
		 "update " asb-prefix "EXPT_EXPERIMENT set expt_id = expt_id  where EXPT_ID = "
		 (sqlQuote experiment_id))))
      (dbu (string-append 
	    "insert into " asb-prefix
	    "EXPT_EXPERIMENT "
	    "(EXPT_ID, DESCRIPTION, NAME, CFW_GROUP_ID)"
	    "   values ("
	    (sqlQuote experiment_id)","
	    (sqlQuote experiment_id)","
	    (sqlQuote experiment_id)","
	    (sqlQuote cfw_group_id)")"))
      ))
  experiment_id
  )



(define (getSocietyTemplates)
  (let
      ((ht (Hashtable.)))
    (define (do-entry rs)
      (cond
       ((.next rs)
	(.put ht (.getString rs "DESCRIPTION")(.getString rs "CFW_GROUP_ID"))
	(do-entry rs))))
    (with-query-jdbc (string-append 
		      "select  DESCRIPTION,CFW_GROUP_ID from " cfw-prefix "CFW_GROUP")
		     do-entry
		     )
    ht))


(define (getOrganizationGroups experiment_id)
  (let
      ((ht (Hashtable.)))
    (define (do-entry rs)
      (cond
       ((.next rs)
	(.put ht (.getString rs "DESCRIPTION")(.getString rs "ORG_GROUP_ID"))
	(do-entry rs)
	)))
    (with-query-jdbc (string-append 
		      "select distinct ogom.ORG_GROUP_ID, og.DESCRIPTION from " 
		      asb-prefix "EXPT_EXPERIMENT exp,"
		      cfw-prefix "CFW_GROUP_MEMBER gm,"
		      cfw-prefix cog-type"ORG_GROUP og,"
		      cfw-prefix "CFW_ORG_GROUP_ORG_MEMBER ogom"
		      "   where exp.expt_id="(sqlQuote experiment_id)
		      "   and exp.cfw_group_id =gm.cfw_group_id"
		      "   and ogom.cfw_id=gm.cfw_id"
		      "   and og.org_group_id=ogom.org_group_id"
		      "   and og.description like '%CLONABLE%'"
		      )
		     do-entry
		     )
    ;;    (.put ht "Third Infantry Division" "Third Infantry Division")
    ;;    (.put ht "2nd Brigade" "2nd Brigade")
    ht)
  )


(define (getOrganizationsInGroup experiment_id group_id)
  (let
      ((hs (HashSet.)))
    (define (do-entry rs)
      (cond
       ((.next rs)
	(.add hs (.getString rs "ORG_ID"))
	(do-entry rs)
	)))
    (with-query-jdbc (string-append 
		      "select om.ORG_ID from " 
		      asb-prefix "EXPT_EXPERIMENT exp,"
		      cfw-prefix "CFW_GROUP_MEMBER gm,"
		      cfw-prefix "CFW_ORG_GROUP_ORG_MEMBER om"
		      "   where exp.expt_id="(sqlQuote experiment_id)
		      "   and exp.cfw_group_id =gm.cfw_group_id"
		      "   and om.cfw_id=gm.cfw_id"
		      "   and om.org_group_id="(sqlQuote group_id))
		     do-entry
		     )
;;    (.put ht "Third Infantry Division" "Third Infantry Division")
;;    (.put ht "2nd Brigade" "2nd Brigade")
    hs)
  )

;;describe v4_expt_trial_thread
;; Name				 Null?	  Type
;; ------------------------------- -------- ----
;; EXPT_ID			 NOT NULL VARCHAR2(50)
;; TRIAL_ID			 NOT NULL VARCHAR2(50)
;; THREAD_ID			 NOT NULL VARCHAR2(50)
;;

(define (isULThreadSelected trial_id thread_name)
  (let*
      ((thread_id (get-thread-id thread_name))
       (threadSelected #f))
    
    (define (do-entry rs)
      (cond
       ((.next rs)
	(set! threadSelected #t)
	)))
    (with-query-jdbc (string-append 
		      "select THREAD_ID from " 
		      asb-prefix "expt_trial_thread tt"
		      "   where tt.trial_id="(sqlQuote trial_id)
		      "   and tt.thread_id="(sqlQuote thread_id)
		      )
		     do-entry
		     )
    threadSelected
    ))

(define (get-thread-id thread_name)
  (cond
   ((.equals "Subsistence (Class 1)" thread_name) "CLASS-1")
   ((.equals "Fuel (Class 3)" thread_name) "CLASS-3")
   ((.equals "Construction Material (Class 4)" thread_name)"CLASS-4")
   ((.equals "Ammunition (Class 5)" thread_name)"CLASS-5")
   ((.equals "Spare Parts (Class 9)" thread_name)"CLASS-9")
   ((.equals "CLASS-1" thread_name) "CLASS-1")
   ((.equals "CLASS-3" thread_name) "CLASS-3")
   ((.equals "CLASS-4" thread_name)"CLASS-4")
   ((.equals "CLASS-5" thread_name)"CLASS-5")
   ((.equals "CLASS-9" thread_name) "CLASS-9")))


(define (setULThreadSelected trial_id thread_name) 
  (let
      ((thread_id (get-thread-id thread_name)))
    (cond
     ;; don't insert twice
     ((= 0 (dbu (string-append
		 "update " asb-prefix "EXPT_TRIAL_THREAD tt"
		 " set THREAD_ID=THREAD_ID" 
		 "   where tt.trial_id="(sqlQuote trial_id)
		 "   and tt.thread_id="(sqlQuote thread_id)
		 )))
      (dbu (string-append 
	    "insert into " asb-prefix "EXPT_TRIAL_THREAD"
	    "(EXPT_ID,TRIAL_ID, THREAD_ID)"
	    "    select expt_id, trial_id, " (sqlQuote thread_id)
	    "    from V4_EXPT_TRIAL where trial_id = " (sqlQuote trial_id)
	    ))))))

(define (setULThreadNotSelected trial_id thread_name) 
  (let
      ((thread_id (get-thread-id thread_name)))
    (dbu (string-append 
	  "delete from " asb-prefix "EXPT_TRIAL_THREAD tt"
	  "   where tt.trial_id="(sqlQuote trial_id)
	  "   and tt.thread_id="(sqlQuote thread_id)
	  )))
  )


;;  public static boolean isGroupSelected(String trialId, String groupName) {
;;    return false;
;;  }

(define (isGroupSelected trial_id group_name)
  (let*
      ((groupSelected #f)
       (group_id (get-group-id trial_id group_name)))
    (define (do-entry rs)
      (cond
       ((.next rs)
	(set! groupSelected #t)
	)))
    (with-query-jdbc (string-append 
		      "select org_group_id from " 
		      asb-prefix "EXPT_TRIAL_ORG_MULT om"
		      "   where om.trial_id="(sqlQuote trial_id)
		      "   and om.org_group_id="(sqlQuote group_id)
		      )
		     do-entry
		     )
    groupSelected
    ))


;;CREATE TABLE V4_EXPT_TRIAL_ORG_MULT(
;;    EXPT_ID      VARCHAR2(50)    NOT NULL,
;;    TRIAL_ID     VARCHAR2(50)    NOT NULL,       
;;    CFW_ID          VARCHAR2(50)    NOT NULL,
;;    ORG_GROUP_ID    VARCHAR2(50)    NOT NULL,
;;    MULTIPLIER	    NUMBER,
;;    DESCRIPTION     VARCHAR2(50),
;;

(define (get-group-id trial_id group_name)
  (query-1-string
   (string-append
    "select og.ORG_GROUP_ID from"
    "   " asb-prefix "EXPT_EXPERIMENT exp,"
    "   " asb-prefix "EXPT_TRIAL et,"
    "   " cfw-prefix "CFW_GROUP_MEMBER gm,"
    "   " cfw-prefix cog-type "ORG_GROUP og,"
    "   " cfw-prefix "CFW_ORG_GROUP_ORG_MEMBER ogom"
    "   where et.trial_id="(sqlQuote trial_id)
    "   and exp.expt_id=et.expt_id"
    "   and exp.cfw_group_id =gm.cfw_group_id"
    "   and ogom.cfw_id=gm.cfw_id"
    "   and ogom.org_group_id=og.org_group_id"
    "   and og.description="(sqlQuote group_name))
   "ORG_GROUP_ID"))

(define (setGroupSelected trial_id group_name selected)
  (let
      ((group_id (get-group-id trial_id group_name)))
    (cond
     ((isGroupSelected trial_id group_id)
      (if (not selected)
	  (dbu
	   (string-append 
	    "delete from " 
	    asb-prefix "EXPT_TRIAL_ORG_MULT om"
	    "   where om.trial_id="(sqlQuote trial_id)
	    "   and om.org_group_id ="(sqlQuote group_id)
	   ))))
     (selected
      (dbu (string-append
	    "insert into " asb-prefix "EXPT_TRIAL_ORG_MULT"
	    "(TRIAL_ID, CFW_ID , ORG_GROUP_ID, EXPT_ID, MULTIPLIER, DESCRIPTION)"
	    "   select distinct "
	    "   et.expt_id,"
	    "   et.trial_id,"
	    "   gm.cfw_id,"
	    "   og.org_group_id,"
	    "   1,"
	    "   null"
	    "   from "
	    asb-prefix "EXPT_EXPERIMENT exp,"
	    asb-prefix "EXPT_TRIAL et,"
	    cfw-prefix "CFW_GROUP_MEMBER gm,"
	    cfw-prefix cog-type "ORG_GROUP og"
	    "   where et.trial_id="(sqlQuote trial_id)
	    "   and exp.expt_id=et.expt_id"
	    "   and exp.cfw_group_id =gm.cfw_group_id"
	    "   and og.cfw_id=gm.cfw_id"
	    "   and og.org_group_id="(sqlQuote group_id))
	   )))))

(define (getMultiplier trial_id group_name)
  (let*
      ((group_id (get-group-id trial_id group_name))
       (mult
	(query-1-int
	 (string-append 
	  "select om.MULTIPLIER from " 
	  asb-prefix "EXPT_TRIAL_ORG_MULT om"
	  "   where om.trial_id="(sqlQuote trial_id)
	  "   and om.org_group_id="(sqlQuote group_id)
	  )
	 "MULTIPLIER"
	 )))
    (if
     (eq? mult #null)
     1
     mult)))

(define (setMultiplier trial_id group_name value)
  (let
      ((group_id (get-group-id trial_id group_name)))
    (dbu
     (string-append 
      "update " asb-prefix "EXPT_TRIAL_ORG_MULT"
      "   set MULTIPLIER= " value
      "   where trial_id="(sqlQuote trial_id)
      "   and org_group_id="(sqlQuote group_id)
      )
     )
    )
  )

(define (setSocietyTemplate experiment_id cfw_group_id)
  (cond
     ;; don't insert twice
     ((= 0 (dbu (string-append
		 "update " asb-prefix "EXPT_EXPERIMENT set expt_id = expt_id  where EXPT_ID = "
		 (sqlQuote experiment_id))))
      (dbu (string-append 
	    "insert into " asb-prefix
	    "EXPT_EXPERIMENT "
	    "(EXPT_ID, DESCRIPTION, NAME, CFW_GROUP_ID)"
	    "values ("
	    (sqlQuote experiment_id)","
	    (sqlQuote experiment_id)","
	    (sqlQuote experiment_id)","
	    (sqlQuote cfw_group_id)")"))
      ))
  (dbu (string-append 
	    "update " asb-prefix "EXPT_EXPERIMENT"
	    " set cfw_group_id= " (sqlQuote cfw_group_id)
	    " where expt_id=" (sqlQuote experiment_id)))
  experiment_id
  )

;;describe V4_ASB_COMPONENT_HIERARCHY
;; Name				 Null?	  Type
;; ------------------------------- -------- ----
;; ASSEMBLY_ID			 NOT NULL VARCHAR2(50)
;; COMPONENT_ALIB_ID		 NOT NULL VARCHAR2(150)
;; PARENT_COMPONENT_ALIB_ID	 NOT NULL VARCHAR2(150)
;; INSERTION_ORDER			  NUMBER
;;

(define (addNodeAssignments nodeTable assemblyName)
  (let
      ((assembly_id assemblyName))
    (addCSMARTAssembly assembly_id assemblyName)
    (for-each*
     (lambda (nodeName)
       (for-each*
	(lambda (agentName)
	  (cond
	   ;; don't insert twice
	   ((= 0 (dbu (string-append
		       "update " asb-prefix "ASB_COMPONENT_HIERARCHY a set assembly_id = assembly_id"
		       "   where assembly_id = " (sqlQuote assembly_id) 
		       "   and exists ("
		       "   select assembly_id"
		       "   from "
		       "   " asb-prefix "ASB_COMPONENT_HIERARCHY b,"
		       "   " asb-prefix "ALIB_COMPONENT node,"
		       "   " asb-prefix "ALIB_COMPONENT agent"
		       "   where"
		       "   assembly_id = " (sqlQuote assembly_id) 
		       "   and node.COMPONENT_NAME="(sqlQuote nodeName)
		       "   and agent.COMPONENT_NAME="(sqlQuote agentName)
		       "   and b.component_alib_id=agent.component_alib_id"
		       "   and b.parent_component_alib_id=node.component_alib_id"
		       ")")))
	    (dbu (string-append 
		  "insert into " asb-prefix "ASB_COMPONENT_HIERARCHY"
		  "(ASSEMBLY_ID, COMPONENT_ALIB_ID, PARENT_COMPONENT_ALIB_ID, INSERTION_ORDER)"
		  "   select distinct " (sqlQuote assembly_id) ",agent.component_alib_id,node.component_alib_id,0"
		  "   from "
		  "   " asb-prefix "ALIB_COMPONENT node,"
		  "   " asb-prefix "ALIB_COMPONENT agent"
		  "   where"
		  "   node.COMPONENT_NAME="(sqlQuote nodeName)
		  "   and agent.COMPONENT_NAME="(sqlQuote agentName)
		  ))
	    ))
	  )
	(.get nodeTable nodeName)))
     (.keySet nodeTable))
    assembly_id
    )
  )

(define (addMachineAssignments machineTable assemblyName)
  (let
      ((assembly_id assemblyName))
    (addCSMARTAssembly assembly_id assemblyName)
    (for-each*
     (lambda (machineName)
       (for-each*
	(lambda (nodeName)
	  (cond
	   ;; don't insert twice
	   ((= 0 (dbu (string-append
		       "update " asb-prefix "ASB_COMPONENT_HIERARCHY a set assembly_id = assembly_id"
		       "   where assembly_id = " (sqlQuote assembly_id) 
		       "   and exists ("
		       "   select assembly_id"
		       "   from "
		       "   " asb-prefix "ASB_COMPONENT_HIERARCHY b,"
		       "   " asb-prefix "ALIB_COMPONENT machine,"
		       "   " asb-prefix "ALIB_COMPONENT node"
		       "   where"
		       "   assembly_id = " (sqlQuote assembly_id) 
		       "   and machine.COMPONENT_NAME="(sqlQuote machineName)
		       "   and node.COMPONENT_NAME="(sqlQuote nodeName)
		       "   and b.component_alib_id=node.component_alib_id"
		       "   and b.parent_component_alib_id=machine.component_alib_id"
		       ")")))
	    (dbu (string-append 
		  "insert into " asb-prefix "ASB_COMPONENT_HIERARCHY"
		  "(ASSEMBLY_ID, COMPONENT_ALIB_ID, PARENT_COMPONENT_ALIB_ID, INSERTION_ORDER)"
		  "   select distinct " (sqlQuote assembly_id) ",node.component_alib_id,machine.component_alib_id,0"
		  "   from "
		  "   " asb-prefix "ALIB_COMPONENT machine,"
		  "   " asb-prefix "ALIB_COMPONENT node"
		  "   where"
		  "   machine.COMPONENT_NAME="(sqlQuote machineName)
		  "   and node.COMPONENT_NAME="(sqlQuote nodeName)
		  ))
	    ))
	  )
	(.get machineTable machineName)))
     (.keySet machineTable))
    assembly_id)
  )

(define (addCSMARTAssembly assembly_id assembly_description)
  (cond
   ((= 0 (dbu (string-append
	       "update " asb-prefix "asb_assembly set assembly_id = assembly_id where assembly_id = "
	       (sqlQuote assembly_id))))
    (dbu (string-append
	  "insert into " asb-prefix "asb_assembly"
	  "(ASSEMBLY_ID, ASSEMBLY_TYPE, DESCRIPTION)"
	  "values ("
	  (sqlQuote assembly_id)","
	  "'CSMART',"
	  (sqlQuote assembly_description)")"))
    )))


(define (not-exists table id_field where)
  (= 0
     (dbu (string-append "update " table "set " field "="field where))))



;;describe v4_expt_experiment
;; Name				 Null?	  Type
;; ------------------------------- -------- ----
;; EXPT_ID			 NOT NULL VARCHAR2(50)
;; DESCRIPTION				  VARCHAR2(200)
;; NAME					  VARCHAR2(50)
;; CFW_GROUP_ID				  VARCHAR2(50)

;;describe V4_EXPT_TRIAL
;; Name				 Null?	  Type
;; ------------------------------- -------- ----
;; TRIAL_ID			 NOT NULL VARCHAR2(50)
;; EXPT_ID				  VARCHAR2(50)
;; DESCRIPTION				  VARCHAR2(100)
;; NAME					  VARCHAR2(50)
;;
;;describe v4_expt_trial_assembly
;;Name				 Null?	  Type
;; ------------------------------- -------- ----
;; EXPT_ID			 NOT NULL VARCHAR2(50)
;; TRIAL_ID			 NOT NULL VARCHAR2(50)
;; ASSEMBLY_ID			 NOT NULL VARCHAR2(50)
;; DESCRIPTION				  VARCHAR2(200)
;;

(define (cloneExperiment experiment_id new_name)
  (let*
      ((new_expt_id (string-append "EXPT-" (query-1-int "select experiment_number.nextval from dual" "NEXTVAL")))
       )
    (dbu (string-append
	  "insert into " asb-prefix "expt_experiment"
	  "(EXPT_ID, DESCRIPTION, NAME, CFW_GROUP_ID)"
	  "   select " (sqlQuote new_expt_id) ","
	  "   " (sqlQuote new_name) ","
	  "   " (sqlQuote new_name) ","
	  "   cfw_group_id from " asb-prefix "expt_experiment where expt_id=" (sqlQuote experiment_id)))
    (dbu (string-append
	  "insert into " asb-prefix "expt_trial"
	  "(TRIAL_ID, EXPT_ID, DESCRIPTION, NAME)"
	  "   select " 
	  "   " (sqlQuote (string-append new_expt_id ".TRIAL")) ","
	  "   " (sqlQuote new_expt_id) ","
	  "   " (sqlQuote new_name) ","
	  "   "(sqlQuote new_name) 
	  "   from "
	  "   "	  asb-prefix "expt_trial where expt_id=" (sqlQuote experiment_id)))

;;describe v4_expt_trial_thread
;; Name				 Null?	  Type
;; ------------------------------- -------- ----
;; EXPT_ID			 NOT NULL VARCHAR2(50)
;; TRIAL_ID			 NOT NULL VARCHAR2(50)
;; THREAD_ID			 NOT NULL VARCHAR2(50)
;;


    (dbu (string-append
	  "insert into " asb-prefix "expt_trial_thread"
	  "( EXPT_ID, TRIAL_ID, THREAD_ID)"
	  "   select " 
	  "   " (sqlQuote new_expt_id) ","
	  "   " (sqlQuote (string-append new_expt_id ".TRIAL")) ","
	  "   thread_id"
	  "   from "
	  "   "	  asb-prefix "expt_trial_thread where expt_id=" (sqlQuote experiment_id)))

;;SQL> describe v4_expt_trial_org_mult
;; Name				 Null?	  Type
;; ------------------------------- -------- ----
;; EXPT_ID			 NOT NULL VARCHAR2(50)
;; TRIAL_ID			 NOT NULL VARCHAR2(50)
;; CFW_ID 			 NOT NULL VARCHAR2(50)
;; ORG_GROUP_ID			 NOT NULL VARCHAR2(50)
;; MULTIPLIER				  NUMBER
;; DESCRIPTION				  VARCHAR2(50)
;;

    (dbu (string-append
	  "insert into " asb-prefix "expt_trial_org_mult"
	  "(TRIAL_ID, CFW_ID , ORG_GROUP_ID, EXPT_ID, MULTIPLIER, DESCRIPTION)"
	  "   select " 
	  "   " (sqlQuote new_expt_id) ","
	  "   " (sqlQuote (string-append new_expt_id ".TRIAL")) ","
	  "   cfw_id,"
	  "   org_group_id,"
	  "   multiplier,"
	  "   description"
	  "   from "
	  "   "	  asb-prefix "expt_trial_org_mult where expt_id=" (sqlQuote experiment_id)))
    (dbu (string-append
	  "insert into " asb-prefix "expt_trial_assembly"
	  "(EXPT_ID,TRIAL_ID,ASSEMBLY_ID,DESCRIPTION)"
	  "   select " 
	  "   " (sqlQuote new_expt_id) ","
	  "   " (sqlQuote (string-append new_expt_id ".TRIAL")) ","
	  "   ta.assembly_id,"
	  "   ta.description"
	  "   from "
	  "   "	  asb-prefix "expt_trial_assembly ta,"
	  "   "	  asb-prefix "asb_assembly a"
	  "   where expt_id=" (sqlQuote experiment_id)
	  "   and ta.assembly_id=a.assembly_id"
	  "   and a.assembly_type <> 'CSM'"))
    new_expt_id
    ))



(define (deleteExperiment experiment_id)
  (dbu (string-append
	"delete from " asb-prefix "expt_trial_assembly"
	"   where expt_id=" (sqlQuote experiment_id)
	))
  (dbu (string-append
	"delete from " asb-prefix "expt_trial_thread"
	"   where expt_id=" (sqlQuote experiment_id)
	))
  (dbu (string-append
	"delete from " asb-prefix "expt_trial_org_mult"
	"   where expt_id=" (sqlQuote experiment_id)
	))
  (dbu (string-append
	"delete from "
	"   "	  asb-prefix "expt_trial"
	"   where expt_id=" (sqlQuote experiment_id)))
  (dbu (string-append
	"delete from "
	asb-prefix "expt_experiment"
	"   where expt_id=" (sqlQuote experiment_id)))
  (clear-unused-cmt-assemblies)
  )

(define (query-set query col)
  ;; cols is a String[].
  (with-query-jdbc
   query
   (lambda (result)
     (do ((ans (HashSet.)))
         ((not (.next result)) ans)     
       (.add ans (.getString result col))))))

(define (query-list query cols)
  ;; cols is a String[].
  (with-query-jdbc
   query
   (lambda (result)
     (do ((ans ()))
         ((not (.next result)) ans)     
       (set! ans 
	     (append ans
		     (list
		      (map (lambda (col)
			     ((eval (first col)) result (second col)))
			   cols))))))))


(define (updateCMTAssembly experiment_id)
  (let*
      ((assembly_description (string-append "assembly for: " experiment_id))
       (threads 
	(query-set 
	 (string-append 
	  "select thread_id from "
	  asb-prefix "expt_trial_thread"
	  "   where expt_id="(sqlQuote experiment_id))
	 "THREAD_ID"))
       (cfw_group_id 
	(query-1-string
	 (string-append
	  "select cfw_group_id from "
	  asb-prefix "expt_experiment"
	  "  where  expt_id=" (sqlQuote experiment_id))
	 "CFW_GROUP_ID"))
       (clones 
	(query-list (string-append
		     "select org_group_id, multiplier from "
		    "   " asb-prefix "EXPT_TRIAL_ORG_MULT" 
		    "   where multiplier >1"
		    "   and expt_id=" (sqlQuote experiment_id))
		    '((.getString "ORG_GROUP_ID")(.getInt "MULTIPLIER"))))
       )
    (.add threads "STRATEGIC-TRANS")
    (.add threads "THEATER-TRANS")
    (set! threads (order-threads threads))
    ;;(println (list 'create-cmt-asb assembly_description cfw_group_id threads "" clones))
    (set! assembly_id (create-cmt-asb assembly_description cfw_group_id threads "" clones))
    (dbu (string-append
	  "update "
	  asb-prefix "expt_trial_assembly" 
	  "   set assembly_id="(sqlQuote assembly_id)
	  "   where expt_id="(sqlQuote experiment_id)
	  "   and assembly_id like 'CMT-%'"))
    ))

(define (listif bool item)
  (if bool (list item) ()))

(define (order-threads set)
  (append
   (listif (.contains set "STRATEGIC-TRANS") "STRATEGIC-TRANS")
   (listif (.contains set "THEATER-TRANS") "THEATER-TRANS")
   (listif (.contains set "CLASS-1") "CLASS-1")
   (listif (.contains set "CLASS-3") "CLASS-3")
   (listif (.contains set "CLASS-4") "CLASS-4")
   (listif (.contains set "CLASS-5") "CLASS-5")
;;   (listif (.contains set "CLASS-9") "CLASS-9")
   ))


(define (expt_agents expt_id)
  (query-set
   (string-append
    "select aa.component_alib_id from "
    asb-prefix "expt_trial_assembly ta,"
    asb-prefix "asb_agent aa"
    "   where "
    "   ta.expt_id="(sqlQuote expt_id)
    "   and ta.assembly_id=aa.assembly_id"
    )
   "component_alib_id"))


(define [] java.lang.reflect.Array.get)
(define []! java.lang.reflect.Array.set)
(define [].length java.lang.reflect.Array.getLength)

(define (string-array l)
  (list->array String.class l))

(define (plugin_args agent-match)
  (let
      ((query
	(string-append
	 "select * from v4_asb_component_arg where component_alib_id like '%"
	 agent-match
	 "%' and assembly_id like '%ST3%' order by component_alib_id")))
    (println (list 'query query))
    (vq query)))


(list '
 (vq "SELECT DISTINCT 
   ':assembly_id'  AS ASSEMBLY_ID, 
   CH.COMPONENT_ALIB_ID AS COMPONENT_ALIB_ID, 
   PA.ARGUMENT AS ARGUMENT, 
   PA.ARGUMENT_ORDER AS ARGUMENT_ORDER 
  FROM 
    V4_ASB_COMPONENT_HIERARCHY CH, 
    V4_ALIB_COMPONENT PLUGIN_ALIB, 
    V4_ASB_AGENT ORG_AGENT, 
    V6_CFW_CONTEXT_PLUGIN_ARG CPA, 
    V6_LIB_PLUGIN_ARG PA, 
    V6_LIB_PLUGIN_ARG_THREAD PAT 
  WHERE 
    PLUGIN_ALIB.COMPONENT_TYPE='plugin'
    AND CH.PARENT_COMPONENT_ALIB_ID=ORG_AGENT.COMPONENT_ALIB_ID 
    AND CH.COMPONENT_ALIB_ID=PLUGIN_ALIB.COMPONENT_ALIB_ID 
    AND CPA.CFW_ID IN (SELECT CFW_ID FROM V6_CFW_GROUP_MEMBER WHERE CFW_GROUP_ID LIKE 'TINY-1AD-TRANS-STUB%')
    AND PA.ARGUMENT IS NOT NULL 
    AND CPA.ORG_CONTEXT = ORG_AGENT.COMPONENT_LIB_ID 
    AND PA.PLUGIN_ARG_ID=CPA.PLUGIN_ARG_ID 
    AND ('plugin|' || PA.PLUGIN_CLASS)=PLUGIN_ALIB.COMPONENT_LIB_ID 
    AND PA.PLUGIN_ARG_ID=PAT.PLUGIN_ARG_ID ")


'(vq "  SELECT DISTINCT 
   ':assembly_id'  AS ASSEMBLY_ID, 
   CH.COMPONENT_ALIB_ID AS COMPONENT_ALIB_ID, 
   PA.ARGUMENT AS ARGUMENT, 
   PA.ARGUMENT_ORDER AS ARGUMENT_ORDER 
  FROM 
   V4_ASB_COMPONENT_HIERARCHY CH, 
   V4_ALIB_COMPONENT PLUGIN_ALIB, 
   V4_ASB_AGENT ORG_AGENT, 
   V6_CFW_CONTEXT_PLUGIN_ARG CPA, 
   V6_CFW_ORG_ORGTYPE OT, 
   V6_LIB_PLUGIN_ARG PA, 
   V6_LIB_PLUGIN_ARG_THREAD PAT 
  WHERE 
   CH.PARENT_COMPONENT_ALIB_ID=ORG_AGENT.COMPONENT_ALIB_ID 
   AND CH.COMPONENT_ALIB_ID=PLUGIN_ALIB.COMPONENT_ALIB_ID 
   AND CPA.CFW_ID IN (SELECT CFW_ID FROM V6_CFW_GROUP_MEMBER WHERE CFW_GROUP_ID LIKE 'TINY-1AD-TRANS-STUB%') 
   AND OT.CFW_ID IN (SELECT CFW_ID FROM V6_CFW_GROUP_MEMBER WHERE CFW_GROUP_ID LIKE 'TINY-1AD-TRANS-STUB%') 
   AND OT.ORG_ID=ORG_AGENT.COMPONENT_LIB_ID 
   AND PA.ARGUMENT IS NOT NULL 
   AND CPA.ORG_CONTEXT = OT.ORGTYPE_ID 
   AND PA.PLUGIN_ARG_ID=CPA.PLUGIN_ARG_ID 
   AND ('plugin|' || PA.PLUGIN_CLASS)=PLUGIN_ALIB.COMPONENT_LIB_ID 
   AND PA.PLUGIN_ARG_ID=PAT.PLUGIN_ARG_ID")



)


