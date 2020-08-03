package fsu.jportal.backend;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URLConnection;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.SecureDirectoryStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.core.Response;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlTransient;

import org.apache.logging.log4j.LogManager;
import org.mycore.datamodel.metadata.MCRMetadataManager;
import org.mycore.datamodel.metadata.MCRObjectID;
import org.mycore.datamodel.niofs.MCRFileAttributes;
import org.mycore.datamodel.niofs.MCRMD5AttributeView;
import org.mycore.datamodel.niofs.MCRPath;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.Ordering;

import fsu.jportal.urn.URNTools;

public class DerivateDirectoryTools {


    //temporary solution from MyCore Rest API v2, for better performance, will be replaced with COMA Module
    public static Response serveDirectory(MCRPath mcrPath, MCRFileAttributes dirAttrs) {
        Directory dir = new Directory(mcrPath, dirAttrs);
        try (DirectoryStream ds = Files.newDirectoryStream(mcrPath)) {
            String derivateID = mcrPath.getOwner();
            MCRObjectID derivateObID  = MCRObjectID.getInstance(derivateID);
            String urn = MCRMetadataManager.retrieveMCRDerivate(derivateObID).getDerivate().getURN();
            //A SecureDirectoryStream may get attributes faster than reading attributes for every path instance
            Function<MCRPath, MCRFileAttributes> attrResolver = p -> {
                try {
                    return (ds instanceof SecureDirectoryStream)
                        ? ((SecureDirectoryStream<MCRPath>) ds).getFileAttributeView(MCRPath.toMCRPath(p.getFileName()),
                        MCRMD5AttributeView.class).readAllAttributes() //usually faster
                        : Files.readAttributes(p, MCRFileAttributes.class);
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            };
            List<DirectoryEntry> entries = StreamSupport
                .stream(((DirectoryStream<MCRPath>) ds).spliterator(), false)
                .collect(Collectors.toMap(p -> p, attrResolver))
                .entrySet()
                .stream()
                .map(e -> e.getValue().isDirectory() ? new Directory(e.getKey(), e.getValue())
                    : new File(e.getKey(), e.getValue(), URLConnection
                    .guessContentTypeFromName(e.getKey().getFileName().toString()), urn != null))
                .sorted() //directories first, than sort for filename
                .collect(Collectors.toList());

            JPDerivateComponent derivateComponent = new JPDerivateComponent(derivateObID);
            String maindoc = DerivateTools.getMaindoc(derivateComponent);
            dir.setMaindocName(maindoc);
            dir.setEntries(entries);
            dir.setHasURN(urn != null);
            dir.setURN(urn);
            dir.setDisplay(DerivateTools.isDisplayEnabled(derivateID));
            dir.setUrnEnabled(DerivateTools.urnEnabled());
        } catch (IOException | UncheckedIOException e) {
            throw new InternalServerErrorException(e);
        }
        return Response.ok(dir).lastModified(new Date(dirAttrs.lastModifiedTime().toMillis())).build();
    }

    private static class Directory extends DirectoryEntry {
        private List<DirectoryEntry> children;

        private String maindocName;

        private boolean hasURN;

        private boolean display;

        private boolean urnEnabled;

        private String urn;

        private String parentName;

        private Date parentLastMod;

        Directory() {
            super();
        }

        void setEntries(List<? extends DirectoryEntry> entries) {
            LogManager.getLogger().info(entries);
            children = new ArrayList<>();
            entries.stream()
                .collect(Collectors.groupingBy(Object::getClass))
                .forEach((c, e) -> {
                    if (File.class.isAssignableFrom(c)) {
                        children.addAll((List<File>) e);
                    } else if (Directory.class.isAssignableFrom(c)) {
                        children.addAll((List<Directory>) e);
                    }

                });
        }

        void setMaindocName (String maindocName) {
            this.maindocName = maindocName;
        }

        void setHasURN (boolean hasURN) {
            this.hasURN = hasURN;
        }

        void setDisplay (boolean display) {
            this.display = display;
        }

        void setUrnEnabled (boolean urnEnabled) {
            this.urnEnabled = urnEnabled;
        }

        void setURN (String urn) {
            this.urn = urn;
        }

        Directory(MCRPath p, MCRFileAttributes attr) {
            super(p, attr);
            this.parentName = p.getOwner();
            try {
                MCRFileAttributes ownerAttributes = Files.readAttributes(p.getRoot(), MCRFileAttributes.class);
                this.parentLastMod = Date.from(ownerAttributes.lastModifiedTime().toInstant());
            } catch (IOException e) {
                this.parentLastMod = Date.from(attr.lastModifiedTime().toInstant());
            }
        }

        @XmlElement(name = "children")
        @JsonProperty("children")
        @JsonInclude(content = JsonInclude.Include.NON_EMPTY)
        public List<DirectoryEntry> getChildren() {
            return children;
        }

        @XmlAttribute
        @JsonProperty(index = 1)
        public String getMaindocName() {
            return maindocName;
        }

        @XmlAttribute
        @JsonProperty(index = 2)
        public boolean getHasURN() {
            return hasURN;
        }

        @XmlAttribute
        @JsonProperty(index = 3)
        public String getUrn() {
            return urn;
        }

        @XmlAttribute
        @JsonProperty(index = 4)
        public boolean getDisplay() {
            return display;
        }

        @XmlAttribute
        @JsonProperty(index = 5)
        public boolean getUrnEnabled() {
            return urnEnabled;
        }

        @XmlAttribute
        @JsonProperty(index = 6)
        public String getParentName() {
            return parentName;
        }

        @XmlAttribute
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd.MM.yyyy HH:mm:ss", locale = "de_DE")
        @JsonProperty(index = 7)
        public Date getParentLastMod() {
            return parentLastMod;
        }

    }

    private static class File extends DirectoryEntry {

        private String mimeType;

        private String md5;

        private String contentType;

        private String urn;

        File() {
            super();
        }

        File(MCRPath p, MCRFileAttributes attr, String mimeType, boolean hasURN) {
            super(p, attr);
            this.md5 = attr.md5sum();
            this.mimeType = mimeType;
            this.contentType = mimeType.substring(mimeType.indexOf("/") + 1);
            if (hasURN) {
                this.urn = URNTools.getURNForFile(p.getOwner(), p.getOwnerRelativePath());
            }
        }

        @XmlAttribute
        @JsonProperty(index = 1)
        public String getMd5() {
            return md5;
        }

        @XmlAttribute
        @JsonProperty(index = 2)
        public String getMimeType() {
            return mimeType;
        }

        @XmlAttribute
        @JsonProperty(index = 3)
        public String getContentType() {
            return contentType;
        }

        @XmlAttribute
        @JsonProperty(index = 4)
        public String getUrn() {
            return urn;
        }
    }

    @XmlTransient
    @XmlSeeAlso({File.class, Directory.class})
    private abstract static class DirectoryEntry implements Comparable<DirectoryEntry> {
        private String name;

        private Date lastmodified;

        private String type;

        private long size;

        private String absPath;

        DirectoryEntry(MCRPath p, MCRFileAttributes attr) {
            this.name = Optional.ofNullable(p.getFileName())
                .map(java.nio.file.Path::toString)
                .orElse(p.getOwner());
            this.lastmodified = Date.from(attr.lastModifiedTime().toInstant());
            this.type = (attr.isDirectory()) ? "directory" : "file";
            this.size = attr.size();
            this.absPath = p.getOwnerRelativePath();
        }

        DirectoryEntry() {
        }

        @XmlAttribute
        @JsonProperty(index = 0)
        @JsonInclude(content = JsonInclude.Include.NON_EMPTY)
        String getName() {
            return name;
        }

        @XmlAttribute
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd.MM.yyyy HH:mm:ss", locale = "de_DE")
        @JsonProperty(index = 2)
        public Date getLastmodified() {
            return lastmodified;
        }

        @XmlAttribute
        @JsonProperty(index = 3)
        @JsonInclude(content = JsonInclude.Include.NON_EMPTY)
        String getType() {
            return type;
        }

        @XmlAttribute
        @JsonProperty(index = 1)
        public long getSize() {
            return size;
        }

        @XmlAttribute
        @JsonProperty(index = 4)
        @JsonInclude(content = JsonInclude.Include.NON_EMPTY)
        String getAbsPath() {
            return absPath;
        }

        @Override
        public int compareTo(DirectoryEntry o) {
            return Ordering
                .<DirectoryEntry> from((de1, de2) -> {
                    if (de1 instanceof Directory && !(de2 instanceof Directory)) {
                        return -1;
                    }
                    if (de1.getClass().equals(de2.getClass())) {
                        return 0;
                    }
                    return 1;
                })
                .compound((de1, de2) -> de1.getName().compareTo(de2.getName()))
                .compare(this, o);
        }
    }
}
