package de.deepamehta.plugins.images;

import java.io.File;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;

import de.deepamehta.core.ResultSet;
import de.deepamehta.core.osgi.PluginActivator;
import de.deepamehta.core.service.ClientState;
import de.deepamehta.core.service.PluginService;
import de.deepamehta.core.service.annotation.ConsumesService;
import de.deepamehta.plugins.files.DirectoryListing.FileItem;
import de.deepamehta.plugins.files.ItemKind;
import de.deepamehta.plugins.files.ResourceInfo;
import de.deepamehta.plugins.files.StoredFile;
import de.deepamehta.plugins.files.UploadedFile;
import de.deepamehta.plugins.files.service.FilesService;

/**
 * CKEditor compatible resources for image upload and browse.
 */
@Path("/images")
public class ImagePlugin extends PluginActivator {

    public static final String IMAGES = "images";

    private static final String FILE_REPOSITORY_PATH = System.getProperty("dm4.filerepo.path");

    private static Logger log = Logger.getLogger(ImagePlugin.class.getName());

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
     * @param cookie
     *            Actual cookie.
     * @return JavaScript snippet that calls CKEditor
     */
    @POST
    @Path("/upload")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.TEXT_HTML)
    public String upload(//
            UploadedFile image,//
            @QueryParam("CKEditorFuncNum") Long func,//
            @HeaderParam("Cookie") ClientState cookie) {
        log.info("upload image " + image.getName());
        try {
            StoredFile file = fileService.storeFile(image, IMAGES, cookie);
            String path = "/" + IMAGES + "/" + file.getFileName();
            return getCkEditorCall(func, getRepoUri(path), "");
        } catch (Exception e) {
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
    public ResultSet<Image> browse() {
        log.info("browse images");
        try {
            Set<Image> images = new HashSet<Image>();
            for (FileItem image : fileService.getDirectoryListing(IMAGES).getFileItems()) {
                String src = getRepoUri(image.getPath());
                String fileName = image.getName();
                images.add(new Image(src, image.getMediaType(), image.getSize(), fileName));
            }
            return new ResultSet<Image>(images.size(), images);
        } catch (WebApplicationException e) { // fileService.getDirectoryListing
            throw e; // do not wrap it again
        } catch (Exception e) {
            throw new WebApplicationException(e);
        }
    }

    /**
     * Nullify file service reference.
     */
    @Override
    public void serviceGone(PluginService service) {
        if (service == fileService) {
            fileService = null;
        }
    }

    /**
     * Reference the file service and create the repository path if necessary.
     */
    @Override
    @ConsumesService("de.deepamehta.plugins.files.service.FilesService")
    public void serviceArrived(PluginService service) {
        if (service instanceof FilesService) {
            log.fine("file service arrived");
            fileService = (FilesService) service;
            postInstallMigration();
        }
    }

    private void postInstallMigration() {
        // TODO move the initialization to migration "0"
        try {
            // check image file repository
            ResourceInfo resourceInfo = fileService.getResourceInfo(IMAGES);
            if (resourceInfo.getItemKind() != ItemKind.DIRECTORY) {
                String message = "image storage directory " + FILE_REPOSITORY_PATH + File.separator
                        + IMAGES + " can not be used";
                throw new IllegalStateException(message);
            }
        } catch (WebApplicationException e) {
            // catch fileService info request error
            if (e.getResponse().getStatus() != 404) {
                throw e;
            } else {
                log.info("create image directory");
                fileService.createFolder(IMAGES, "/");
            }
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
        return uriInfo.getBaseUri() + "filerepo" + path;
    }
}
