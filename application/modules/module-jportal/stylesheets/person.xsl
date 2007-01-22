<?xml version="1.0" encoding="UTF-8"?>
<navigation dir="/content" template="template_pamietnik" hrefStartingPage="/content/below/index.xml"
	mainTitle="MyCoReSample">
	<!-- main menu  -->
	<navi-main dir="/main/">
		<label xml:lang="de">Hauptmenü links</label>
		<label xml:lang="en">Main menu left</label>
		<item href="/content/main/search.xml" type="intern" target="_self" style="normal" replaceMenu="false"
			constrainPopUp="false">
			<label xml:lang="de">Suche</label>
			<label xml:lang="en">Retrieval</label>
			
			<item href="/editor_form_search-jpvolume.xml?XSL.editor.source.new=true" type="extern" target="_self"
				style="normal" replaceMenu="false" constrainPopUp="false">
				<label xml:lang="de">nach Bänden</label>
			</item>
			<item href="/editor_form_search-jparticle.xml?XSL.editor.source.new=true" type="extern" target="_self"
				style="normal" replaceMenu="false" constrainPopUp="false">
				<label xml:lang="de">nach Artikeln</label>
			</item>
			<item href="/editor_form_search-jpinst.xml?XSL.editor.source.new=true" type="extern" target="_self"
				style="normal" replaceMenu="false" constrainPopUp="false">
				<label xml:lang="de">nach Institutionen</label>
			</item>
			<item href="/editor_form_search-jpperson.xml?XSL.editor.source.new=true" type="extern" target="_self"
				style="normal" replaceMenu="false" constrainPopUp="false">
				<label xml:lang="de">nach Personen</label>
			</item>
			<!--			<item
			href="/editor_form_search-simpledocument.xml?XSL.editor.source.new=true"
			type="extern" target="_self" style="normal" replaceMenu="false">
			<label xml:lang="de">nach Dokumenten einfach</label>
			<label xml:lang="en">for documents simple</label>
			</item>
			<item
			href="/editor_form_search-complexdocument.xml?XSL.editor.source.new=true"
			type="extern" target="_self" style="normal" replaceMenu="false">
			<label xml:lang="de">nach Dokumenten erweitert</label>
			<label xml:lang="en">for documents extended</label>
			</item>
			<item
			href="/editor_form_search-expertdocument.xml?XSL.editor.source.new=true"
			type="extern" target="_self" style="normal" replaceMenu="false">
			<label xml:lang="de">nach Dokumenten komplex</label>
			<label xml:lang="en">for documents complex</label>
			</item>
			<item
			href="/editor_form_search-person.xml?XSL.editor.source.new=true"
			type="extern" target="_self" style="normal" replaceMenu="false">
			<label xml:lang="de">nach Personen</label>
			<label xml:lang="en">for a person</label>
			</item>
			<item href="/indexpage?searchclass=creators" type="extern"
			target="_self" style="normal" replaceMenu="false">
			<label xml:lang="de">Index Personen</label>
			<label xml:lang="en">index of persons</label>
			</item>
			
			<item
			href="/editor_form_search-institution.xml?XSL.editor.source.new=true"
			type="extern" target="_self" style="normal" replaceMenu="false">
			<label xml:lang="de">nach Institutionen</label>
			<label xml:lang="en">for institutions</label>
			</item>
			-->
			<item href="/editor_form_search-expert.xml?XSL.editor.source.new=true" type="extern" target="_self"
				style="normal" replaceMenu="false">
				<label xml:lang="de">Expertensuche</label>
				<label xml:lang="en">expert search</label>
			</item>
			
		</item>
		
		<item href="/receive/jportal_jpjournal_00000001?XSL.toc.pos.SESSION=0&amp;XSL.view.objectmetadata.SESSION=true"
			type="extern" target="_self" style="normal" replaceMenu="false" constrainPopUp="false">
			<label xml:lang="de">Blättern</label>
		</item>
		<item href="/content/main/jpEditors.xml" type="intern" target="_self" style="normal" replaceMenu="false"
			constrainPopUp="false">
			<label xml:lang="de">Aufnahmemasken</label>
			
			<item href="/servlets/MCRStartEditorServlet?type=person&amp;step=author&amp;todo=wnewobj" type="extern"
				target="_self" style="normal" replaceMenu="false">
				<label xml:lang="de">neue Person</label>
				<label xml:lang="en">new person</label>
				<label xml:lang="ru">ноый автор</label>
				<label xml:lang="pl">nowa osoba</label>
			</item>
			<item href="/servlets/MCRStartEditorServlet?type=jpinst&amp;step=author&amp;todo=wnewobj" type="extern"
				target="_self" style="normal" replaceMenu="false">
				<label xml:lang="de">neue Institution</label>
				<label xml:lang="en">new institution</label>
				<label xml:lang="ru">ноый институт</label>
				<label xml:lang="pl">nowa instytucja</label>
			</item>
			
		</item>
<!--		<item href="/content/main/authorsArea.xml" type="intern" target="_self" style="normal" replaceMenu="false">
			<label xml:lang="de">Dokumentenverwaltung</label>
			<item href="/servlets/MCRStartEditorServlet?type=author&amp;step=author&amp;todo=wnewobj" type="extern"
				target="_self" style="normal" replaceMenu="false">
				<label xml:lang="de">Person anlegen</label>
			</item>
			<item href="/servlets/MCRStartEditorServlet?type=institution&amp;step=author&amp;todo=wnewobj" type="extern"
				target="_self" style="normal" replaceMenu="false">
				<label xml:lang="de">Institution anlegen</label>
			</item>
			<item href="/servlets/MCRStartEditorServlet?type=document&amp;step=author&amp;todo=wnewobj" type="extern"
				target="_self" style="bold" replaceMenu="false">
				<label xml:lang="de">Dokument anlegen</label>
			</item>
			<item href="/editor_author_editor.xml" type="extern" target="_self" style="normal" replaceMenu="false">
				<label xml:lang="de">Workflow Person</label>
			</item>
			<item href="/editor_institution_editor.xml" type="extern" target="_self" style="normal" replaceMenu="false">
				<label xml:lang="de">Workflow Institution</label>
			</item>
			<item href="/editor_document_editor.xml" type="extern" target="_self" style="normal" replaceMenu="false">
				<label xml:lang="de">Workflow Dokument</label>
				<label xml:lang="en">Edit document</label>
			</item>
			<item href="/browse?mode=edit" type="extern" target="_self" style="normal" replaceMenu="false">
				<label xml:lang="de">Klassifikationseditor</label>
				<label xml:lang="en">Class Editor</label>
			</item>
			<label xml:lang="en">Authors section</label>
		</item>-->
		<item href="/content/main/useradmin.xml" type="intern" target="_self" style="normal" replaceMenu="false">
			<label xml:lang="de">Benutzerverwaltung</label>
			<label xml:lang="en">User administration</label>
			<item href="/servlets/MCRUserServlet?url=/content/below/index.xml&amp;mode=CreatePwdDialog" type="extern"
				target="_self" style="normal" replaceMenu="false">
				<label xml:lang="de">Passwort ändern</label>
				<label xml:lang="en">Change password</label>
			</item>
			<item href="/servlets/MCRUserServlet?url=/content/below/index.xml&amp;mode=ShowUser" type="extern"
				target="_self" style="normal" replaceMenu="false">
				<label xml:lang="de">Benutzerdaten anzeigen</label>
				<label xml:lang="en">Show user data</label>
			</item>
			<item href="/servlets/MCRLoginServlet?url=/content/below/index.xml&amp;uid=gast&amp;pwd=gast" type="intern"
				target="_self" style="normal" replaceMenu="false">
				<label xml:lang="de">Benutzer Gast</label>
				<label xml:lang="en">Guest user</label>
			</item>
			<item href="/servlets/MCRUserAdminServlet?mode=newuser" type="extern" target="_self" style="normal"
				replaceMenu="false">
				<label xml:lang="de">Neuen Account anlegen</label>
				<label xml:lang="en">Create a new user</label>
			</item>
			<item href="/servlets/MCRUserAdminServlet?mode=newgroup" type="extern" target="_self" style="normal"
				replaceMenu="false">
				<label xml:lang="de">Neue Gruppe anlegen</label>
				<label xml:lang="en">Create a new group</label>
			</item>
		</item>
		<item href="/content/main/docu.xml" type="intern" target="_self" style="normal" replaceMenu="false">
			<label xml:lang="de">Anleitungen und mehr</label>
			<item href="/content/main/docu/general.xml" type="intern" target="_self" style="normal" replaceMenu="false">
				<label xml:lang="de">Allgemeines</label>
			</item>
			<item href="/Overview.pdf" type="extern" target="_self" style="normal" replaceMenu="false">
				<label xml:lang="de">MyCoRe Overview</label>
				<label xml:lang="en">MyCoRe Overview</label>
			</item>
			<item href="/UserGuide.pdf" type="extern" target="_self" style="normal" replaceMenu="false">
				<label xml:lang="de">MyCoRe User Guide</label>
				<label xml:lang="en">MyCoRe User Guide</label>
			</item>
			<item href="/ProgrammerGuide.pdf" type="extern" target="_self" style="normal" replaceMenu="false">
				<label xml:lang="de">MyCoRe Programmer Guide</label>
				<label xml:lang="en">MyCoRe Programmer Guide</label>
			</item>
			<item href="/QuickInstallationGuide.pdf" type="extern" target="_self" style="normal" replaceMenu="false">
				<label xml:lang="de">DocPortal Quick Installation Guide</label>
				<label xml:lang="en">DocPortal Quick Installation Guide</label>
			</item>
			<item href="/DocPortal.pdf" type="extern" target="_self" style="normal" replaceMenu="false">
				<label xml:lang="de">DocPortal Dokumentation</label>
				<label xml:lang="en">DocPortal Documentation</label>
			</item>
			<item href="http://www.mycore.de/" type="extern" target="_blank" style="normal" replaceMenu="false">
				<label xml:lang="de">Mycore.de</label>
			</item>
			<label xml:lang="en">Documentation</label>
		</item>
		<item href="/content/main/contact.xml" type="intern" target="_self" style="normal" replaceMenu="false">
			<label xml:lang="de">Kontakt</label>
		</item>
	</navi-main>
	<!-- main menu  -->
	<!-- navi bar below -->
	<navi-below dir="/below/">
		<label xml:lang="de">Menü oben</label>
		<label xml:lang="en">Menu above</label>
		<item href="/content/below/index.xml" type="intern" target="_self" style="normal" replaceMenu="false"
			constrainPopUp="false">
			<label xml:lang="de">Pamietnik Literacki </label>
			<label xml:lang="en">Home</label>
		</item>
		<item href="/content/below/sitemap.xml" type="extern" target="_self" style="normal">
			<label xml:lang="de">Sitemap</label>
			<label xml:lang="en">Site map</label>
		</item>
		<item href="/servlets/WCMSLoginServlet" type="extern" target="_self" style="normal" template="template_wcms">
			<label xml:lang="de">WCMS</label>
			<label xml:lang="en">WCMS</label>
			<dynamicContentBinding>
				<rootTag>cms</rootTag>
			</dynamicContentBinding>
		</item>
		<item href="/servlets/MCRLoginServlet" type="extern" target="_self" style="bold" replaceMenu="false">
			<label xml:lang="de">Nutzer wechseln</label>
			<label xml:lang="en">Log in</label>
		</item>
	</navi-below>
	<!-- END OF: navi bar below -->
</navigation>