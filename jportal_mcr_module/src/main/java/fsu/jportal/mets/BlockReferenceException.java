package fsu.jportal.mets;

import java.util.ArrayList;
import java.util.List;

/**
 * Contains a list of logical div identifiers which couldn't be referenced to
 * an alto block.
 * 
 * @author Matthias Eichner
 */
public class BlockReferenceException extends ConvertException {

    private static final long serialVersionUID = 1L;

    private List<String> divIds;

    public BlockReferenceException(String message) {
        super(message);
        this.divIds = new ArrayList<>();
    }

    public void addDiv(String logicalDivId) {
        this.divIds.add(logicalDivId);
    }

    public List<String> getDivIds() {
        return divIds;
    }

}
