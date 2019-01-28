### TOC
1. [REQUIREMENTS](#requirements)
2. [RUN JPORTAL](#run-jportal)
3. [DEPLOY JPORTAL](#deploy-jportal)
4. [CONFIGURE JPORTAL](#configure-jportal)
5. [RIGHTS MANAGEMENT](#rights-management)


### REQUIREMENTS
* JAVA 11 JDK
* GRADLE


### RUN JPORTAL

Go to the checked out jportal project.

    cd /jportal2

We use gradle as our build system. If you checked out jportal the first time run the following command. This will build and start solr (search engine), h2 (database) and the web application.

    ./gradlew build runSystem runApp

After the command is processed you find the application [here](http://localhost:8291/jportal "jportal") and solr [here](http://localhost:8391/solr "solr").

To log in use **administrator** as login name and **alleswirdgut** as password.

To stop the application just press ENTER in the command line. Be aware that the database and the solr
server are still running. To stop those enter

    ./gradlew stopSystem

Usually its is not necessary to start and stop solr and the database while developing. You can just rebuild the web application.

    ./gradlew clean build runApp

### DEPLOY JPORTAL
After building jportal (see [RUN JPORTAL](#run-jportal)) a *.war file will be created in *jportal2/jportal_webapp/build/libs/*. You can use this war in your preferred servlet container.


### CONFIGURE JPORTAL
On build time a new configuration folder will be created for jportal. You can find it under *~/.mycore/jportal/*

#### home directory
To specify where jportal is installed, you can start your servlet container with **-DMCR.Home=/path to the mycore home directory**.

#### solr
If you want to use your own solr you can simply change the url. Open the mycore.properties file and change **MCR.Solr.ServerURL** to your preferred location.

#### database
To change your database go to *~/.mycore/jportal/resources/META-INF/* and edit the persistence.xml


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

The groups using a hierarchy system. A user in a higher group has
also the rights of all the lower groups.

    admin > supervisor > journalgroup > editorsgroup
