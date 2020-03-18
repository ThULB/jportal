package fsu.jportal.resources;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.attribute.FileTime;
import java.text.SimpleDateFormat;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.mycore.access.MCRAccessException;
import org.mycore.datamodel.common.MCRActiveLinkException;
import org.mycore.datamodel.niofs.MCRFileAttributes;
import org.mycore.datamodel.niofs.MCRPath;
import org.mycore.frontend.fileupload.MCRUploadHandlerIFS;
import org.mycore.frontend.fileupload.MCRUploadHelper;
import org.mycore.frontend.jersey.filter.access.MCRRestrictedAccess;

import fsu.jportal.backend.DerivateDirectoryTools;
import fsu.jportal.backend.DerivateTools;
import fsu.jportal.backend.DocumentTools;
import fsu.jportal.backend.JPUploader;
import fsu.jportal.backend.MetaDataTools;
import fsu.jportal.domain.model.Document;
import fsu.jportal.domain.model.DerivateFiles;
import fsu.jportal.domain.model.MoveDocs;
import fsu.jportal.domain.model.RenameMultiple;
import fsu.jportal.domain.model.UploadResponse;

@Path("derivatebrowser")
public class DerivateBrowserResource {

    static Logger LOGGER = LogManager.getLogger(DerivateBrowserResource.class);

    @Context
    HttpServletRequest request;

    @Context
    HttpServletResponse response;

    @Context
    ServletContext context;

    @GET
    @Path("{derivID}{path:(/.*)*}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response browsePath(@PathParam("derivID") String derivID, @PathParam("path") String path) {
        MCRPath mcrPath = MCRPath.getPath(derivID, path);
        MCRFileAttributes fileAttributes;
        try {
            fileAttributes = Files.readAttributes(mcrPath, MCRFileAttributes.class);
        } catch (IOException e) {
            throw new WebApplicationException("Unable to find " + derivID + path, e);
        }
        if (fileAttributes.isDirectory()) {
            return DerivateDirectoryTools.serveDirectory(mcrPath, fileAttributes);
        }
        return Response.serverError().build();
    }

    @GET
    @Path("start")
    @Produces(MediaType.TEXT_HTML)
    @MCRRestrictedAccess(DerivateBrowserPermission.class)
    public Response start() {
        InputStream mainGui = getClass().getResourceAsStream(
            "/META-INF/resources/modules/derivate-browser/gui/derivatebrowser.html");
        return Response.ok(mainGui).build();
    }

    @GET
    @Path("compact")
    @MCRRestrictedAccess(DerivateBrowserPermission.class)
    public byte[] startCompact() throws Exception {
        return MetaDataTools.transformMCRWebPage(request,
            "/META-INF/resources/modules/derivate-browser/gui/derivatebrowserCompact.xml");
    }

    @PUT
    @Path("docs")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @MCRRestrictedAccess(DerivateBrowserPermission.class)
    public Document[] deleteDocs(Document... docs) {
        for (int i = 0; i < docs.length; i++) {
            String objID = docs[i].getObjId();
            int respID = DocumentTools.delete(objID);
            docs[i].setStatus(respID);
        }
        return docs;
    }

    @PUT
    @Path("multiple")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @MCRRestrictedAccess(DerivateBrowserPermission.class)
    public DerivateFiles deleteFiles(DerivateFiles files) {
        DerivateFiles.File[] fileArray = files.getFiles();
        for (int i = 0; i < fileArray.length; i++) {
            String deriID = fileArray[i].getDerivId();
            String path = fileArray[i].getPath();
            int status = DerivateTools.delete(MCRPath.getPath(deriID, path));
            fileArray[i].setStatus(status);
        }
        return files;
    }

    @POST
    @Path("{derivID}{path:(/.*)*}")
    @MCRRestrictedAccess(DerivateBrowserPermission.class)
    public Response createFolder(@PathParam("derivID") String derivID, @PathParam("path") String path) {
        if (!DerivateTools.mkdir(derivID + ":" + path)) {
            throw new WebApplicationException(derivID + ":" + path, Status.CONFLICT);
        }
        return Response.ok().build();
    }

    @POST
    @Path("rename")
    @MCRRestrictedAccess(DerivateBrowserPermission.class)
    public Response rename(@QueryParam("file") String file, @QueryParam("name") String name,
        @QueryParam("mainFile") Boolean start) {
        try {
            DerivateTools.rename(file, name);
        } catch (FileAlreadyExistsException e) {
            throw new WebApplicationException(file + " does already exists", e, Status.CONFLICT);
        } catch (IOException e) {
            throw new WebApplicationException("Unable to rename file " + file + " to " + name, e);
        } catch (MCRAccessException e) {
            throw new WebApplicationException(Status.UNAUTHORIZED);
        }
        if (start) {
            String path = file.substring(0, file.lastIndexOf("/") + 1) + name;
            DerivateTools.setAsMain(file.substring(0, file.lastIndexOf(":")),
                path.substring(path.lastIndexOf(":") + 1));
        }
        return Response.ok().build();
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("moveDeriFiles")
    @MCRRestrictedAccess(DerivateBrowserPermission.class)
    public DerivateFiles moveDeriFiles(DerivateFiles files) throws Exception {
        DerivateFiles.File[] fileArray = files.getFiles();
        MCRPath targetPath = getPath(files.getTarget());
        for (int i = 0; i < fileArray.length; i++) {
            MCRPath sourcePath = getPath(fileArray[i]);
            int status = DerivateTools.mv(sourcePath, targetPath) ? 1 : 0;
            fileArray[i].setStatus(status);
        }
        return files;
    }

    private MCRPath getPath(DerivateFiles.File file) {
        return MCRPath.getPath(file.getDerivId(), file.getPath());
    }

    @PUT
    @Path("{derivID}{path:(/.*)*}/main")
    @MCRRestrictedAccess(DerivateBrowserPermission.class)
    public Response setMainDoc(@PathParam("derivID") String deriID, @PathParam("path") String path) {
        DerivateTools.setAsMain(deriID, path);
        return Response.ok().build();
    }

    @GET
    @Path("folders/{derivID}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getFolders(@PathParam("derivID") String deriID) {
        return Response.ok(DerivateTools.getDerivateFolderAsJson(deriID).toString()).build();
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("exists")
    @MCRRestrictedAccess(DerivateBrowserPermission.class)
    public DerivateFiles existsCheck(DerivateFiles derivateFiles) {
        DerivateFiles.File[] files = derivateFiles.getFiles();
        for (int i = 0; i < files.length; i++) {
            String derivId = files[i].getDerivId();
            String path = files[i].getPath();

            MCRPath mcrPath = MCRPath.getPath(derivId, path);

            if (Files.exists(mcrPath)) {
                files[i].setExists(1);
                try {
                    FileTime lastModifiedTime = Files.getLastModifiedTime(mcrPath);
                    SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
                    String lastModifiedTimeStr = sdf.format(lastModifiedTime.toMillis());
                    long size = Files.size(mcrPath);

                    files[i].setLastModifiedTime(lastModifiedTimeStr);
                    files[i].setSize(size);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                //jsonFile.addProperty("exists", "1");
                //jsonFile.add("existingFile", childJson);
                //uploadFilesAndAsk in derivatebrowser-upload.js - 368
                //askOverwrite in derivatebrowser-uploadEntry.js - 85
            } else {
                files[i].setExists(0);
            }
        }
        return derivateFiles;
    }

    @POST
    @Path("startUpload")
    @MCRRestrictedAccess(DerivateBrowserPermission.class)
    @Produces(MediaType.APPLICATION_JSON)
    public UploadResponse startUpload(@QueryParam("documentID") String documentID,
                                      @QueryParam("derivateID") String derivateID, @QueryParam("num") int num) {
        if (derivateID.equals("")) {
            derivateID = null;
        }
        UUID uuid = JPUploader.start(documentID, derivateID, num);

        return new UploadResponse(uuid.toString());
    }

    @POST
    @Path("finishUpload")
    @MCRRestrictedAccess(DerivateBrowserPermission.class)
    @Produces(MediaType.APPLICATION_JSON)
    public Response finishUpload(@QueryParam("uploadID") String uploadID) {
        try {
            JPUploader.finish(UUID.fromString(uploadID));
        } catch (Exception exc) {
            throw new InternalServerErrorException("Unable to finish upload with id " + uploadID, exc);
        }
        return Response.ok().build();
    }

    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("upload")
    @MCRRestrictedAccess(DerivateBrowserPermission.class)
    public UploadResponse getUpload(@FormDataParam("uploadID") String uploadID,
                                    @FormDataParam("file") InputStream inputStream, @FormDataParam("filename") String filename,
                                    @FormDataParam("size") long filesize, @FormDataParam("path") String path,
                                    @FormDataParam("overwrite") boolean overwrite, @FormDataParam("type") String type) {

        String filePath = path + "/" + filename;
        MCRUploadHelper.checkPathName(filePath.startsWith("/") ? filePath.substring(1) : filePath);
        UUID uuid = UUID.fromString(uploadID);
        MCRUploadHandlerIFS uploadHandler = JPUploader.get(uuid);
        if (uploadHandler == null) {
            throw new WebApplicationException(
                Response.status(Status.NOT_FOUND).entity("No upload handler with id " + uploadID + " found!").build());
        }
        type = type.toLowerCase();

        // TODO: ignore supported files types because there are some issues with windows 7 and
        // firefox V. 49.0.1. the type is octet stream and not pdf
        // List<String> fileTypes = CONFIG.getStrings("MCR.Derivate.Upload.SupportedFileTypes");
        // if (fileTypes.contains(type)) {
        if (overwrite) {
            MCRPath mcrFilePath = MCRPath.getPath(uploadHandler.getDocumentID(), path + "/" + filename);
            if (DerivateTools.delete(mcrFilePath) != 1) {
                throw new WebApplicationException(
                    "Unable to delete/overwrite " + filePath + " while uploading " + filename);
            }
        }

        try {
            JPUploader.upload(uuid, filePath, inputStream, filesize);
        } catch (Exception e) {
            throw new WebApplicationException("Error while uploading file " + filename, e);
        }

        UploadResponse uploadResponse = new UploadResponse(uploadID);
        uploadResponse.setDerivateID(uploadHandler.getDerivateID());
        String md5forFile = DerivateTools.getMD5forFile(uploadHandler.getDerivateID(), filePath);
        uploadResponse.setMd5(md5forFile);

        return uploadResponse;
        // }
        // throw new WebApplicationException(
        //    new MCRException("Unsupported media type " + type + ". Only one of " + fileTypes + " is allowed."),
        //    Status.UNSUPPORTED_MEDIA_TYPE);
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("addURN")
    @MCRRestrictedAccess(DerivateBrowserPermission.class)
    public DerivateFiles.File addURN(DerivateFiles.File file) {
        String derivId = file.getDerivId();
        String path = file.getPath();
        String urn = DerivateTools.addURNToFile(derivId, path);

        file.setURN(urn);
        return file;
    }

    @POST
    @Path("addURN/{derivId}")
    @MCRRestrictedAccess(DerivateBrowserPermission.class)
    public Response addURN(@PathParam("derivId") String derivId) {
        if (DerivateTools.addURNToDerivate(derivId)) {
            return Response.ok().build();
        }
        return Response.serverError().build();
    }

    @PUT
    @Path("moveDocs")
    @MCRRestrictedAccess(DerivateBrowserPermission.class)
    public MoveDocs moveDocs(MoveDocs moveDocs) {
        MoveDocs.MoveDoc[] docs = moveDocs.getDocs();

        for (int i = 0; i < docs.length; i++) {
            MoveDocs.MoveDoc currentDoc = docs[i];
            String objID = currentDoc.getObjId();
            String newParentID = currentDoc.getNewParentId();
            Boolean success = DocumentTools.move(objID, newParentID);
            currentDoc.setSuccess(success);
        }
        return moveDocs;
    }

    @POST
    @Path("link")
    @MCRRestrictedAccess(DerivateBrowserPermission.class)
    public Response setLink(@QueryParam("docID") String docID, @QueryParam("imgPath") String imgPath) {
        if (docID != null && !docID.equals("") && !docID.contains("derivate") && imgPath != null
            && !imgPath.equals("")) {
            try {
                DerivateTools.setLink(docID, imgPath);
            } catch (MCRActiveLinkException e) {
                throw new WebApplicationException("Unable to set link " + imgPath + " from " + docID, e);
            } catch (MCRAccessException e) {
                throw new WebApplicationException(Status.UNAUTHORIZED);
            }
            return Response.ok().build();
        } else {
            throw new WebApplicationException("Unable to set link " + imgPath + " from " + docID);
        }
    }

    @DELETE
    @Path("link")
    @MCRRestrictedAccess(DerivateBrowserPermission.class)
    public Response removeLink(@QueryParam("docID") String docID, @QueryParam("imgPath") String imgPath) {
        if (docID != null && !docID.equals("") && !docID.contains("derivate") && imgPath != null
            && !imgPath.equals("")) {
            try {
                DerivateTools.removeLink(docID, imgPath);
            } catch (MCRActiveLinkException e) {
                throw new WebApplicationException("Unable to remove link " + imgPath + " from " + docID, e);
            } catch (MCRAccessException e) {
                throw new WebApplicationException(Status.UNAUTHORIZED);
            }
            return Response.ok().build();
        } else {
            throw new WebApplicationException("Unable to remove link " + imgPath + " from " + docID);
        }
    }

    @POST
    @Path("tileDerivate")
    @MCRRestrictedAccess(DerivateBrowserPermission.class)
    public Response tileDerivate(@QueryParam("deriID") String deriID) {
        if (DerivateTools.tileDerivate(deriID)) {
            return Response.ok().build();
        }
        return Response.serverError().build();
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("renameMultiple")
    @MCRRestrictedAccess(DerivateBrowserPermission.class)
    public RenameMultiple.Response[] renameMultiple(RenameMultiple data) {
        String derivId = data.getDerivId();
        String newName = data.getNewName();
        String pattern = data.getPattern();


        try {
            Map<String, String> resultMap = DerivateTools.renameFiles(derivId, pattern, newName);

            for (Map.Entry<String, String> entry : resultMap.entrySet()) {
                String currentOldName = entry.getKey();
                String currentNewName = entry.getValue();
                data.add(new RenameMultiple.Response(currentOldName, currentNewName));
            }

            return data.getResponseList();

        } catch (Exception e) {
            throw new WebApplicationException("Error while renaming!", e);
        }
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("renameMultiple/test")
    @MCRRestrictedAccess(DerivateBrowserPermission.class)
    public Response renameMultipleTest(RenameMultiple data) {
        String filename = data.getFileName();
        String pattern = data.getPattern();
        String newName = data.getNewName();

        String newFilename = "";

        try {
            Pattern patternObj = Pattern.compile(pattern);
            Matcher matcher = patternObj.matcher(filename);
            newFilename = matcher.replaceAll(newName);
            LOGGER.info("The file {} will be renamed to {}", filename, newFilename);
        } catch (PatternSyntaxException e) {
            LOGGER.info("The pattern '{}' contains errors!", pattern);
        } catch (IndexOutOfBoundsException e) {
            LOGGER.info("The file {} can't be renamed to {}. To many groups!", filename, newName);
        } catch (IllegalArgumentException e) {
            LOGGER.info("The new name '{}' contains illegal characters!", newName);
        }

        return Response.ok(newFilename).build();
    }

    @GET
    @Path("checkFileType/{derivID}{path:(/.*)*}")
    public Response checkFileType(@PathParam("derivID") String derivID, @PathParam("path") String path) {
        return Response.ok(DerivateTools.checkFileType(derivID, path)).build();
    }

}
