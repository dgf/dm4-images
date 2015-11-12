package de.deepamehta.plugins.images;

import de.deepamehta.core.osgi.PluginActivator;
import de.deepamehta.core.service.Inject;
import de.deepamehta.core.Topic;
import de.deepamehta.core.service.ResultList;
import de.deepamehta.core.service.Transactional;
import de.deepamehta.plugins.files.DirectoryListing.FileItem;
import de.deepamehta.plugins.files.*;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.RuntimeDelegate;
import java.io.File;
import java.util.ArrayList;
import java.util.logging.Logger;

/**
 * CKEditor compatible resources for image upload and browse.
 */
@Path("/images")
public class ImagePlugin extends PluginActivator {
    
    private static Logger log = Logger.getLogger(ImagePlugin.class.getName());

    public static final String FILEREPO_BASE_URI_NAME = "filerepo";
    public static final String FILEREPO_IMAGES_SUBFOLDER = "images";
    // public static final String FILE_REPOSITORY_PATH = System.getProperty("dm4.filerepo.path");

    @Inject
    private FilesService fileService;

    @Context
    private UriInfo uriInfo;

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
        log.info("upload image " + image.getName());
        createImagesDirectoryInFileRepo();
        try {
            StoredFile file = fileService.storeFile(image, prefix() + File.separator + FILEREPO_IMAGES_SUBFOLDER);
            String path = prefix() + File.separator + FILEREPO_IMAGES_SUBFOLDER + File.separator + file.getFileName();
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
    public ResultList<Image> browse() {
        log.info("browse images in repository path " + prefix() + File.separator + FILEREPO_IMAGES_SUBFOLDER);
        createImagesDirectoryInFileRepo();
        try {
            ArrayList<Image> images = new ArrayList<Image>();
            DirectoryListing imagesDirectory= fileService.getDirectoryListing(prefix() + File.separator +
                    FILEREPO_IMAGES_SUBFOLDER);
            for (FileItem image : imagesDirectory.getFileItems()) {
                String src = getRepoUri(image.getPath());
                images.add(new Image(src, image.getMediaType(), image.getSize(), image.getName()));
            }
            return new ResultList<Image>(images.size(), images);
        } catch (WebApplicationException e) { // fileService.getDirectoryListing
            throw e; // do not wrap it again
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void createImagesDirectoryInFileRepo() {
        try {
            // check image file repository
            ResourceInfo resourceInfo = fileService.getResourceInfo(prefix() + File.separator +
                    FILEREPO_IMAGES_SUBFOLDER);
            // depending on prefix() we check for an "images" folder in the global or workspace filerepo
            if (resourceInfo.getItemKind() != ItemKind.DIRECTORY) {
                String message = "ImagePlugin: \"images\" storage directory in repo path " + fileService.getFile("/") +
                        prefix() + File.separator + ImagePlugin.FILEREPO_IMAGES_SUBFOLDER + " can not be used";
                throw new IllegalStateException(message);
            }
        } catch (WebApplicationException e) {
            log.info("Created the \"images\" subfolder on the fly for new filerepo in " + fileService.getFile("/") +
                    prefix() + File.separator + FILEREPO_IMAGES_SUBFOLDER + "!");
            try {
                fileService.createFolder(FILEREPO_IMAGES_SUBFOLDER, prefix());
            } catch (RuntimeException ex) {
                log.warning("RuntimeException caught during folder creation, presumably the folder " +
                        "must already EXIST, so OK:" + ex.getMessage());
            }
            // catch fileService create request failed because of: folder exists
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Returns a in-line JavaScript snippet that calls the parent CKEditor.
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
     * Returns an external accessible file repository URI of path based on
     * actual request URI.
     * 
     * @param path
     *            Relative path of a file repository resource.
     * @return URI
     */
    private String getRepoUri(String path) {
        // ### in some cases the uriInfo (Context) may be empty!
        return uriInfo.getBaseUri() + FILEREPO_BASE_URI_NAME + path;
    }

    private String prefix() {
        File repo = fileService.getFile("/");
        return ((FilesPlugin) fileService).repoPath(repo);
    }

}
