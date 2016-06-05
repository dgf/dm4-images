package de.deepamehta.plugins.images;

import de.deepamehta.core.osgi.PluginActivator;
import de.deepamehta.core.service.Inject;
import de.deepamehta.core.service.Transactional;
import de.deepamehta.files.DirectoryListing.FileItem;
import de.deepamehta.files.*;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

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
