package fsu.jportal.frontend.toc;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import fsu.jportal.backend.JPContainer;
import fsu.jportal.backend.JPObjectType;
import fsu.jportal.util.JPComponentUtil;
import org.jdom2.Element;
import org.mycore.common.MCRException;
import org.mycore.datamodel.metadata.MCRObjectID;

/**
 * Table of content result POJO.
 *
 * @author Matthias Eichner
 */
public class JPTocResults {

    /**
     * Start hit.
     */
    private int start;

    /**
     * Amount of entries per page.
     */
    private int hitsPerPage;

    /**
     * Total amount of hits.
     */
    private int total;

    /**
     * Type of the result, e.g. jparticle or jpvolume.
     */
    private JPObjectType type;

    /**
     * List of the results
     */
    private List<JPTocResult> hits;

    public JPTocResults(MCRObjectID parentId, JPObjectType type, int start, int hitsPerPage) {
        this.type = type;
        this.start = start;
        this.hitsPerPage = hitsPerPage;
        this.total = 0;
        this.hits = new ArrayList<>();

        Optional<JPContainer> containerOptional = JPComponentUtil.getContainer(parentId);
        if (containerOptional.isPresent()) {
            JPContainer container = containerOptional.get();
            List<MCRObjectID> children = container.getChildren(type);
            this.total = children.size();
            List<MCRObjectID> resultIds = children.subList(start, Math.min(start + hitsPerPage, this.total));
            resultIds.stream().map(id -> {
                if (JPObjectType.jparticle.equals(type)) {
                    return new JPTocArticle(id);
                } else if (JPObjectType.jpvolume.equals(type)) {
                    return new JPTocVolume(id);
                } else {
                    throw new MCRException(
                            "Unable to add object '" + id.toString() + "' to 'table of content' due invalid type");
                }
            }).forEach(this.hits::add);
        }
    }

    public Element toXML() {
        Element resultsElement = new Element("results");
        resultsElement.setAttribute("type", String.valueOf(type));
        resultsElement.setAttribute("total", String.valueOf(total));
        resultsElement.setAttribute("start", String.valueOf(start));
        resultsElement.setAttribute("hitsPerPage", String.valueOf(hitsPerPage));
        resultsElement.setAttribute("columns", String.valueOf(getColumnCount()));
        for (JPTocResult result : this.hits) {
            resultsElement.addContent(result.toXML());
        }
        return resultsElement;
    }

    /**
     * Calculates how many columns should be displayed for this results. Its depending on the total amount of hits to
     * display and on the title length.
     *
     * @return amount of columns
     */
    public int getColumnCount() {
        int size = this.hits.size();
        int maxCharacters = this.hits.stream()
                                     .map(JPTocResult::getTitle)
                                     .max(Comparator.comparingInt(String::length))
                                     .map(String::length)
                                     .orElse(0);
        if (size > 31 && maxCharacters < 35) {
            return 4;
        } else if (size > 21 && maxCharacters < 50) {
            return 3;
        } else if (size > 11) {
            return 2;
        }
        return 1;
    }

}
