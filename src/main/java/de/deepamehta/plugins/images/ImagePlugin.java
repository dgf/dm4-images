package de.deepamehta.plugins.images;

import de.deepamehta.core.Association;
import de.deepamehta.core.Topic;
import de.deepamehta.core.osgi.PluginActivator;
import de.deepamehta.core.service.Inject;
import de.deepamehta.core.service.Transactional;
import de.deepamehta.files.DirectoryListing.FileItem;
import de.deepamehta.files.*;
import java.awt.image.BufferedImage;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.ws.rs.core.Response;
import org.imgscalr.Scalr;

/**
 * CKEditor compatible resources for image upload and browse.
 */
@Path("/images")
public class ImagePlugin extends PluginActivator {
    
    private static Logger log = Logger.getLogger(ImagePlugin.class.getName());

    public static final String FILEREPO_BASE_URI_NAME       = "filerepo";
    public static final String FILEREPO_IMAGES_SUBFOLDER    = "images";
    public static final String DM4_HOST_URL = System.getProperty("dm4.host.url");

    @Inject private FilesService fileService;

    /**
     * CKEditor image upload integration, see
     * CKEDITOR.config.filebrowserImageBrowseUrl
     * 
     * @param image
     *            Uploaded file resource.
     * @param func
     *            CKEDITOR function number to call.
     * @return JavaScript snippet that calls CKEditor
     */
    @POST
    @Path("/upload/ckeditor")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.TEXT_HTML)
    @Transactional
    public String uploadCKEditor(UploadedFile image, @QueryParam("CKEditorFuncNum") Long func) {
        String imagesFolderPath = getImagesDirectoryInFileRepo();
        log.info("Upload image " + image.getName() + " to filerepo folder=" + imagesFolderPath);
        try {
            StoredFile file = fileService.storeFile(image, imagesFolderPath);
            String path = imagesFolderPath + File.separator + file.getFileName();
            return getCkEditorCall(func, getRepoUri(path), "");
        } catch (Exception e) {
            log.severe(e.getMessage() + ", caused by " + e.getCause().getMessage());
            return getCkEditorCall(func, "", e.getMessage());
        }
    }

    /**
     * Standard image upload integration.
     * @param image     Uploaded file resource.
     * @return topic    File Topic
     */
    @POST
    @Path("/upload")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    @Transactional
    public Topic upload(UploadedFile image, @QueryParam("CKEditorFuncNum") Long func) {
        String imagesFolderPath = getImagesDirectoryInFileRepo();
        log.info("Upload image " + image.getName() + " to filerepo folder=" + imagesFolderPath);
        try {
            StoredFile file = fileService.storeFile(image, imagesFolderPath);
            String path = imagesFolderPath + File.separator + file.getFileName();
            return fileService.getFileTopic(path);
        } catch (Exception e) {
            log.severe(e.getMessage() + ", caused by " + e.getCause().getMessage());
            return null;
        }
    }

    /**
     * Resizes an existing image represented by a DeepaMehta 4 file topic. File topic must already exist.
     * @return  Topic   A new file topic representing the resized image.
     */
    @GET
    @Path("/resize/{topicId}/{maxSize}")
    @Produces(MediaType.APPLICATION_JSON)
    @Transactional
    public Topic resizeImageFileTopic(@PathParam("topicId") long fileTopicId, @PathParam("maxSize") int maxSize) {
        log.info("File Topic to Resize (ID: " + fileTopicId + ") Max Size: " + maxSize + "px");
        try {
            File fileTopicFile = fileService.getFile(fileTopicId);
            String fileTopicFileName = fileTopicFile.getName();
            Topic fileTopic = dm4.getTopic(fileTopicId).loadChildTopics();
            String fileTopicRepositoryPath = fileTopic.getChildTopics().getString("dm4.files.path");
            String fileMediaType = fileTopic.getChildTopics().getString("dm4.files.media_type");
            if (fileMediaType.contains("jpeg") || fileMediaType.contains("png")) {
                log.info("Image File Topic Path requested to be RESIZED: " + fileTopicRepositoryPath);
                BufferedImage srcImage = ImageIO.read(fileTopicFile); // Load image
                log.info("Image File Buffered " + srcImage); // Print Image Metadata
                BufferedImage scaledImage = Scalr.resize(srcImage, maxSize); // Scale image
                // 
                String imageFileEnding = fileTopicFileName.substring(fileTopicFileName.indexOf(".") + 1);
                String imageFileTopicParentRepositoryPath = getParentFoldRepositoryPath(fileTopicRepositoryPath);
                String newFileName = calculateResizedFilename(fileTopicFileName, maxSize + "");
                File resizedImageFile = new File(fileTopicFile.getParent() + File.separator + newFileName);
                if (resizedImageFile.createNewFile()) {
                    ImageIO.write(scaledImage, imageFileEnding, resizedImageFile);
                    log.info("Resized Image File \"" + resizedImageFile.getAbsolutePath() + "\" CREATED");
                } else {
                    if (imageFileEnding.equals("jpg")) imageFileEnding = "jpeg";
                    ImageIO.write(scaledImage, imageFileEnding, resizedImageFile);
                    log.warning("Image File already exists \"" + resizedImageFile.getPath() + "\" - REWRITE");
                }
                // Create File topic for newly created file
                Topic resizedImageFileTopic = fileService.getFileTopic(
                    imageFileTopicParentRepositoryPath + File.separator + newFileName);
                // Associate new file topic with original file topic
                createResizedImageAssociation(fileTopic, resizedImageFileTopic);
                return resizedImageFileTopic;
            } else {
                throw new WebApplicationException(new RuntimeException("Sorry! At the moment we can "
                    + "only resize JPGs or PNGs and not file topics with MediaType: " + fileMediaType),
                    Response.Status.BAD_REQUEST);
            }
        } catch (Exception ex) {
            throw new WebApplicationException(ex);
        }
    }

    private String calculateResizedFilename(String originalFilename, String sizeParameter) {
        String imageFileEnding = originalFilename.substring(originalFilename.indexOf(".") + 1);
        String imageFileBeginning = originalFilename.substring(originalFilename.lastIndexOf("/") + 1, originalFilename.indexOf("."));
        return imageFileBeginning + "-" + sizeParameter + "px." + imageFileEnding;
    }

    private String getParentFoldRepositoryPath(String fileTopicRepositoryPath) {
        return fileTopicRepositoryPath.substring(0, fileTopicRepositoryPath.lastIndexOf("/"));
    }

    private void createResizedImageAssociation(Topic original, Topic resized) {
        dm4.createAssociation(mf.newAssociationModel("dm4.images.resized_image",
                mf.newTopicRoleModel(original.getId(), "dm4.core.parent"),
                mf.newTopicRoleModel(resized.getId(), "dm4.core.child")));
    }

    /**
     * Returns a set of all image source URLs.
     * 
     * @return all image sources
     */
    @GET
    @Path("/browse")
    @Produces(MediaType.APPLICATION_JSON)
    public List<Image> browse() {
        String imagesFolderPath = getImagesDirectoryInFileRepo();
        try {
            ArrayList<Image> images = new ArrayList<Image>();
            DirectoryListing imagesDirectory = fileService.getDirectoryListing(imagesFolderPath);
            for (FileItem image : imagesDirectory.getFileItems()) {
                log.info("  Include image in repository with name \"" + image.getName() + "\"");
                String src = getRepoUri(image.getPath());
                images.add(new Image(src, image.getMediaType(), image.getSize(), image.getName()));
            }
            return images;
        } catch (WebApplicationException e) { // fileService.getDirectoryListing
            log.info("Calling for a DirectoryListing has THROWN an Error");
            throw e; // do not wrap it again
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /** Caller must make sure that folderPath has set the right prefix(). */
    private String getImagesDirectoryInFileRepo() {
        String folderPath = FILEREPO_IMAGES_SUBFOLDER; // global filerepo
        if (!fileService.pathPrefix().equals("/")) { // add workspace specific path in front of image folder name
            folderPath = fileService.pathPrefix() + File.separator + FILEREPO_IMAGES_SUBFOLDER;
        }
        try {
            // check image file repository
            if (fileService.fileExists(folderPath)) {
                ResourceInfo resourceInfo = fileService.getResourceInfo(fileService.pathPrefix() + File.separator +
                    FILEREPO_IMAGES_SUBFOLDER);
                if (resourceInfo.getItemKind() != ItemKind.DIRECTORY) {
                    String message = "ImagePlugin: \"images\" storage directory in repo path " + folderPath + " can not be used";
                    throw new IllegalStateException(message);
                }
            } else { // images subdirectory does not exist yet in repo
                log.info("Creating the \"images\" subfolder on the fly for new filerepo in " + folderPath+ "!");
                // A (potential) workspace folder gets created no the fly, too (since #965).
                fileService.createFolder(FILEREPO_IMAGES_SUBFOLDER, fileService.pathPrefix());
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return folderPath;
    }

    /**
     * Returns an in-line JavaScript snippet that calls the parent CKEditor.
     * 
     * @param func
     *            CKEDITOR function number.
     * @param uri
     *            Resource URI.
     * @param error
     *            Error message.
     * @return JavaScript snippet that calls CKEditor
     */
    private String getCkEditorCall(Long func, String uri, String error) {
        return "<script type='text/javascript'>" + "window.parent.CKEDITOR.tools.callFunction("
                + func + ", '" + uri + "', '" + error + "')" + "</script>";
    }

    /**
     * Returns an external accessible file repository URI of path based on the
     * <code>dm4.host.url</code> platform configuration option.
     * 
     * @param path
     *            Relative path of a file repository resource.
     * @return URI
     */
    private String getRepoUri(String path) {
        return DM4_HOST_URL + FILEREPO_BASE_URI_NAME + path;
    }

}
