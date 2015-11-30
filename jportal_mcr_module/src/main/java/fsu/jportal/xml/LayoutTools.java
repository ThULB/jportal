package fsu.jportal.xml;

import java.util.List;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.output.DOMOutputter;
import org.mycore.common.MCRSessionMgr;
import org.mycore.common.MCRUserInformation;
import org.w3c.dom.Node;

import fsu.jportal.util.JPComponentUtil;
import fsu.jportal.util.JPComponentUtil.JPInfoProvider;
import fsu.jportal.util.JPComponentUtil.JPObjectInfo;

public abstract class LayoutTools {

    private static class DerivateDisplay implements JPObjectInfo<Boolean> {
        @Override
        public Boolean getInfo(List<Object> node) {
            if (node.size() == 0) {
                return false;
            } else {
                return true;
            }
        }
    }

    private static class DatesInfo implements JPObjectInfo<Node> {
        @Override
        public Node getInfo(List<Object> nodes) {
            Element root = new Element("datesInfo");
            Document datesDoc = new Document(root);

            for (Object node : nodes) {
                node.getClass().getCanonicalName();
                if (node instanceof Element) {
                    root.addContent(((Element) node).detach());
                }
            }

            DOMOutputter domOutputter = new DOMOutputter();
            try {
                return domOutputter.output(datesDoc).getFirstChild();
            } catch (JDOMException e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    public static String getNameOfTemplate(String journalID) {
        return JPComponentUtil.getNameOfTemplate(journalID).orElse("");
    }

    public static String getJournalID(String mcrID) {
        return JPComponentUtil.getJournalID(mcrID).orElse("");
    }

    public static String getMaintitle(String mcrID) {
        return JPComponentUtil.getMaintitle(mcrID).orElse("");
    }

    public static String getListType(String mcrID) {
        return JPComponentUtil.getListType(mcrID).orElse("");
    }

    public static String getDerivateDisplay(String derivateID) {
        JPInfoProvider infoProvider = new JPInfoProvider(derivateID,
            "/mycorederivate/derivate[not(@display) or @display!='false']");
        return infoProvider.get(new DerivateDisplay()).toString();
    }

    public static Node getDatesInfo(String journalID) {
        JPInfoProvider infoProvider = new JPInfoProvider(journalID, "/mycoreobject/metadata/dates",
            "/mycoreobject/metadata/hidden_genhiddenfields1");
        return infoProvider.get(new DatesInfo());
    }

    public static String getUserName() {
        MCRUserInformation userInformation = MCRSessionMgr.getCurrentSession().getUserInformation();
        String realname = userInformation.getUserAttribute(MCRUserInformation.ATT_REAL_NAME);
        if (realname != null && !"".equals(realname.trim())) {
            return realname;
        } else {
            return userInformation.getUserID();
        }
    }
}
