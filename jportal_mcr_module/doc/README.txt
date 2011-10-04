TOC
===========================================================================================================================
1. LICENSE
2. REQUIREMENTS
3. GETTING SOURCES
4. CONFIGURATION
5. INSTALLATION
    5.1 NEW INSTALLATION
    5.2 REINSTALLATION
6. RUNNING    
7. DEFAULT USERS
8. RIGHTS MANAGEMENT
9. CREATE NEW JOURNAL
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
- MAVEN

3. GETTING SOURCES
======================================
======================================
svn checkout https://server.mycore.de/svn/docportal/trunk docportal
svn checkout https://server.mycore.de/svn/mycore/trunk mycore
svn checkout http://svn.thulb.uni-jena.de/repos/jportal2/trunk jportal

4. CONFIGURATION  
======================================
======================================
Setting system environment variables (this is optional)
  - $JPORTAL_HOME to <Installation-Directory>/jportal
  - $DOCPORTAL_HOME to <Installation-Directory>/docportal

Copy templates
  - cp $DOCPORTAL_HOME/config/mycore.private.properties.template $DOCPORTAL_HOME/config/mycore.private.properties
  - cp $DOCPORTAL_HOME/config/pom.xml.template $DOCPORTAL_HOME/config/pom.xml

Setting properties in mycore.private.properties
  - MCR.basedir=<$DOCPORTAL_HOME>
  - MCR.Modules.Application=common,maven
  - MCR.Components.Exclude=migration20-21,iview

Add in pom.xml below the <dependencies> element
  <dependency>
    <groupId>fsu.thulb</groupId>
    <artifactId>jportal_mcr_module</artifactId>
    <version>2.0.13</version>
    <type>jar</type>
    <scope>compile</scope>
  </dependency>
  
For DB2 users, the driver and licence jar have to be installed, eG.
  mvn install:install-file -Dfile=$PATH_TO_LIB/db2jcc.jar -DgroupId=com.ibm.db2 
  	-DartifactId=db2jcc -Dversion=9.1 -Dpackaging=jar
  
  mvn install:install-file -Dfile=$PATH_TO_LIB/db2jcc_license_cu.jar 
  	-DgroupId=com.ibm.db2 -DartifactId=db2jcc_license_cu -Dversion=9.1 -Dpackaging=jar
  	
  and add following into pom.xml
  <dependency>
  	<groupId>com.ibm.db2</groupId>
    <artifactId>db2jcc</artifactId>
   	<version>9.1</version>
  </dependency>
  <dependency>
  	<groupId>com.ibm.db2</groupId>
    <artifactId>db2jcc_license_cu</artifactId>
    <version>9.1</version>
  </dependency>
  

5. INSTALLATION
======================================
======================================

5.1 NEW INSTALLATION
==================================================
cd $JPORTAL_HOME
mvn install
cd $DOCPORTAL_HOME 
ant clean clean.data
ant resolve create.jar create.scripts
build/bin/hsqldbstart.sh &
ant create.users create.default-rules create.class create.webapp

5.2 REINSTALLATION - already installed application 
==================================================
cd $JPORTAL_HOME
mvn clean install
cd $DOCPORTAL_HOME
ant resolve create.jar create.webapp

6. RUNNING
======================================
======================================          
Once you have followed all steps from chapter 5 you can run the server and watch JPortal in action
All you have to do is 
- make sure RDBMS is running ($DOCPORTAL_HOME/build/bin/hsqldbstart.sh)
- $DOCPORTAL_HOME/build/bin/jettystart.sh
- Go to web browser and visit http://localhost:8291


7. DEFAULT USERS
======================================
======================================
By default the installation creates a super user called "administrator" with password "alleswirdgut", that is member of group "rootgroup". Watch chapter "RIGHTS MANAGEMENT" to
see what this user is allowed to do. 


8. RIGHTS MANAGEMENT
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

         
9. CREATE NEW JOURNAL
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
