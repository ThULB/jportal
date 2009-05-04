/*
 * $Revision: 14968 $ 
 * $Date: 2009-03-19 13:08:02 +0100 (Do, 19. Mär 2009) $
 *
 * This file is part of ***  M y C o R e  ***
 * See http://www.mycore.de/ for details.
 *
 * This program is free software; you can use it, redistribute it
 * and / or modify it under the terms of the GNU General Public License
 * (GPL) as published by the Free Software Foundation; either version 2
 * of the License or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program, in a file called gpl.txt or license.txt.
 * If not, write to the Free Software Foundation Inc.,
 * 59 Temple Place - Suite 330, Boston, MA  02111-1307 USA
 */

package org.mycore.datamodel.ifs2;

import java.io.File;
import java.util.Iterator;

import org.apache.commons.vfs.FileObject;
import org.apache.log4j.Logger;
import org.mycore.common.MCRConfiguration;
import org.mycore.common.MCRConfigurationException;
import org.mycore.common.MCRSessionMgr;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.auth.BasicAuthenticationManager;
import org.tmatesoft.svn.core.auth.SVNAuthentication;
import org.tmatesoft.svn.core.auth.SVNUserNameAuthentication;
import org.tmatesoft.svn.core.internal.io.fs.FSRepositoryFactory;
import org.tmatesoft.svn.core.io.SVNRepository;
import org.tmatesoft.svn.core.io.SVNRepositoryFactory;

/**
 * Stores metadata objects both in a local filesystem structure and in a
 * Subversion repository. Changes can be tracked and restored. To enable
 * versioning, configure the repository URL, for example
 * 
 * MCR.IFS2.Store.DocPortal_document.SVNRepositoryURL=file:///foo/svnroot/
 * 
 * @author Frank L�tzenkirchen
 */
public class MCRVersioningMetadataStore extends MCRMetadataStore {

    protected final static Logger LOGGER = Logger.getLogger(MCRVersioningMetadataStore.class);

    protected SVNURL repURL;

    static {
        FSRepositoryFactory.setup();
    }

    /**
     * Returns the store for the given metadata document type
     * 
     * @param type
     *            the type of metadata to store
     * @return the store for this metadata type
     */
    public static MCRVersioningMetadataStore getStore(String type) {
        return (MCRVersioningMetadataStore) (MCRStore.getStore(type));
    }

    protected void init(String type) {
        super.init(type);

        String repositoryURL = MCRConfiguration.instance().getString("MCR.IFS2.Store." + type + ".SVNRepositoryURL");
        try {
            LOGGER.info("Versioning metadata store " + type + " repository URL: " + repositoryURL);
            repURL = SVNURL.parseURIDecoded(repositoryURL);
            File dir = new File(repURL.getPath());
            if (!dir.exists()) {
                LOGGER.info("Repository does not exist, creating new SVN repository at " + repositoryURL);
                repURL = SVNRepositoryFactory.createLocalRepository(dir, true, false);
            }
        } catch (SVNException ex) {
            String msg = "Error initializing SVN repository at URL " + repositoryURL;
            throw new MCRConfigurationException(msg, ex);
        }
    }

    /**
     * Returns the SVN repository used to manage metadata versions in this
     * store.
     * 
     * @return the SVN repository used to manage metadata versions in this
     *         store.
     */
    SVNRepository getRepository() throws Exception {
        SVNRepository repository = SVNRepositoryFactory.create(repURL);
        String user = MCRSessionMgr.getCurrentSession().getCurrentUserID();
        SVNAuthentication[] auth = new SVNAuthentication[] { new SVNUserNameAuthentication(user, false) };
        BasicAuthenticationManager authManager = new BasicAuthenticationManager(auth);
        repository.setAuthenticationManager(authManager);
        return repository;
    }

    /**
     * Returns the URL of the SVN repository used to manage metadata versions in
     * this store.
     * 
     * @return the URL of the SVN repository used to manage metadata versions in
     *         this store.
     */
    SVNURL getRepositoryURL() throws Exception {
        return repURL;
    }

    public MCRVersionedMetadata create(MCRContent xml, int id) throws Exception {
        return (MCRVersionedMetadata) (super.create(xml, id));
    }

    public MCRVersionedMetadata create(MCRContent xml) throws Exception {
        return (MCRVersionedMetadata) (super.create(xml));
    }

    /**
     * Returns the metadata stored under the given ID, or null. Note that this
     * metadata may not exist currently in the store, it may be a deleted
     * version, which can be restored then.
     * 
     * @param id
     *            the ID of the XML document
     * @return the metadata stored under that ID, or null when there is no such
     *         metadata object
     */
    public MCRVersionedMetadata retrieve(int id) throws Exception {
        if (exists(id))
            return (MCRVersionedMetadata) (super.retrieve(id));
        else
            return new MCRVersionedMetadata(this, getSlot(id), id);
    }

    /**
     * Updates all stored metadata to the latest revision in SVN
     */
    public void updateAll() throws Exception {
        for (Iterator<Integer> ids = listIDs(true); ids.hasNext();)
            retrieve(ids.next()).update();
    }

    public void delete(int id) throws Exception {
        MCRVersionedMetadata vm = retrieve(id);
        vm.delete();
    }

    protected MCRVersionedMetadata buildMetadataObject(FileObject fo, int id) {
        return new MCRVersionedMetadata(this, fo, id);
    }
}
