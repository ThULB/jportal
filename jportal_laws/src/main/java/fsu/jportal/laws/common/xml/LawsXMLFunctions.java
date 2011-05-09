package fsu.jportal.laws.common.xml;

import org.apache.log4j.Logger;
import org.mycore.datamodel.ifs.MCRDirectory;
import org.mycore.datamodel.ifs.MCRFilesystemNode;

public abstract class LawsXMLFunctions {

    private static final Logger LOGGER = Logger.getLogger(LawsXMLFunctions.class);

    public static String getImageByLaw(String numberOfLaw, String derivateId) {
        // check null and empty
        if(numberOfLaw == null || numberOfLaw.equals("")) {
            LOGGER.warn("Lawnumber is null or empty");
            return "";
        }
        if(derivateId == null || derivateId.equals("")) {
            LOGGER.warn("Derivate id is null or empty");
            return "";
        }
        // get law number as integer
        int number;
        try {
            number = Integer.parseInt(numberOfLaw);
        } catch(NumberFormatException nfe) {
            LOGGER.warn("while parsing law number " + numberOfLaw, nfe);
            return "";
        }
        // get files
        MCRDirectory dir = MCRDirectory.getRootDirectory(derivateId);
        if(dir == null) {
            LOGGER.warn("Unable to get diretory of derivate " + derivateId);
            return "";
        }
        return getImageByNumber(dir, number);
    }

    /**
     * Internal method to get a image by number. Number is always the first
     * part of the image name e.g. <b>004</b>_HZA_1821_T_001.tif.
     * 
     * @param parent parent directory
     * @param number number to find
     * @return name of the image
     */
    private static String getImageByNumber(MCRDirectory parent, int number) {
        MCRFilesystemNode[] children = parent.getChildren();
        for(MCRFilesystemNode node : children) {
            if (node instanceof MCRDirectory) {
                String fileName = getImageByNumber((MCRDirectory)node, number);
                if(fileName != null)
                    return fileName;
            } else {
                try {
                    String fileName = node.getName();
                    String numberPart = fileName.split("_")[3];
                    numberPart = numberPart.substring(3);
                    int compareNumber = Integer.parseInt(numberPart);
                    if(number == compareNumber)
                        return fileName;
                } catch(Exception exc) {
                    continue;
                }
            }
        }
        return null;
    }

}
