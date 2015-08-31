package de.deepamehta.plugins.images.migrations;

import de.deepamehta.core.service.Inject;
import de.deepamehta.core.service.Migration;
import de.deepamehta.plugins.files.ItemKind;
import de.deepamehta.plugins.files.ResourceInfo;
import de.deepamehta.plugins.files.service.FilesService;
import de.deepamehta.plugins.images.ImagePlugin;
import java.io.File;
import java.util.logging.Logger;
import javax.ws.rs.WebApplicationException;

/**
 *
 * @author <malte@mikromedia.de>
 */
public class Migration1 extends Migration {

    private static final Logger log = Logger.getLogger(Migration1.class.getName());

    @Inject
    FilesService fileService;

    @Override
    public void run() {
        try {
            // check image file repository
            ResourceInfo resourceInfo = fileService.getResourceInfo(ImagePlugin.FILEREPO_IMAGES_SUBFOLDER);
            if (resourceInfo.getItemKind() != ItemKind.DIRECTORY) {
                String message = "Migration1: image storage directory " + ImagePlugin.FILE_REPOSITORY_PATH + File.separator
                        + ImagePlugin.FILEREPO_IMAGES_SUBFOLDER + " can not be used";
                throw new IllegalStateException(message);
            }
        } catch (WebApplicationException e) {
            // catch fileService info request error
            if (e.getResponse().getStatus() != 404) {
                throw e;
            } else {
                log.info("Migration1: create image directory");
                fileService.createFolder(ImagePlugin.FILEREPO_IMAGES_SUBFOLDER, "/");
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
}
