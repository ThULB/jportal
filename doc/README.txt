TOC
======================================
1. LICENSE
2. REQUIREMENTS
3. GETTING SOURCES
4. RUN JPORTAL
5. DEPLOY JPORTAL
6. CONFIGURE JPORTAL
7. RIGHTS MANAGEMENT
======================================


1. LICENSE
======================================
======================================
Watch and agree license agreement specified in LICENSE.txt


2. REQUIREMENTS
======================================
======================================
- Subversion-Client
- JAVA 8 JDK
- GRADLE


3. GETTING SOURCES
======================================
======================================
svn checkout https://svn.thulb.uni-jena.de/repos/jportal2/trunk jportal2

# optional: if you want to checkout the mycore system
svn checkout https://server.mycore.de/svn/mycore/trunk mycore


4. RUN JPORTAL
======================================
======================================

# Go to the checked out jportal2 project.
cd /jportal2

# We use gradle as our build system. If you checked out jportal2 the
# first time run the following command. This will build 
# and start solr (search engine), h2 (database) and the web application.
./gradlew build runApp

# After the command is processed you find the application here
# application: http://localhost:8291/jportal
# solr: http://localhost:8391/solr
#
# To log in use 'administrator' and 'alleswirdgut'.

# To stop the whole thing do:
./gradlew stopApp

# To rebuild the whole thing do:
./gradlew stopApp clean build runApp

# For developing it is usually not necessary to start/stop solr and the
# database. Its easier to just restart the jetty.
./gradlew stopJetty clean build runJetty


5. DEPLOY JPORTAL
======================================
======================================
After building jportal2 (see 4. RUN JPORTAL) a *.war file will be created
in 'jportal2/jportal_webapp/build/libs/'. You can use this war in your
preferred servlet container.


6. CONFIGURE JPORTAL
======================================
======================================
On build time a new configuration folder will be created for jportal2.
You can find it under '~/.mycore/jportal/'

To specify where jportal is installed, you can start your servlet container
with '-DMCR.Home=/path to the mycore home directory'.

If you want to use your own solr you can simply change the url.
Open the mycore.properties file and change 'MCR.Module-solr.ServerURL' to
your preferred location.

To change your database go to '~/.mycore/jportal/resources/META-INF/' and
edit the persistence.xml


7. RIGHTS MANAGEMENT
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
