TOC
===========================================================================================================================
1. LICENSE
2. REQUIREMENTS
3. GETTING SOURCES
4. PREPERATION 
5. CONFIGURATION
6. INSTALLATION
    6.1 NEW INSTALLATION
    6.2 REINSTALLATION
7. RUNNING    
8. DEFAULT USERS
9. RIGHTS MANAGEMENT
10. CREATE NEW JOURNAL
===========================================================================================================================

1. LICENSE
======================================
======================================
Watch and agree license agreement specified in LICENSE.txt


2. REQUIREMENTS
======================================
======================================
- Subversion-Client
- JAVA 6 JDK
- ANT

3. GETTING SOURCES
======================================
======================================
svn checkout http://svn.thulb.uni-jena.de/repos/jportal2/trunk jportal

4. PREPERATION 
======================================
======================================
- Setting system environment variables
-- $MYCORE_HOME to <Installation-Directory>/jportal/mycore
-- $DOCPORTAL_HOME to <Installation-Directory>/jportal/application

- Create JPortal properties 
// -- cp $MYCORE_HOME/config/build.properties.template $MYCORE_HOME/config/build.properties
-- cp $DOCPORTAL_HOME/config/mycore.properties.private.template $DOCPORTAL_HOME/config/mycore.properties.private
-- cp $DOCPORTAL_HOME/config/hibernate/hibernate.cfg.xml.template $DOCPORTAL_HOME/config/hibernate/hibernate.cfg.xml


5. CONFIGURATION
======================================
======================================
- Customize jportal system 
-- vi $DOCPORTAL_HOME/config/mycore.properties.private
--- set $MCR.basedir to your <$DOCPORTAL_HOME>
--- set $MCR.FileUpload.IP to your local running server's IP address
--- add module "JPortal" to system, by appending on property "MCR.Modules.Application" the value "jportal"
--- exclude module "DocPortal" from system, by appending on property "MCR.Components.Exclude" the value "docportal"
// --- remove default module "docportal" and "iview" from system, by removing value "docportal" resp. "iview" from property "MCR.Modules.Application" 


6. INSTALLATION
======================================
======================================
6.1 NEW INSTALLATION
==================================================
   1. Mycore:
         1. cd $MYCORE_HOME
         2. ant jar
   2. Application
         1. cd $DOCPORTAL_HOME
         3. ant -f jportal-build.xml jp.create.schema jar create.scripts
         4. $DOCPORTAL_HOME/build/bin/hsqldbstart.sh
         5. ant -f jportal-build.xml jp.create.metastore jp.create.usermanag jp.create.default-rules jp.create.class create.genkeys webapps
         6. install Image-Viewer (watch $DOCPORTAL_HOME/modules/UNINSTALLED_module-iview/INSTALL.txt) 
         
6.2 REINSTALLATION - already installed application 
==================================================
cd $MYCORE_HOME; ant clean jar; cd $DOCPORTAL_HOME; ant -f jportal-build.xml jar create.scripts webapps 


7. RUNNING
======================================
======================================          
Once you have followed all steps from chapter 6 you can run the server and watch JPortal in action
All you have to do is 
- make sure RDBMS is running ($DOCPORTAL_HOME/build/bin/hsqldbstart.sh)
- $DOCPORTAL_HOME/build/bin/jettystart.sh
- Go to web browser and visit http://localhost:8291


8. DEFAULT USERS
======================================
======================================
By default the installation creates a super user called "administrator" with password "alleswirdgut", that is member of group "rootgroup". Watch chapter "RIGHTS MANAGEMENT" to
see what this user is allowed to do. 

If you want to use WCMS (Web Content Management System) of JPortal:
- go to your web browser and click menu point "Tools" > "WCMS" 
- log in with login name "admin" and password "wcms" 

         
9. RIGHTS MANAGEMENT
======================================
======================================          
Following groups will created by default:

    1 ."journalgroup"
    - Allowed to ... JPJournal 
    -- create
    -- edit
    -- append derivate on
    
    2. "volumegroup"
    - Allowed to ... JPVolume
    -- create
    -- edit
    -- append derivate on
    
    3. "editorsgroup"
    - Allowed to ... JPArticle
    -- create
    -- edit
    -- delete
    -- append derivate on 
    
    - Allowed to ... Person
    -- create
    -- edit
    -- delete
    
    - Allowed to ... JPInst
    -- create
    -- edit
    -- delete

    4. "admingroup"
    - Allowed to do all actions on JPJournal, JPVolumes, JPArticles, Persons, JPinst
    - add, edit, delete users and groups
    - 

         
10. CREATE NEW JOURNAL
======================================
======================================
   1. Go to JPortal web application in your browser
   2. Log in as "administrator"
   3. Click on "Editors" in menu left
   4. Create a new journal
   5. Create a new Journal-Context, by 
      - go to created journals metadata page
      - click on "Ja, Zeitschriften-Kontext jetzt einrichten!"
      - follow form and submit