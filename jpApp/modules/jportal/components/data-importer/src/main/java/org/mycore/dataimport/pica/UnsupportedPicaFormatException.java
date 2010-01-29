package org.mycore.dataimport.pica;

import org.mycore.common.MCRException;

public class UnsupportedPicaFormatException extends MCRException {

    private static final long serialVersionUID = 1L;

    public UnsupportedPicaFormatException(String fileName) {
        super(fileName + " uses no valid pica formt (ZiNG or srw)!");
    }
}
