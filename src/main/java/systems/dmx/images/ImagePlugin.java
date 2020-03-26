package systems.dmx.images;


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
import systems.dmx.core.Topic;
import systems.dmx.core.osgi.PluginActivator;
import systems.dmx.core.service.Inject;
import systems.dmx.core.service.Transactional;
import systems.dmx.core.util.JavaUtils;
import systems.dmx.files.DirectoryListing;
import systems.dmx.files.DirectoryListing.FileItem;
import systems.dmx.files.FilesService;
import systems.dmx.files.ItemKind;
import systems.dmx.files.ResourceInfo;
import systems.dmx.files.StoredFile;
import systems.dmx.files.UploadedFile;

/**
 * CKEditor compatible resources for image upload and browse.
 */
@Path("/images")
public class ImagePlugin extends PluginActivator implements ImageService {
    
    private static Logger log = Logger.getLogger(ImagePlugin.class.getName());

    public static final String FILEREPO_BASE_URI_NAME       = "filerepo";
    public static final String FILEREPO_IMAGES_SUBFOLDER    = "images";
    public static final String DM4_HOST_URL = System.getProperty("dm4.host.url");

    @Inject private FilesService files;

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
    public Topic upload(UploadedFile image) {
        String imagesFolderPath = getImagesDirectoryInFileRepo();
        log.info("Upload image " + image.getName() + " to filerepo folder=" + imagesFolderPath);
        try {
            StoredFile file = files.storeFile(image, imagesFolderPath);
            String path = imagesFolderPath + File.separator + file.getFileName();
            return files.getFileTopic(path);
        } catch (Exception e) {
            log.severe(e.getMessage() + ", caused by " + e.getCause().getMessage());
            return null;
        }
    }

    @POST
    @Path("/resize/{topicId}/{maxSize}/{mode}")
    @Produces(MediaType.APPLICATION_JSON)
    @Transactional
    @Override
    public Topic resizeImageFileTopic(@PathParam("topicId") long fileTopicId, @PathParam("maxSize") int maxSize,
            @PathParam("mode") String mode) {
        log.info("File Topic to Resize (ID: " + fileTopicId + ") Max Size: " + maxSize + "px");
        try {
            File fileTopicFile = files.getFile(fileTopicId);
            String fileTopicFileName = fileTopicFile.getName();
            Topic fileTopic = dmx.getTopic(fileTopicId).loadChildTopics();
            String fileTopicRepositoryPath = fileTopic.getChildTopics().getString("dm4.files.path");
            String fileMediaType = fileTopic.getChildTopics().getString("dm4.files.media_type");
            if (fileMediaType.contains("jpeg") || fileMediaType.contains("jpg") || fileMediaType.contains("png")) {
                log.info("Image File Topic Path requested to be RESIZED: " + fileTopicRepositoryPath);
                BufferedImage srcImage = ImageIO.read(fileTopicFile); // Load image
                log.info("Image File Buffered " + srcImage); // Print Image Metadata
                BufferedImage scaledImage = null;
                if (mode.equals("width")) {
                    scaledImage = Scalr.resize(srcImage, org.imgscalr.Scalr.Mode.FIT_TO_WIDTH, maxSize);
                } else if (mode.equals("height")) {
                    scaledImage = Scalr.resize(srcImage, org.imgscalr.Scalr.Mode.FIT_TO_HEIGHT, maxSize);
                } else {
                    scaledImage = Scalr.resize(srcImage, org.imgscalr.Scalr.Mode.AUTOMATIC, maxSize);
                }
                String imageFileEnding = fileTopicFileName.substring(fileTopicFileName.indexOf(".") + 1);
                String imageFileTopicParentRepositoryPath = getParentFolderRepositoryPath(fileTopicRepositoryPath);
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
                Topic resizedImageFileTopic = files.getFileTopic(
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

    private String getParentFolderRepositoryPath(String fileTopicRepositoryPath) {
        return fileTopicRepositoryPath.substring(0, fileTopicRepositoryPath.lastIndexOf("/"));
    }

    private void createResizedImageAssociation(Topic original, Topic resized) {
        dmx.createAssoc(mf.newAssocModel("dm4.images.resized_image",
                mf.newTopicPlayerModel(original.getId(), "dm4.core.parent"),
                mf.newTopicPlayerModel(resized.getId(), "dm4.core.child")));
    }

    /**
     * Returns a set of all image source URLs.
     * Todo: Deprecated
     * @return all image sources
     */
    @GET
    @Path("/browse")
    @Produces(MediaType.APPLICATION_JSON)
    public List<Image> browse() {
        String imagesFolderPath = getImagesDirectoryInFileRepo();
        try {
            ArrayList<Image> images = new ArrayList<Image>();
            DirectoryListing imagesDirectory = files.getDirectoryListing(imagesFolderPath);
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

    /**
     * Constructs path to the "images" folder, either in a workspace or in a global file repository.
     * @return String   Repository path for storing images.
     */
    private String getImagesDirectoryInFileRepo() {
        String folderPath = FILEREPO_IMAGES_SUBFOLDER; // global filerepo
        if (!files.pathPrefix().equals("/")) { // add workspace specific path in front of image folder name
            folderPath = files.pathPrefix() + File.separator + FILEREPO_IMAGES_SUBFOLDER;
        }
        try {
            // check image file repository
            if (files.fileExists(folderPath)) {
                ResourceInfo resourceInfo = files.getResourceInfo(files.pathPrefix() + File.separator +
                    FILEREPO_IMAGES_SUBFOLDER);
                if (resourceInfo.getItemKind() != ItemKind.DIRECTORY) {
                    String message = "ImagePlugin: \"images\" storage directory in repo path " + folderPath + " can not be used";
                    throw new IllegalStateException(message);
                }
            } else { // images subdirectory does not exist yet in repo
                log.info("Creating the \"images\" subfolder on the fly for new filerepo in " + folderPath+ "!");
                // A (potential) workspace folder gets created no the fly, too (since #965).
                files.createFolder(FILEREPO_IMAGES_SUBFOLDER, files.pathPrefix());
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return folderPath;
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
        return DM4_HOST_URL + FILEREPO_BASE_URI_NAME + "/" + JavaUtils.encodeURIComponent(path);
    }

}
