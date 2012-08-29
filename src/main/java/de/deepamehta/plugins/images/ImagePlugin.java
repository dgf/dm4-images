package de.deepamehta.plugins.images;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
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

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import de.deepamehta.core.osgi.PluginActivator;
import de.deepamehta.core.service.PluginService;
import de.deepamehta.core.service.listener.PluginServiceArrivedListener;
import de.deepamehta.core.service.listener.PluginServiceGoneListener;
import de.deepamehta.core.util.DeepaMehtaUtils;
import de.deepamehta.plugins.files.DirectoryListing;
import de.deepamehta.plugins.files.ResourceInfo;
import de.deepamehta.plugins.files.StoredFile;
import de.deepamehta.plugins.files.UploadedFile;
import de.deepamehta.plugins.files.service.FilesService;

@Path("/images")
public class ImagePlugin extends PluginActivator implements PluginServiceArrivedListener,
        PluginServiceGoneListener {

    public static final String IMAGES = "images";

    private Logger log = Logger.getLogger(getClass().getName());

    private FilesService fileService;

    /**
     * CKEditor image upload integration, see
     * CKEDITOR.config.filebrowserImageBrowseUrl
     * 
     * @return a JavaScript snippet that calls CKEditor
     */
    @POST
    @Path("/upload")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.TEXT_HTML)
    public String upload(UploadedFile image, @QueryParam("CKEditorFuncNum") Long func,
            @Context UriInfo uriInfo) {
        log.info("upload image " + image.getName());
        try {
            StoredFile file = fileService.storeFile(image, IMAGES);
            String fileName = file.toJSON().getString("file_name");
            String url = uriInfo.getBaseUri() + "filerepo/" + IMAGES + "/" + fileName;
            return getCkEditorCall(func, url, "");
        } catch (Exception e) {
            return getCkEditorCall(func, "", e.getMessage());
        }
    }

    /**
     * Returns a collection of all image source URLs.
     * 
     * @return all image sources
     */
    @GET
    @Path("/browse")
    @Produces(MediaType.APPLICATION_JSON)
    public Collection<Image> browse(@Context UriInfo uriInfo) {
        log.info("browse images");
        try {
            List<Image> images = new ArrayList<Image>();
            for (JSONObject image : getImages()) {
                String path = image.getString("path");
                String src = uriInfo.getBaseUri() + "filerepo" + path;
                images.add(new Image(src));
            }
            return images;
        } catch (WebApplicationException e) { // fileService.getDirectoryListing
            throw e;
        } catch (Exception e) {
            throw new WebApplicationException(e);
        }
    }

    @Override
    public void pluginServiceGone(PluginService service) {
        if (service == fileService) {
            fileService = null;
        }
    }

    /**
     * Reference the file service and create the repository path if necessary.
     */
    @Override
    public void pluginServiceArrived(PluginService service) {
        if (service instanceof FilesService) {
            log.fine("file service arrived");
            fileService = (FilesService) service;
            // TODO move the initialization to migration "0"
            try {
                // check image file repository
                ResourceInfo resourceInfo = fileService.getResourceInfo(IMAGES);
                String kind = resourceInfo.toJSON().getString("kind");
                if (kind.equals("directory") == false) {
                    String repoPath = System.getProperty("dm4.filerepo.path");
                    String message = "image storage directory " + repoPath + File.separator
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
    }

    private String getCkEditorCall(Long func, String uri, String error) {
        return "<script type='text/javascript'>" + "window.parent.CKEDITOR.tools.callFunction("
                + func + ", '" + uri + "', '" + error + "')" + "</script>";
    }

    /**
     * Returns the directory listing of images.
     * 
     * @return images
     * @throws JSONException
     */
    @SuppressWarnings("unchecked")
    private Collection<JSONObject> getImages() throws JSONException {
        DirectoryListing files = fileService.getDirectoryListing(IMAGES);
        JSONArray jsonArray = files.toJSON().getJSONArray("items");
        List<JSONObject> list = DeepaMehtaUtils.toList(jsonArray);
        return list;
    }
}
