package fsu.jportal.frontend.filter;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.io.IOException;
import java.util.*;

/**
 * Removes the qry parameter of a request if its empty. An empty qry parameter causes
 * solr to throw a NPE. So its better to remove the parameter and let solr use the 
 * default value for qry.
 * 
 * @author Matthias Eichner
 */
public class SolrQueryFilter implements Filter {

    @Override
    public void init(FilterConfig config) throws ServletException {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        SolrServletRequestWrapper wrapper = new SolrServletRequestWrapper((HttpServletRequest) request);
        chain.doFilter(wrapper, response);
    }

    @Override
    public void destroy() {
    }

    /**
     * Removes the query parameter if its an empty string.
     */
    private static class SolrServletRequestWrapper extends HttpServletRequestWrapper {

        final static String QRY = "qry";

        public SolrServletRequestWrapper(HttpServletRequest request) {
            super(request);
        }

        @Override
        public String[] getParameterValues(String name) {
            String[] values = super.getParameterValues(name);
            if (values != null && name.equals(QRY)) {
                List<String> valueList = new ArrayList<>();
                for (int i = 0; i < values.length; i++) {
                    if (values[i].trim().length() > 0) {
                        valueList.add(values[i]);
                    }
                }
                return valueList.isEmpty() ? null : valueList.toArray(new String[valueList.size()]);
            }
            return values;
        }

        @Override
        public String getParameter(String name) {
            String[] values = this.getParameterValues(name);
            return values == null ? null : values[0];
        }

        @Override
        @SuppressWarnings({ "unchecked", "rawtypes" })
        public Map getParameterMap() {
            Map paramMap = new HashMap(super.getParameterMap());
            if (paramMap.containsKey(QRY)) {
                String[] values = getParameterValues(QRY);
                if (values == null) {
                    paramMap.remove(QRY);
                }
            }
            return paramMap;
        }

        @Override
        @SuppressWarnings({ "unchecked", "rawtypes" })
        public Enumeration getParameterNames() {
            return new Vector(this.getParameterMap().keySet()).elements();
        }

    }
}
