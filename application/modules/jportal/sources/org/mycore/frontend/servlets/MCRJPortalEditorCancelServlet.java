package org.mycore.frontend.servlets;

import java.net.URLDecoder;

public class MCRJPortalEditorCancelServlet extends MCRServlet {

    private static final long serialVersionUID = 1L;

    @Override
    protected void doGetPost(MCRServletJob job) throws Exception {
        String referer = job.getRequest().getHeader("Referer");
        
        String url = null;
        String returnUrl = getValueOfReferer(referer, "returnUrl");
        String cancelUrl = getValueOfReferer(referer, "cancelUrl");
        // first try -> go to cancel url
        if(cancelUrl != null) {
            url = URLDecoder.decode(cancelUrl, "UTF-8");
        } else if(returnUrl != null) {
            // next try -> go to return url
            url = URLDecoder.decode(returnUrl, "UTF-8");
        } else {
            // finally try -> go to edited object
            String mcrId = getValueOfReferer(referer, "mcrid");
            url = getBaseURL() + "receive/" + mcrId;
            if(mcrId == null) {
                // nothing defined -> go to main page
                url = getBaseURL();
            }
        }
        job.getResponse().sendRedirect(job.getResponse().encodeRedirectURL(url));
    }

    protected String getValueOfReferer(String referer, String id) {
        String value = null;
        int index = referer.indexOf(id);
        if (index != -1) {
            int startIndex = index + id.length() + 1;
            int endIndex = referer.indexOf("&", index + 1);
            if(endIndex == -1)
                endIndex = referer.length();
            value = referer.substring(startIndex, endIndex);
        }
        return value;
    }
}