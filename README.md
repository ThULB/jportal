### TOC
1. [REQUIREMENTS](#requirements)
2. [GETTING SOURCES](#getting-sources)
3. [RUN JPORTAL](#run-jportal)
4. [DEPLOY JPORTAL](#deploy-jportal)
5. [CONFIGURE JPORTAL](#configure-jportal)
6. [RIGHTS MANAGEMENT](#rights-management)


### REQUIREMENTS
* Subversion-Client
* JAVA 8 JDK
* GRADLE


### GETTING SOURCES

    svn checkout https://svn.thulb.uni-jena.de/repos/jportal2/trunk jportal2

If you want to checkout the mycore system. (not required to run jportal)

    svn checkout https://server.mycore.de/svn/mycore/trunk mycore


### RUN JPORTAL

Go to the checked out jportal project.

    cd /jportal2

We use gradle as our build system. If you checked out jportal the
first time run the following command. This will build 
and start solr (search engine), h2 (database) and the web application.

    ./gradlew build runApp

After the command is processed you find the application [here](http://localhost:8291/jportal "jportal")
and solr [here](http://localhost:8391/solr "solr").

To log in use **administrator** as login name and **alleswirdgut** as password.

To stop the whole thing do:

    ./gradlew stopApp

To rebuild it do:

    ./gradlew stopApp clean build runApp

For developing it is usually not necessary to start/stop solr and the
database. Its faster to just restart the jetty.

    ./gradlew stopJetty clean build runJetty


### DEPLOY JPORTAL
After building jportal (see [RUN JPORTAL](#run-jportal)) a *.war file will be created
in *jportal2/jportal_webapp/build/libs/*. You can use this war in your
preferred servlet container.


### CONFIGURE JPORTAL
On build time a new configuration folder will be created for jportal.
You can find it under *~/.mycore/jportal/*

#### home directory
To specify where jportal is installed, you can start your servlet container
with **-DMCR.Home=/path to the mycore home directory**.

#### solr
If you want to use your own solr you can simply change the url.
Open the mycore.properties file and change **MCR.Module-solr.ServerURL** to
your preferred location.

#### database
To change your database go to *~/.mycore/jportal/resources/META-INF/* and
edit the persistence.xml


### RIGHTS MANAGEMENT

Following groups are created by default.

+ editorsgroup
  * jpvolume (create, delete)
  * jparticle (create, delete)
  * person (create, delete)
  * jpinst (create, delete)
  * derivate (create)
+ journalgroup
  * jpjournal (create, delete)
+ derivategroup
  * derivate (delete)
+ supervisor
  * classifications (create, delete)
  * administrate users (create, delete)
+ admin
  * is allowed to do everything

The groups using a hierarchy system. If a user is in a higher group he has
also the rights of the lower group. 

    admin > supervisor > journalgroup > editorsgroup
