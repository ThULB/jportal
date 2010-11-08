package fsu.jportal.servlets;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.mycore.datamodel.ifs2.MCRFileCollection;
import org.mycore.datamodel.ifs2.MCRFileStore;
import org.mycore.frontend.servlets.MCRServlet;
import org.mycore.frontend.servlets.MCRServletJob;

public class JornalListServlet extends MCRServlet {
	@Override
	protected void doGetPost(MCRServletJob job) throws Exception {
		HttpServletRequest request = job.getRequest();
		HttpServletResponse response = job.getResponse();
		
		MCRFileStore jpdataStore = MCRFileStore.getStore("JPDATA");
		MCRFileCollection jpdataFileCollection = jpdataStore.create();
		
//		jpdataFileCollection.
	}
}
