(jde-project-file-version "1.0")
(jde-set-variables
 ;; Set here default parameter for make program
 '(jde-make-args "jar")
 ;; What should be put as java file header
 '(jde-gen-buffer-boilerplate
   (quote
    ("/*"
     " * couchdb-java-viewserver - Java-based view server for CouchDB"
     " * http://cloudant.com"
     " * (C) 2010, Cloudant"
     " *"
     " */")))
 ;; Sometimes JDEE prints useful messages, but if everything works well
 ;; you will be not using this.
 '(jde-log-max 5000)
 ;; Must be on to improve your coding: you write: "if " and JDEE generates
 ;; templeate code for "if" statement.
 '(jde-enable-abbrev-mode t)

 '(jde-compile-option-hide-classpath nil)

 ;; Path to source files for automatic loading
 '(jde-sourcepath
   (quote
    ("/Users/brad/dev/java/couchdb-view-server/src/")))
 ;; Classpath for browsing files and generates code templates
 '(jde-global-classpath
   (quote
    ("./build"
     "./src"
     "./lib"
     "./")))
 ;; output (dest) directory for compiilation
 '(jde-compile-option-directory
   (quote
    "./bin"))
 ;; If you want to run Java apps from within emacs for example for debuging
 ;; set default startup class for your project.
 '(jde-run-application-class "com.cloudant.couchdbjavaserver.RunServer")
 '(jde-run-working-directory "")
 ;; Set name for your make program: ant or maybe maven?
 ;'(jde-make-program "ant")
 ;; For javadoc templates version tag can be customized
 '(jde-javadoc-version-tag-template "\"* @version $Id: prj.el,v 1.4 2003/04/23 14:28:25 kobit Exp $\"")
 ;; Defines bracket placement style - now it is set according to SUN standards
 '(jde-gen-k&r t)
 ;; Do you prefer to have java.io.* imports or separate import for each
 ;; used class - now it is set for importing classes separately
 '(jde-import-auto-collapse-imports nil)
 ;; You can define many JDKs and choose one for each project
 ;'(jde-compile-option-target (quote ("1.6")))
 ;; Nice feature sorting imports.
 '(jde-import-auto-sort t)
 ;; For syntax highlighting and basic syntax checking parse buffer
 ;; number of seconds from the time you changed the buffer.
 '(jde-auto-parse-buffer-interval 600)
 ;; You can set different user name and e-mail address for each project
 '(user-mail-address "brad@cloudant.com")
)