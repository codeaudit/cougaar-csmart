(import "java.sql.DriverManager")
(load "elf/jdbc.scm")
(load "elf/basic.scm")
(load "build/compile.scm")
(import "java.util.Date")

(set! cfw-prefix "V1_")
(set! asb-prefix "V4_")




;; for BBN Eiger access
(define (use-eiger user password)
  (set! refDBDriver   "oracle.jdbc.driver.OracleDriver")
  (set! refDBConnURL  "jdbc:oracle:thin:@eiger.alpine.bbn.com:1521:alp")
  (set! refDBUser     user)
  (set! refDBPasswd   password)
  (initialize)
  )

(define (initialize)
  ;; Register the Oracle driver. 
  ;;(DriverManager.registerDriver (OracleDriver.))
  (Class.forName refDBDriver)
  ;; Open a connection to the reference database
  (set! refDBConnection (DriverManager.getConnection refDBConnURL refDBUser refDBPasswd))
  #t)

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

(define (with-query-jdbc query ex)
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
    answer))

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
  (set! assembly_id (get-assembly-id cfw_g_id threads version))
  (dbu (string-append "delete from " asb-prefix "asb_component_hierarchy where assembly_id = " (sqlQuote assembly_id)))
  (dbu (string-append "delete from " asb-prefix "asb_agent_pg_attr where assembly_id = " (sqlQuote assembly_id)))
  (dbu (string-append "delete from " asb-prefix "asb_agent_relation where assembly_id = " (sqlQuote assembly_id)))
  (dbu (string-append "delete from " asb-prefix "asb_component_arg where assembly_id = " (sqlQuote assembly_id)))
  ;;(dbu (string-append "delete from " asb-prefix "alib_component"))
  (dbu (string-append "delete from " asb-prefix "asb_assembly  where assembly_id = " (sqlQuote assembly_id)))
  )

(define (use-threads tlist)
  (let
      ((tl (cons "BASE" tlist)))
    (sqlList tl)
    )
  )

(set! all-threads '(STRATEGIC-TRANS THEATER-TRANS CLASS-1 CLASS-3 CLASS-4 CLASS-5 CLASS-8 CLASS-9))
(set! 135threads '(STRATEGIC-TRANS THEATER-TRANS CLASS-1 CLASS-3 CLASS-5))
(set! 1thread '(STRATEGIC-TRANS THEATER-TRANS CLASS-1))
(set! 3thread '(STRATEGIC-TRANS THEATER-TRANS CLASS-3))
(set! 5thread  '(STRATEGIC-TRANS THEATER-TRANS CLASS-5))

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
   ((eq? thread 'CLASS-9)9)))


(define (get-assembly-id cfw_g_id threads version)
  (set! assembly_id (string-append "CMT-" cfw_g_id
				   version
				   "{" (apply string-append (map tabbrev threads)) "}")))


(define (create-cmt-asb assembly_description cfw_g_id threads version)
  ;; intentionally setting a global variable for debugging purposes
  (print "clear-cmt-assembly started")
  (print (time (clear-cmt-assembly cfw_g_id threads version) 1))
  (print "clear-cmt-assembly completed")
  (set! assembly_id (get-assembly-id cfw_g_id threads version))
  (set! cfw_group_id cfw_g_id)
  (set! threads (use-threads threads))
  (print "update_cmt_lib_items started")
  (print (time (update_cmt_lib_items cfw_g_id threads) 1))
  (print "update_cmt_lib_items completed")
  (cond
   ((= 0 (dbu (string-append
	       "update " asb-prefix "asb_assembly set assembly_id = assembly_id where assembly_id = "
	       (sqlQuote assembly_id))))
    (dbu (string-append
	  "insert into " asb-prefix "asb_assembly values ("
	  (sqlQuote assembly_id)","
	  "'CMT',"
	  (sqlQuote assembly_description)")"))
    (print (string-append "inserted assembly_id " assembly_id " into " asb-prefix "ASB_ASSEMBLY table"))))
  (print "")
  (print "add-plugin-asb-component-hierarchy started")
  (print (time (add-plugin-asb-component-hierarchy assembly_id cfw_group_id threads) 1))
  (print "add-plugin-asb-component-hierarchy completed")
  (print "")
  (print "add-agent-name-component-arg started")
  (print (time (add-agent-name-component-arg assembly_id cfw_group_id threads) 1))
  (print "add-agent-name-component-arg completed")
  (print "")
  (print "add-asb-agent-pg-attr started")
  (print (time (add-asb-agent-pg-attr assembly_id cfw_group_id threads) 1))
  (print "add-asb-agent-pg-attr completed")
  (print "")
  (print "add-asb-agent-relation started")
  (print (time (add-asb-agent-relation assembly_id cfw_group_id threads)1))
  (print "add-asb-agent-relation completed")
  (print "")
  (print "add-asb-agent-hierarchy-relation started")
  (print (time (add-asb-agent-hierarchy-relation assembly_id cfw_group_id threads)1))
  (print "add-asb-agent-hierarchy-relation completed")
  (add-plugin-args assembly_id cfw_group_id threads)
  "done"
  )
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
   "select distinct " (sqlQuote  assembly_id) "as ASSEMBLY_ID,"
   "go.org_id || '|' ||  pl.plugin_class AS COMPONENT_ALIB_ID,"
   "go.org_id as PARENT_COMPONENT_ALIB_ID,"
   "(pl.plugin_class_order+(5* pg.plugin_group_order)) as INSERTION_ORDER"

   " from " cfw-prefix "cfw_group_org go,"
   cfw-prefix "cfw_org_orgtype ot,"
   cfw-prefix "cfw_group_member gm,"
   cfw-prefix "cfw_orgtype_plugin_grp opg,"
   cfw-prefix "lib_plugin_group pg,"
   cfw-prefix "cfw_plugin_group_member pl,"
   cfw-prefix "lib_plugin_thread pth"
   "   where go.cfw_group_id=" (sqlQuote  cfw_group_id)
   "   and gm.cfw_group_id=" (sqlQuote  cfw_group_id)
   
   "   and go.org_id =ot.org_id"
   "   and gm.cfw_id=ot.cfw_id"

   "   and ot.orgtype_id=opg.orgtype_id"
   "   and gm.cfw_id=opg.cfw_id"

   "   and pg.plugin_group_id = pl.plugin_group_id"
   "   and pg.plugin_group_id = opg.plugin_group_id"
   "   and ('plugin'  || '|' || pl.plugin_class) in (select component_lib_id from " asb-prefix "lib_component)"
   "   and pth.plugin_class=pl.plugin_class"
   "   and pth.thread_id in " threads
   ;;"   order by go.org_id,(pl.plugin_class_order+(5 * pg.plugin_group_order))"
   )
  )

(define (add-plugin-asb-component-hierarchy assembly_id cfw_group_id threads)
  (dbu
   (string-append 
    "insert into " asb-prefix "asb_component_hierarchy "
    (get-plugin-asb-component-hierarchy-sql assembly_id cfw_group_id threads))))



(define (update_cmt_lib_items cfw_g_id threads)
  (add-new-plugin-alib-component cfw_group_id  threads)
  (add-new-agent-alib-component cfw_group_id threads))


(define (get-agent-alib-component-sql cfw_group_id threads)
  (string-append 
   "select distinct go.org_id as COMPONENT_ALIB_ID,"
   "org.org_name as COMPONENT_NAME,"
   "org.org_id as COMPONENT_LIB_ID,"
   "'agent' as COMPONENT_TYPE,"
   "0 as  CLONE_SET_ID"
   " from " cfw-prefix "cfw_group_org go,"
   " " cfw-prefix "lib_organization org,"
   " " asb-prefix "lib_component comp"
   "   where go.cfw_group_id=" (sqlQuote  cfw_group_id)
   "   and go.org_id =org.org_id"
   "   and org.org_id=comp.component_lib_id"
   "   and go.org_id not in (select component_alib_id from " asb-prefix "alib_component)"
   ;;"   order by go.org_id,(pl.plugin_class_order+(5 * pg.plugin_group_order))"
   )
  )

(define (add-new-agent-alib-component cfw_group_id threads)
  (dbu
   (string-append 
    "insert into " asb-prefix "alib_component "
    (get-agent-alib-component-sql cfw_group_id threads))))


(define (get-plugin-alib-component-sql cfw_group_id threads)
  (string-append 
   "select distinct go.org_id || '|' ||  pl.plugin_class AS COMPONENT_ALIB_ID,"
   "go.org_id || '|' ||  pl.plugin_class as COMPONENT_NAME,"
   "'plugin'  || '|' || pl.plugin_class AS component_lib_id,"
   "'plugin' AS COMPONENT_TYPE,"
   "0 as  CLONE_SET_ID"
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
   "   and go.org_id || '|' ||  pl.plugin_class not in (select component_alib_id from " asb-prefix "alib_component)"
   "   and ('plugin'  || '|' || pl.plugin_class) in (select component_lib_id from " asb-prefix "lib_component)"
   "   and pth.plugin_class=pl.plugin_class"
   "   and pth.thread_id in " threads
   )
  )

(define (add-new-plugin-alib-component cfw_group_id threads)
  (dbu
   (string-append 
    "insert into " asb-prefix "alib_component "
    (get-plugin-alib-component-sql cfw_group_id threads))))


(define (get-asb-agent-pg-attr-sql assembly_id cfw_group_id threads)
  (string-append 
   "select distinct " (sqlQuote assembly_id) " as ASSEMBLY_ID,"
   "go.org_id AS COMPONENT_ALIB_ID,"
   "pga.pg_attribute_lib_id as PG_ATTRIBUTE_LIB_ID,"
   "pga.attribute_value as ATTRIBUTE_VALUE,"
   "pga.attribute_order as ATTRIBUTE_ORDER,"
   "pga.start_date as START_DATE,"
   "pga.end_date as END_DATE"


   "   from " cfw-prefix "cfw_group_org go,"
   "   " cfw-prefix "cfw_group_member gm,"
   "   " cfw-prefix "cfw_org_pg_attr pga,"
   "   " asb-prefix "lib_pg_attribute lpga"

   "   where go.cfw_group_id=" (sqlQuote  cfw_group_id)
   "   and gm.cfw_group_id=" (sqlQuote  cfw_group_id)

   "   and go.org_id =pga.org_id"
   "   and gm.cfw_id =pga.cfw_id"
   "   and lpga.pg_attribute_lib_id=pga.pg_attribute_lib_id"

   "   and not exists "
   "   (select assembly_id from " asb-prefix "asb_agent_pg_attr px"
   "     where px.assembly_id="(sqlQuote assembly_id)
   "     and px.component_alib_id=go.org_id"
   "     and px.pg_attribute_lib_id=pga.pg_attribute_lib_id"
   "     and px.start_date=pga.start_date)"
   )
  )

(define (add-asb-agent-pg-attr assembly_id cfw_group_id threads)
  (dbu
   (string-append 
    "insert into " asb-prefix "asb_agent_pg_attr "
    (get-asb-agent-pg-attr-sql assembly_id cfw_group_id threads))))



(define (get-asb-agent-relation-sql assembly_id cfw_group_id threads)
  (string-append 
   "select distinct " (sqlQuote assembly_id) " as ASSEMBLY_ID,"
   "orgrel.role as ROLE,"
   "orgrel.org_id as SUPPORTING_COMPONENT_ALIB_ID,"
   "ogom.org_id as SUPPORTED_COMPONENT_ALIB_ID,"
   "orgrel.start_date as START_DATE,"
   "orgrel.end_date as END_DATE"


   "   from"
   "   " cfw-prefix "cfw_group_org go,"
   "   " cfw-prefix "cfw_group_member gm,"
   "   " cfw-prefix "cfw_org_og_relation orgrel,"
;;   "   " cfw-prefix "lib_role_thread thread,"
   "   " cfw-prefix "cfw_org_group_org_member ogom"


   "   where go.cfw_group_id=" (sqlQuote  cfw_group_id)
   "   and gm.cfw_group_id=" (sqlQuote  cfw_group_id)

   "   and go.org_id = orgrel.org_id"
   "   and gm.cfw_id = orgrel.cfw_id"
   "   and ogom.cfw_id = orgrel.cfw_id"
   "   and ogom.org_group_id = orgrel.org_group_id"
   
   ;; role threads
;;   "   and orgrel.role=thread.role"
;;   "   and thread.thread_id in " threads
 
   "   and not exists "
   "   (select assembly_id from " asb-prefix "asb_agent_relation ar"
   "     where ar.assembly_id="(sqlQuote assembly_id)
   "     and ar.supporting_component_alib_id=orgrel.org_id"
   "     and ar.supported_component_alib_id =ogom.org_id"
   "     and ar.role=orgrel.role"
   "     and ar.start_date=orgrel.start_date)"
   )
  )

(define (add-asb-agent-relation assembly_id cfw_group_id threads)
  (dbu
   (string-append 
    "insert into " asb-prefix "asb_agent_relation "
    (get-asb-agent-relation-sql assembly_id cfw_group_id threads))))


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
   "org_alib.component_alib_id as COMPONENT_ALIB_ID,"
   "org_alib.component_name as ARGUMENT,"
   "0 as ARGUMENT_ORDER"

   " from"
   "   " asb-prefix "asb_component_hierarchy ch,"
   "   " asb-prefix "alib_component org_alib"

   "   where"
   "   org_alib.component_type='agent'"
   "   and ch.parent_component_alib_id=org_alib.component_alib_id"

   "   and not exists ("
   "   select assembly_id from " asb-prefix "asb_component_arg aca"
   "   where"
   "   assembly_id="(sqlQuote assembly_id)
   "   and aca.component_alib_id=org_alib.component_alib_id"
   "   and aca.argument=org_alib.component_name"
   ")"
   )
  )

(define (add-agent-name-component-arg assembly_id cfw_group_id threads)
  (dbu
   (string-append 
    "insert into " asb-prefix "asb_component_arg "
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
   "   " asb-prefix "alib_component org_alib,"
   "   " cfw-prefix "cfw_context_plugin_arg cpa,"
   "   " cfw-prefix "lib_plugin_arg pa,"
   "   " cfw-prefix "lib_plugin_arg_thread pat,"
   "   " cfw-prefix "cfw_group_member gm"

   "   where"
   "   gm.cfw_group_id=" (sqlQuote  cfw_group_id)
   "   and org_alib.component_type='agent'"
   "   and plugin_alib.component_type='plugin'"
   "   and ch.parent_component_alib_id=org_alib.component_alib_id"
   "   and ch.component_alib_id=plugin_alib.component_alib_id"
   "   and cpa.cfw_id=gm.cfw_id"
   "   and pa.argument is not null"

   "   and cpa.org_context = parent_component_alib_id"
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
    (get-plugin-agent-asb-component-arg-sql assembly_id cfw_group_id threads))))


(get-plugin-orgtype-asb-component-arg-sql assembly_id cfw_group_id (use-threads threads))

(define (get-plugin-orgtype-asb-component-arg-sql assembly_id cfw_group_id threads)
  (string-append 
   "select distinct " (sqlQuote assembly_id) " as ASSEMBLY_ID,"
   "ch.component_alib_id as COMPONENT_ALIB_ID,"
   "pa.argument as ARGUMENT,"
   "pa.argument_order as ARGUMENT_ORDER"

   " from"
   "   " asb-prefix "asb_component_hierarchy ch,"
   "   " asb-prefix "alib_component plugin_alib,"
   "   " asb-prefix "alib_component org_alib,"
   "   " cfw-prefix "cfw_context_plugin_arg cpa,"
   "   " cfw-prefix "cfw_org_orgtype ot,"
   "   " cfw-prefix "lib_plugin_arg pa,"
   "   " cfw-prefix "lib_plugin_arg_thread pat,"
   "   " cfw-prefix "cfw_group_member gm"

   "   where"
   "   gm.cfw_group_id=" (sqlQuote  cfw_group_id)
   "   and ch.parent_component_alib_id=org_alib.component_alib_id"
   "   and ch.component_alib_id=plugin_alib.component_alib_id"
   "   and cpa.cfw_id=gm.cfw_id"
   "   and ot.cfw_id=gm.cfw_id"
   "   and ot.org_id=org_alib.component_lib_id"
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
   ")"
   )
  )


(define (add-plugin-orgtype-asb-component-arg assembly_id cfw_group_id threads)
  (dbu
   (string-append 
    "insert into " asb-prefix "asb_component_arg "
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
   "   " asb-prefix "alib_component org_alib,"
   "   " cfw-prefix "cfw_context_plugin_arg cpa,"
   "   " cfw-prefix "lib_plugin_arg pa,"
   "   " cfw-prefix "lib_plugin_arg_thread pat,"
   "   " cfw-prefix "cfw_group_member gm"

   "   where"
   "   gm.cfw_group_id=" (sqlQuote  cfw_group_id)
   "   and ch.parent_component_alib_id=org_alib.component_alib_id"
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
    (get-plugin-all-asb-component-arg-sql assembly_id cfw_group_id threads))))


;; OPLAN

(define (get-asb-oplan-agent-attr-sql assembly_id cfw_group_id threads oplan_ids)
  (string-append 
   "select distinct " (sqlQuote assembly_id) " as ASSEMBLY_ID,"
   "ooa.oplan_id as OPLAN_ID,"
   "org_alib.component_alib_id as COMPONENT_ALIB_ID,"
   "org_alib.component_alib_id as COMPONENT_ID,"
   "ooa.start_cday as START_CDAY,"
   "ooa.attribute_name as ATTRIBUTE_NAME,"
   "ooa.end_cday as END_CDAY,"
   "ooa.attribute_value as ATTRIBUTE_VALUE"

   "   from"
   "   " asb-prefix "asb_component_hierarchy ch,"
   "   " asb-prefix "alib_component org_alib,"
   "   " cfw-prefix "cfw_group_member gm,"
   "   " cfw-prefix "cfw_oplan_og_attr ooa,"
   "   " cfw-prefix "cfw_org_group_org_member ogom"

   "   where"
   "   gm.cfw_group_id=" (sqlQuote  cfw_group_id)
   "   and ooa.cfw_id=gm.cfw_id"
   "   and ogom.cfw_id=gm.cfw_id"
   "   and ch.parent_component_alib_id=org_alib.component_alib_id"
   "   and org_alib.component_type='agent'"
   "   and ogom.org_id=org_alib.component_lib_id"
   "   and ooa.org_group_id=ogom.org_group_id"
   "   and ooa.oplan_id in "  oplan_ids

 
   "   and not exists "
   "   (select assembly_id from " asb-prefix "asb_oplan_agent_attr ar"
   "     where ar.assembly_id="(sqlQuote assembly_id)
   "     and ar.oplan_id=ooa.oplan_id"
   "     and ar.component_alib_id=org_alib.component_alib_id"
   "     and ar.component_id=org_alib.component_alib_id"
   "     and ar.start_cday=ooa.start_cday"
   "     and ar.attribute_name=ooa.attribute_name)"
   )
  )

(define (add-asb-oplans assembly_id cfw_group_id threads oplan_ids)
  (dbu
   (string-append
    "insert into " asb-prefix "asb_oplan"
    " select " (sqlQuote assembly_id) " as ASSEMBLY_ID,"
    "op.oplan_id as OPLAN_ID,"
    "op.operation_name as OPERATION_NAME,"
    "op.priority as PRIORITY,"
    "op.c0_date as C0_DATE"
    " from " cfw_prefix "cfw_oplan op,"
    "   " cfw-prefix "cfw_group_member gm"
    "   where gm.cfw_id=op.cfw_id"
    "   and op.oplan_id in " oplan_ids
    "   and not exists "
    "   (select oplan_id from " asb-prefix "asb_oplan"
    "     where assembly_id="(sqlQuote assembly_id)
    "      and oplan_id in "  oplan_ids)))


(define (add-asb-oplan-agent-attr assembly_id cfw_group_id threads oplan_ids)
  (add-asb-oplans  assembly_id cfw_group_id threads oplan_ids)
  (dbu
   (string-append 
    "insert into " asb-prefix "asb_oplan_agent_attr "
    (get-asb-oplan-agent-attr-sql assembly_id cfw_group_id threads))))











;; may not be needed
(define (get-plugin-lib-component-sql assembly_id cfw_group_id threads)
  (string-append 
   "select distinct pl.plugin_class AS component_lib_id,"
   "'plugin' AS COMPONENT_TYPE,"
   " pl.plugin_class AS component_class"
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
   "   and go.org_id || '|' ||  pl.plugin_class not in (select component_alib_id from " asb-prefix "alib_component)"
   "   and pth.plugin_class=pl.plugin_class"
   "   and pth.thread_id in " threads
   )
  )
;; may not be needed
(define (add-new-plugin-lib-component assembly_id cfw_group_id threads)
  (dbu
   (string-append 
    "insert into " asb-prefix "lib_component "
    (get-plugin-lib-component-sql assembly_id cfw_group_id threads))))


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


 ;; temporary hack for testing
(set! cfw_group_id "3ID-CFW-GRP-A")
(set! assembly_id "ASB1")
(set! assembly_description "test assembly 1")
(set! threads all-threads)
(set! version "-1")

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
  (db-update
   (getrefDBConnection)
   query))

(define (missing-asb-component-hierarchy-plugin-sql assembly_id cfw_group_id)
  (string-append 
   "select distinct " (sqlQuote  assembly_id) "as ASSEMBLY_ID,"
   "go.org_id || '|' ||  pl.plugin_class AS COMPONENT_ALIB_ID,"
   "go.org_id as PARENT_COMPONENT_ALIB_ID,"
   "(pl.plugin_class_order+(5* pg.plugin_group_order)) as INSERTION_ORDER"

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

   "   and pg.plugin_group_id = pl.plugin_group_id"
   "   and pg.plugin_group_id = opg.plugin_group_id"
   "   and (go.org_id || '|' ||  pl.plugin_class) not in (select component_alib_id from " asb-prefix "alib_component)"
   "   and ('plugin'  || '|' || pl.plugin_class) in (select component_lib_id from " asb-prefix "lib_component)"
   ;;"   order by go.org_id,(pl.plugin_class_order+(5 * pg.plugin_group_order))"
   )
  )

