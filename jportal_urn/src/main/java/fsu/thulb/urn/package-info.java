@XmlSchema(
        namespace = "http://nbn-resolving.org/epicurlite",
        elementFormDefault = XmlNsForm.QUALIFIED,
        xmlns={
                @XmlNs(prefix="epicurlite", namespaceURI="http://nbn-resolving.org/epicurlite"),
                @XmlNs(prefix="xsi", namespaceURI="http://www.w3.org/2001/XMLSchema-instance")
        })

package fsu.thulb.urn;

import javax.xml.bind.annotation.XmlNs;
import javax.xml.bind.annotation.XmlNsForm;
import javax.xml.bind.annotation.XmlSchema;


