(import "java.sql.DriverManager")
(load "elf/jdbc.scm")
(load "elf/basic.scm")
(load "build/compile.scm")
(import "java.util.Date")

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
    (if (null? s) '()
	(if (eqv? (car s) #\') (cons #\' (cons #\' (quoteDouble (cdr s))))
	    (cons (car s) (quoteDouble (cdr s))))))
  (string-append
   "'" (list->string (quoteDouble (string->list s))) "'"))

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
   "|| org_name ,null,org_name, org.org_id,'AGENT',0 from v1_cfw_group_org go, v1_lib_organization org  where cfw_group_id=" 
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




(define (get-asb-org-data-sql assembly_id cfw_group_id)
   (string-append 
    "select " (sqlQuote  assembly_id) "as ASSEMBLY_ID,"
    (sqlQuote  assembly_id)
    "|| '|' ||  org.org_id AS COMPONENT_ID,"
    "org.org_id as COMPONENT_NAME,"
    "null as PARENT_COMPONENT_ID,"
    "org.org_id as COMPONENT_LIB_ID,"
    "'agent' as COMONENT_CATEGOR,"
    "1 as INSERTION_ORDER "
    " from v1_cfw_group_org go, v1_lib_organization org  where cfw_group_id=" 
    (sqlQuote  cfw_group_id)
    "               and org.org_id=go.org_id"
    " order by org.org_id"
    )
)

(define (view_plugin_components assembly_id cfw_group_id)
  (view-query  
   (getrefDBConnection)
   (get-asb-plugin-data-sql assembly_id cfw_group_id)
   ))


;;select '3ID-135-CMT'as ASSEMBLY_ID,'3ID-135-CMT'|| '|' ||  pl.plugin_class AS COMPONENT_ID,
;;pl.plugin_class as COMPONENT_NAME,
;;null as PARENT_COMPONENT_ID,
;;org.org_id as COMPONENT_LIB_ID,
;;'plugin' as COMONENT_CATEGOR,
;;1 as INSERTION_ORDER  from v1_cfw_group_org go,
;; v1_lib_organization org,
;; v1_cfw_org_orgtype ot,
;; v1_cfw_orgtype_plugin_grp pg,
;; v1_cfw_plugin_group_member pl
;;   where cfw_group_id='3ID-CFW-GRP-A'
;;   and org.org_id=go.org_id
;;    and org.org_id=ot.org_id
;;    and ot.orgtype_id=pg.orgtype_id
;;    and pg.plugin_group_id = pl.plugin_group_id order by org.org_id;
;;
;;
(define (get-asb-plugin-data-sql assembly_id cfw_group_id)
  (string-append 
   "select " (sqlQuote  assembly_id) "as ASSEMBLY_ID,"
   (sqlQuote  assembly_id)
   "|| '|' ||  pl.plugin_class AS COMPONENT_ID,"
   "pl.plugin_class as COMPONENT_NAME,"
   "null as PARENT_COMPONENT_ID,"
   "org.org_id as COMPONENT_LIB_ID,"
   "'plugin' as COMONENT_CATEGOR,"
   "1 as INSERTION_ORDER "
   " from v1_cfw_group_org go, v1_lib_organization org, v1_cfw_org_orgtype ot, v1_cfw_orgtype_plugin_grp pg, v1_cfw_plugin_group_member pl"
   "   where cfw_group_id=" 
   (sqlQuote  cfw_group_id)
   "    and org.org_id=go.org_id"
   "    and org.org_id=ot.org_id"
   "    and ot.orgtype_id=pg.orgtype_id" 
   "    and pg.plugin_group_id = pl.plugin_group_id"
   " order by org.org_id"
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
    

(define (insert_org_components assembly_id cfw_group_id)
  (string-append 
   "insert into v3_asb_component"
   "("
   (get-asbcomponent-data-sql assembly_id cfw_group_id)
   ")"
))

(define (vq query)
  (view-query    
   (getrefDBConnection)
   query))

(define (cq query)
  (collect-query    
   (getrefDBConnection)
   query))

