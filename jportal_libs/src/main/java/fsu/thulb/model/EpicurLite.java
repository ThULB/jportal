/*
 * This file is part of ***  M y C o R e  ***
 * See http://www.mycore.de/ for details.
 *
 * MyCoRe is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * MyCoRe is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with MyCoRe.  If not, see <http://www.gnu.org/licenses/>.
 */

package fsu.thulb.model;

import java.io.OutputStream;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.http.auth.UsernamePasswordCredentials;

/**
 * Created by chi on 25.01.17.
 *
 * @author shermann
 * @author Huu Chi Vu
 */
@XmlRootElement(namespace = "http://nbn-resolving.org/epicurlite", name = "epicurlite")
public final class EpicurLite {
    private static JAXBContext jaxbContext;

    static {
        try {
            jaxbContext = JAXBContext.newInstance(EpicurLite.class);
        } catch (JAXBException e) {
            e.printStackTrace();
        }
    }

    @XmlAttribute(namespace = "http://www.w3.org/2001/XMLSchema-instance")
    private String schemaLocation = "http://nbn-resolving.org/epicurlite " +
            "http://nbn-resolving.org/schemas/epicurlite/1.0/epicurlite.xsd";

    @XmlElement
    private String login;
    @XmlElement
    private String password;
    @XmlElementWrapper(name = "identifier")
    @XmlElement(name = "value")
    private List<String> identifiers = new ArrayList<>();
    @XmlElement
    private Resource resource = new Resource();

    private EpicurLite() {
    }

    public static EpicurLite instance(UsernamePasswordCredentials credentials, String urn, URL url) {
        EpicurLite epicurLite = new EpicurLite();
        epicurLite.login = credentials.getUserName();
        epicurLite.password = credentials.getPassword();
        epicurLite.identifiers.add(urn);
        epicurLite.resource.url = url.toString();

        return epicurLite;
    }

    public String getURN(){
        return identifiers.get(0);
    }

    public String getURL(){
        return resource.url;
    }

    public EpicurLite setPrimary(boolean value) {
        this.resource.primary = value;
        return this;
    }

    public EpicurLite setFrontpage(boolean value) {
        this.resource.frontpage = value;
        return this;
    }

    public Marshaller getMarshaller() throws JAXBException {
        Marshaller marshaller = jaxbContext.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

        return marshaller;
    }

    public void marshall(Writer writer) throws JAXBException {
        getMarshaller().marshal(this, writer);
    }

    public void marshall(OutputStream os) throws JAXBException {
        getMarshaller().marshal(this, os);
    }

    public String asString() throws JAXBException {
        StringWriter stringWriter = new StringWriter();
        marshall(stringWriter);
        return stringWriter.toString();
    }

    private static class Resource {
        @XmlElement
        String url;
        @XmlElement
        boolean primary = true;
        @XmlElement
        boolean frontpage = false;
    }
}
