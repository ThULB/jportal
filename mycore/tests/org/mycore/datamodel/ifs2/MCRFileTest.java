/*
 * $Revision: 15006 $ 
 * $Date: 2009-03-25 10:28:39 +0100 (Mi, 25. Mär 2009) $
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

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Date;

import org.apache.commons.vfs.RandomAccessContent;
import org.apache.commons.vfs.Selectors;
import org.apache.commons.vfs.VFS;
import org.jdom.Document;
import org.jdom.Element;
import org.mycore.common.MCRSessionMgr;
import org.mycore.common.MCRTestCase;
import org.mycore.common.MCRUtils;

/**
 * JUnit test for MCRFile
 * 
 * @author Frank L�tzenkirchen
 */
public class MCRFileTest extends MCRTestCase {

    private static MCRFileStore store;

    private MCRFileCollection col;

    protected void createStore() throws Exception {
        File temp = File.createTempFile("base", "");
        String path = temp.getAbsolutePath();
        temp.delete();

        setProperty("MCR.IFS2.Store.TEST.Class", "org.mycore.datamodel.ifs2.MCRFileStore", true);
        setProperty("MCR.IFS2.Store.TEST.BaseDir", path, true);
        setProperty("MCR.IFS2.Store.TEST.SlotLayout", "4-2-2", true);
        store = MCRFileStore.getStore("TEST");
    }

    protected void setUp() throws Exception {
        super.setUp();
        if (store == null)
            createStore();
        else
            VFS.getManager().resolveFile(store.getBaseDir()).createFolder();
        col = store.create();
    }

    protected void tearDown() throws Exception {
        super.tearDown();
        VFS.getManager().resolveFile(store.getBaseDir()).delete(Selectors.SELECT_ALL);
    }

    public void testFileName() throws Exception {
        MCRFile file = col.createFile("foo.txt");
        Date created = file.getLastModified();
        assertEquals("foo.txt", file.getName());
        assertEquals("txt", file.getExtension());
        bzzz();
        file.renameTo("readme");
        assertEquals("readme", file.getName());
        assertEquals("", file.getExtension());
        assertTrue(file.getLastModified().after(created));
    }

    @SuppressWarnings("deprecation")
    public void testSetLastModified() throws Exception {
        MCRFile file = col.createFile("foo.txt");
        Date other = new Date(2009, 1, 1);
        file.setLastModified(other);
        assertEquals(other, file.getLastModified());
    }

    public void testMD5() throws Exception {
        MCRFile file = col.createFile("foo.txt");
        assertEquals(MCRFile.MD5_OF_EMPTY_FILE, file.getMD5());
        byte[] content = "Hello World".getBytes("UTF-8");
        file.setContent(MCRContent.readFrom(content));
        assertFalse(MCRFile.MD5_OF_EMPTY_FILE.equals(file.getMD5()));
        MCRFileCollection col2 = store.retrieve(col.getID());
        MCRFile child = (MCRFile) (col2.getChild("foo.txt"));
        assertEquals(file.getMD5(), child.getMD5());
    }

    public void testDelete() throws Exception {
        MCRFile file = col.createFile("foo.txt");
        file.delete();
        assertNull(col.getChild("foo.txt"));
    }

    public void testChildren() throws Exception {
        MCRFile file = col.createFile("foo.txt");
        assertNull(file.getChild("foo"));
        assertEquals(0, file.getChildren().size());
        assertFalse(file.hasChildren());
        assertEquals(0, file.getNumChildren());
    }

    public void testContentFile() throws Exception {
        MCRFile file = col.createFile("foo.txt");
        File src = File.createTempFile("foo", "txt");
        src.deleteOnExit();
        byte[] content = "Hello World".getBytes("UTF-8");
        FileOutputStream fo = new FileOutputStream(src);
        MCRUtils.copyStream(new ByteArrayInputStream(content), fo);
        fo.close();
        file.setContent(MCRContent.readFrom(src));
        assertFalse(MCRFile.MD5_OF_EMPTY_FILE.equals(file.getMD5()));
        assertEquals(11, file.getSize());
        src.delete();
        file.getContent().sendTo(src);
        assertEquals(11, src.length());
    }

    public void testContentXML() throws Exception {
        MCRFile file = col.createFile("foo.xml");
        Document xml = new Document(new Element("root"));
        file.setContent(MCRContent.readFrom(xml));
        assertFalse(MCRFile.MD5_OF_EMPTY_FILE.equals(file.getMD5()));
        Document xml2 = file.getContent().asXML();
        assertEquals("root", xml2.getRootElement().getName());
    }

    public void testRandomAccessContent() throws Exception {
        MCRFile file = col.createFile("foo.txt");
        byte[] content = "Hello World".getBytes("UTF-8");
        file.setContent(MCRContent.readFrom(content));
        RandomAccessContent rac = file.getRandomAccessContent();
        rac.skipBytes(6);
        InputStream in = rac.getInputStream();
        char c = (char) (in.read());
        assertEquals('W', c);
        in.close();
        rac.close();
    }

    public void testType() throws Exception {
        MCRFile file = col.createFile("foo.txt");
        assertTrue(file.isFile());
        assertFalse(file.isDirectory());
    }

    public void testLabels() throws Exception {
        MCRFile file = col.createFile("foo.txt");
        assertTrue(file.getLabels().isEmpty());
        assertNull(file.getCurrentLabel());
        file.setLabel("de", "deutsch");
        file.setLabel("en", "english");
        String curr = MCRSessionMgr.getCurrentSession().getCurrentLanguage();
        String label = file.getLabel(curr);
        assertEquals(label, file.getCurrentLabel());
        assertEquals(2, file.getLabels().size());
        assertEquals("english", file.getLabel("en"));
        MCRFileCollection col2 = store.retrieve(col.getID());
        MCRFile child = (MCRFile) (col2.getChild("foo.txt"));
        assertEquals(2, child.getLabels().size());
        file.clearLabels();
        assertTrue(file.getLabels().isEmpty());
    }
}
