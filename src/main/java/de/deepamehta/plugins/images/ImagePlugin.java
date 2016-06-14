package de.deepamehta.plugins.images;

import de.deepamehta.core.Topic;
import de.deepamehta.core.osgi.PluginActivator;
import de.deepamehta.core.service.Inject;
import de.deepamehta.core.service.Transactional;
import de.deepamehta.files.DirectoryListing.FileItem;
import de.deepamehta.files.*;
import java.awt.image.BufferedImage;
import java.awt.image.ImagingOpException;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.imageio.stream.ImageInputStream;
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
    @Path("/upload")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.TEXT_HTML)
    @Transactional
    public String upload(UploadedFile image, @QueryParam("CKEditorFuncNum") Long func) {
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

    @GET
    @Path("/resize/{topicId}/{maxSize}")
    @Produces(MediaType.APPLICATION_JSON)
    @Transactional
    public Topic resizeImageFileTopic(@PathParam("topicId") long fileTopicId, @PathParam("maxSize") int maxSize) {
        log.info("Resize Image File (Topic ID: " + fileTopicId + ") Max Size: " + maxSize + "px");
        try {
            File imageFile = fileService.getFile(fileTopicId);
            Topic imageFileTopic = dm4.getTopic(fileTopicId).loadChildTopics();
            String imageFileTopicRepositoryPath = imageFileTopic.getChildTopics().getString("dm4.files.path");
            log.info("Image File Topic Path requested to be RESIZED: " + imageFileTopicRepositoryPath);
            BufferedImage srcImage = ImageIO.read(imageFile); // Load image
            log.info("Image File Buffered " + srcImage);
            BufferedImage scaledImage = Scalr.resize(srcImage, maxSize); // Scale image
            String imageFileName = imageFile.getName();
            String imageFileEnding = imageFileName.substring(imageFileName.indexOf(".") + 1);
            String imageFileBeginning = imageFileName.substring(imageFileName.lastIndexOf("/") + 1, imageFileName.indexOf("."));
            String imageFileTopicParentRepositoryPath = imageFileTopicRepositoryPath.substring(0, imageFileTopicRepositoryPath.lastIndexOf("/"));
            log.info("Image File Name Ending " + imageFileEnding);
            String newFileName = imageFileBeginning + "-300px." + imageFileEnding;
            File resizedImageFile = new File(imageFile.getParent() + File.separator + newFileName);
            if (resizedImageFile.createNewFile()) {
                log.info("Created New Image File " + resizedImageFile.getAbsolutePath());
                if (imageFileEnding.equals("jpg")) imageFileEnding = "jpeg";
                ImageIO.write(scaledImage, imageFileEnding, resizedImageFile);
                log.info("Wrote new Image File... " + resizedImageFile.getAbsolutePath());
            } else {
                log.warning("Image File already exists" + imageFile.getParent() + File.separator + newFileName);
                if (imageFileEnding.equals("jpg")) imageFileEnding = "jpeg";
                ImageIO.write(scaledImage, imageFileEnding, resizedImageFile);
                log.info("Updated Image File... " + resizedImageFile.getAbsolutePath());
            }
            log.info("Fetching Image File Topic " + resizedImageFile.getAbsolutePath());
            return fileService.getFileTopic(imageFileTopicParentRepositoryPath + File.separator + newFileName);
        } catch (IOException e) {
            log.severe("Resizing Image Failed IOException, " + e.getMessage() + ", caused by " + e.getCause());
            return null;
        } catch (IllegalArgumentException e) {
            log.severe("Resizing Image Failed IllegalArgumentException, " + e.getMessage() + ", caused by " + e.getCause());
            return null;
        } catch (ImagingOpException e) {
            log.severe("Resizing Image Failed ImagingOpException " + e.getMessage() + ", caused by " + e.getCause());
            return null;
        }
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
                log.info("Images Subfolder exists in Repo at=" + folderPath + " doing nothing.");
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
