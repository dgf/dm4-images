package de.deepamehta.plugins.images;

import java.util.logging.Logger;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;

import de.deepamehta.core.osgi.PluginActivator;
import de.deepamehta.core.service.Inject;
import de.deepamehta.core.service.ResultList;
import de.deepamehta.core.service.Transactional;
import de.deepamehta.plugins.files.DirectoryListing.FileItem;
import de.deepamehta.plugins.files.StoredFile;
import de.deepamehta.plugins.files.UploadedFile;
import de.deepamehta.plugins.files.service.FilesService;
import java.util.ArrayList;

/**
 * CKEditor compatible resources for image upload and browse.
 */
@Path("/images")
public class ImagePlugin extends PluginActivator {
    
    private static Logger log = Logger.getLogger(ImagePlugin.class.getName());

    public static final String FILEREPO_BASE_URI_NAME = "filerepo";
    public static final String FILEREPO_IMAGES_SUBFOLDER = "images";
    public static final String FILE_REPOSITORY_PATH = System.getProperty("dm4.filerepo.path");

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
     * @param cookie
     *            Actual cookie.
     * @return JavaScript snippet that calls CKEditor
     */
    @POST
    @Path("/upload")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.TEXT_HTML)
    @Transactional
    public String upload(//
            UploadedFile image,//
            @QueryParam("CKEditorFuncNum") Long func) {
        log.info("upload image " + image.getName());
        try {
            StoredFile file = fileService.storeFile(image, FILEREPO_IMAGES_SUBFOLDER);
            String path = "/" + FILEREPO_IMAGES_SUBFOLDER + "/" + file.getFileName();
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
    public ResultList<Image> browse() {
        log.info("browse images");
        try {
            ArrayList<Image> images = new ArrayList<Image>();
            for (FileItem image : fileService.getDirectoryListing(FILEREPO_IMAGES_SUBFOLDER).getFileItems()) {
                String src = getRepoUri(image.getPath());
                images.add(new Image(src, image.getMediaType(), 
                    image.getSize(), image.getName()));
            }
            return new ResultList<Image>(images.size(), images);
        } catch (WebApplicationException e) { // fileService.getDirectoryListing
            throw e; // do not wrap it again
        } catch (Exception e) {
            throw new WebApplicationException(e);
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
}
