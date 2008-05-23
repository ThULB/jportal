package org.mycore.services.z3950;

/**
 * Dieses Java-Interface beschreibt die Syntax der grundlegenen Funktionen
 * f�r den Z39.50-Suchservice. 
 * @author Andreas de Azevedo
 * @version 1.0
 */
public interface MCRZ3950Query {
	
	/**
	 * Schneidet das Ergebnisdokument an einer bestimmten Stelle ab.
	 * @param maxresults Die Anzahl noch zu verbleibender Ergebnisse.
	 */
	public void cutDownTo(int maxresults);
	
	public void sort();
	
	/**
	 * Gibt alle Ergebnisse als Bytestrom zur�ck.
	 * @return Das Ergebnisdokument als Byte-Array, null falls es keine Ergebnisse gab.
	 */
	public byte[] getDocumentAsByteArray();
	
	/**
	 * F�hrt eine Suchanfrage in MyCoRe aus.
	 * @return True falls es Ergebnisse gab, sonst False.
	 */
	public boolean search();
	
	/**
	 * Gibt die Anzahl der Ergebnisse zur�ck.
	 * @return Die Anzahl der Dokumente in der Ergebnisliste.
	 */
	public int getSize();
	
	public int getIndex();
	
	/**
	 * Verk�rzt das Ergebnisdokument auf das Dokument mit einem bestimmten
	 * Index.
	 * @param index Der Index des gew�nschten Ergebnisses.
	 */
	public void setIndex(int index);
	
	public String getQuery();
	
	/**
	 * Setzt eine Z39.50-Suchanfrage
	 * @param query Eine Suchanfrage im Z39.50-Format (Prefix)
	 */
	public void setQuery(String query);

}
