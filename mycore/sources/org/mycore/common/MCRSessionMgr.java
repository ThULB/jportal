/*
 * $RCSfile: MCRConfiguration.java,v $
 * $Revision: 1.25 $ $Date: 2005/09/02 14:26:23 $
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

package org.mycore.common;

/**
 * Manages sessions for a MyCoRe system. This class is backed by a ThreadLocal
 * variable, so every Thread is guaranteed to get a unique instance of
 * MCRSession. Care must be taken when using an environment utilizing a Thread
 * pool, such as many Servlet engines. In this case it is possible for the
 * session object to stay attached to a thread where it should not be. Use the
 * {@link #releaseCurrentSession()}method to reset the session object for a
 * Thread to its default values.
 * 
 * The basic idea for the implementation of this class is taken from an apache
 * project, namely the class org.apache.common.latka.LatkaProperties.java
 * written by Morgan Delagrange. Please see <http://www.apache.org/>.
 * 
 * @author Detlev Degenhardt
 * @version $Revision: 1.8 $ $Date: 2005/08/15 15:02:25 $
 */
public class MCRSessionMgr {
    /**
     * Custom ThreadLocal class that automatically initializes the default
     * MyCoRe session object for the thread.
     */
    private static class ThreadLocalSession extends ThreadLocal {
        // The first time a ThreadLocal object is accessed on a particular
        // thread, the state for
        // that thread's copy of the local variable is set by executing the
        // method initialValue().
        public Object initialValue() {
            return new MCRSession();
        }
    }

    /**
     * This ThreadLocal is automatically instantiated per thread with a MyCoRe
     * session object containing the default session parameters which are set in
     * the constructor of MCRSession.
     */
    private static ThreadLocalSession theThreadLocalSession = new ThreadLocalSession();

    /**
     * This method returns the unique MyCoRe session object for the current
     * Thread. The session object is initialized with the default MyCoRe session
     * data.
     * 
     * @return MyCoRe MCRSession object
     */
    public static synchronized MCRSession getCurrentSession() {
        MCRSession session = (MCRSession) theThreadLocalSession.get();

        if (session == null) {
            theThreadLocalSession.set(theThreadLocalSession.initialValue());
            session = (MCRSession) theThreadLocalSession.get();
        }

        return session;
    }

    /**
     * This method sets a MyCoRe session object for the current Thread.
     */
    public static synchronized void setCurrentSession(MCRSession theSession) {
        theThreadLocalSession.set(theSession);
    }

    /**
     * Releases the MyCoRe session from its current thread. Subsequent calls of
     * getCurrentSession() will return a different MCRSession object than before
     * for the current Thread. One use for this method is to reset the session
     * inside a Thread-pooling environment like Servlet engines.
     */
    public static synchronized void releaseCurrentSession() {
        MCRSession session = (MCRSession) theThreadLocalSession.get();

        if (session != null) {
            MCRSession.logger.debug("MCRSession released " + session.getID());
            theThreadLocalSession.set(null);
        }
    }
}
