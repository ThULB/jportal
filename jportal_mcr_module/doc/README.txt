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
7. SOLR IN TOMCAT
8. DEFAULT USERS
9. RIGHTS MANAGEMENT
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
  - MCR.Module-solr.ServerURL=http\://localhost:8296/jportal

optional develop properties
  - MCR.LayoutService.LastModifiedCheckPeriod = 1000

Add in pom.xml below the <dependencies> element
  <dependency>
    <groupId>fsu.thulb</groupId>
    <artifactId>jportal_mcr_module</artifactId>
    <version>2.0.18-SNAPSHOT</version>
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
ant clean clean.data; rm -rf save; ant resolve create.jar create.scripts
build/bin/solrstart.sh &
build/bin/hsqldbstart.sh &
ant create.users create.default-rules create.class create.webapp

5.2 REINSTALLATION - already installed application 
==================================================
cd $JPORTAL_HOME
mvn clean install
cd $DOCPORTAL_HOME
ant resolve create.jar create.webapp

6. SOLR IN TOMCAT
======================================
======================================
Its required that JPortal is deployed in docportal. Go to $DOCPORTAL_HOME/config/solr-home and
check if the solr.xml contains a core named 'jportal'. If not, you have to install jportal as
described above.
cd $DOCPORTAL_HOME/config
mvn clean install -f solr-pom.xml

A new target directory is created in the config folder which contains a solr-*.war. Copy this
war to your webapps folder in tomcat and rename it to solr.war.

Now we need to create a solr-home directory where the configuration of solr is set. It should be
outside of tomcat, maybe the home directory. Copy all content of $DOCPORTAL_HOME/config/solr-home
to that directory. Then create a new file 'setenv.sh' in your tomcat/bin directory and paste the
following code (set correct path to solr-home directory):

#!/bin/sh
export CATALINA_OPTS="-Xms2G -Xmx2G -Dsolr.solr.home={PATH TO solr-home directory}"

Start tomcat and look if solr is running at localhost:8080/solr


7. RUNNING
======================================
======================================
Once you have followed all steps from chapter 5 you can run the server and watch JPortal in action
All you have to do is 
- make sure RDBMS is running ($DOCPORTAL_HOME/build/bin/hsqldbstart.sh)
- $DOCPORTAL_HOME/build/bin/jettystart.sh
- Go to web browser and visit http://localhost:8291


8. DEFAULT USERS
======================================
======================================
By default the installation creates a super user called "administrator" with password "alleswirdgut", that is member of group "rootgroup". Watch chapter "RIGHTS MANAGEMENT" to
see what this user is allowed to do. 


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

    4. "admin"
    - Allowed to do all actions on JPJournal, JPVolumes, JPArticles, Persons, JPinst
    - add, edit, delete users and groups
    - 
