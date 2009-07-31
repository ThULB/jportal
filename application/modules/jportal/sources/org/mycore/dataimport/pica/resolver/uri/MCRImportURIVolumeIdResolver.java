package org.mycore.dataimport.pica.resolver.uri;

import org.mycore.importer.mapping.resolver.uri.MCRImportURIResolver;

public class MCRImportURIVolumeIdResolver implements MCRImportURIResolver {
    
    private static int idCounter = 0;
    
    public String resolve(String uri, String oldValue) {
        return oldValue + idCounter++;
    }

}