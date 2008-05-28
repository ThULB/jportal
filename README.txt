1. GETTING SOURCES
======================================
======================================

cvs -d :pserver:anoncvs@ulbaix03.thulb.uni-jena.de:/content/cvsroot co jportal


2. PREPERATION OF SYSTEM
======================================
======================================

- according to a common "DocPortal" installation, prepare the system for MyCoRE
-- e.g. setting $MYCORE_HOME, $DOCPORTAL_HOME


3. CONFIGURATION AND INSTALLATION
======================================
======================================

3.1 Installation of a completely new application:
=================================================
   1. Mycore:
         1. cp $MYCORE_HOME/config/build.properties.template $MYCORE_HOME/config/build.properties
         2. ant jar
   2. Application
		 1. cp -r $MYCORE_HOME/stylesheets/* $DOCPORTAL_HOME/build/stylesheets
         2. cp $DOCPORTAL_HOME/config/mycore.properties.private.template $DOCPORTAL_HOME/config/mycore.properties.private
         3. vi $DOCPORTAL_HOME/config/mycore.properties.private
         4. vi $DOCPORTAL_HOME/config/mycore.properties.jp
         5. ant -f jportal-build.xml jp.create.schema jar jp.create.scripts
         6. $DOCPORTAL_HOME/build/bin/hsqldbstart.sh
         7. ant -f jportal-build.xml create.metastore jp.create.usermanagjp. create.default-rules jp.create.class create.genkeys webapps
         8. $DOCPORTAL_HOME/build/bin/jettystart.sh 
         
3.2 Installation of a once already installed application:
=========================================================
    1. Mycore
         1. ant clean jar
    2. Application
		 1. cp -r $MYCORE_HOME/stylesheets/* $DOCPORTAL_HOME/build/stylesheets
         2. ant -f jportal-build.xml jp.create.schema jar jp.create.scripts create.genkeys webapps


4. HACKS IN DOCPORTAL *
======================================
======================================         
* these are files, that have been modified, replaced or what ever OUTSIDE $DOCPORTAL_HOME/modules/module-jportal/

Nutzerverwaltung:

    * config/user/permission.xml gehackt um eigene Objekttypen --> Modularisieren

Properties:

    * mycore.properties.jp in mycore.properties eingefügt
    * mycore.properties.jp in $DOCPORTAL_HOME/config eingefügt
    * mocules/module-iview/config/mycore.properties.iview in $DOCPORTAL_HOME/config eingefügt

XSL:

    * generatePage --> xsl:include "jp_extensions.xsl"
    * objecttypes.xsl --> xsL:include "jp_objecttypes.xsl"
    * mycoreobject.xsl

JAVA:

    * MCRObject sollte (MYCORE_HEAD) mit nächstem Snapshot mit rein kommen 
    * BUGFIX MCRParentRuleStrategy sollte (MYCORE_HEAD) mit nächstem Snapshot mit rein kommen
    * session problem:
          o MCRStartEditorServlet (MYCORE_HEAD) 
          o MCRUriResolver (MYCORE_HEAD) 
          o MCREditorSourcecReader (MYCORE_HEAD) 
          o MCRServlet (MYCORE_HEAD) 

    * MCRLoginServlet (MYCORE_HEAD) 
    
    
5. CLASSIFICATIONS
======================================
====================================== 

ID  					Macht was?  							Bemerkung
-----------------------+---------------------------------------+------------------------------------
jportal_class_00000001 	Länderliste 							wird nirgends benutzt
jportal_class_00000002 	Sprachliste für die Metadatenfelder 	alle nicht genutzten ausgeblendet
jportal_class_00000003 	DDC 									wird nirgends benutzt
jportal_class_00000004 	Sprache des Journals 	
jportal_class_00000006 	Typen für "Weitere Titel"
jportal_class_00000007 	Rollen für "Beteiligte" 	
urmel_class_001 		Geschlecht von Personen 				wird mit Archiv gemeinsam genutzt
jportal_class_00000008 	Datums-Typen bei Artikeln 	
jportal_class_00000009 	Datums-Typen bei Journalen 	
jportal_class_00000010 	ID-Typen bei Journalen und Artikeln 	
jportal_class_00000011  Classipub  								optionale Klassifikation für Artikel
																optionale Klassifikation für Artikel per Standard diese nehmen
																ist leer
jportal_class_00000012 	Classipub 2								optionale 2. Klassifikation für Artikel
																optionale Klassifikation für Artikel per Standard diese nehmen
																ist leer
	
jportal_class_00000013 	Classipub 3								optionale 3. Klassifikation für Artikel
																optionale Klassifikation für Artikel per Standard diese nehmen
																ist leer
         
         
6. Rechteverwaltung
======================================
======================================          

Folgende Gruppen werden standardmäßig eingerichtet :

"journalgroup"

   1.* darf JPJournal
                o anlegen
                o editieren
                o Derivat anhängen

"volumegroup"

   1. * darf JPVolume
         1. anlegen
         2. editieren
         3. löschen
         4. Derivat anhängen


"editorsgroup"

   1.
          * darf JPArticle
          *
               1. anlegen
               2. editieren
               3. löschen
               4. Derivat anhängen
          * darf Person
          *
               1. anlegen
               2. editieren
               3. löschen
          * darf JPInst
         1. anlegen
         2. editieren
         3. löschen


"rootgroup"

    * darf alles inklusive komplettem Usermanagement

         
         
7. Neue ZS anlegen
======================================
======================================

   1.  Webseite anlegen
         1. template zuweisen
         1. addresse der root webseite merken
   3. jpjournal über web anlegen
         1. gemerkten web context eintragen
         2. jpjournal id eintragen
   5. suchmasken mit eigenem dummy parameter eintragen
   6. Schreibrechte vergeben
         1. spez. volume-goup_abc und editorsgroup_abc über web anlegen
         1. nutzer darin aufnehmen und immer zusätzl. in volumegroup oder editorsgroup aufnehmen
         1. build/bin/mycore.sh update permission writedb for id jportal_jpjournal_0000000x with rulefile* 'volumegoup_abc & editorsgroup_abc' 
         1. build/bin/mycore.sh update permission deletedb for id jportal_jpjournal_0000000x with rulefile* 'volumegoup_abc & editorsgroup_abc' 
   8. webseite anlegen

   * sieht bspw. so aus: 
   <?xml version="1.0" encoding="utf-8"?>
	<condition format="xml" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:noNamespaceSchemaLocation="MCRCondition.xsd">
	  <boolean operator="or">
	    <condition value="editorsgroup_abc" operator="=" field="group" />	  
	    <condition value="volumegoup_abc" operator="=" field="group" />	  	    
	  </boolean>
	</condition>
   