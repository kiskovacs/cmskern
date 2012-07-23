package jobs;

import com.mongodb.BasicDBObject;
import com.mongodb.gridfs.GridFS;
import com.mongodb.gridfs.GridFSDBFile;
import com.mongodb.gridfs.GridFSInputFile;
import models.Asset;
import org.bson.types.ObjectId;
import play.Logger;
import play.jobs.Job;
import play.libs.Images;
import utils.MongoDbUtils;

import java.io.File;
import java.io.IOException;

/**
 * Job to asynchronously resize an image to an thumbnail.
 *
 * @author Niko Schmuck
 * @since 27.02.2012
 */
public class Thumbnailer extends Job {

    private ObjectId assetId;

    public Thumbnailer(ObjectId assetId) {
        this.assetId = assetId;
    }
    
    public void doJob() throws IOException {
        Logger.info("Generate thumbnail ...");

        GridFS gfs = MongoDbUtils.getGridFS();
        GridFSDBFile orig = gfs.findOne(assetId);

        // Resizing image physically
        File origFile = File.createTempFile("orig", null);
        orig.writeTo(origFile);
        File targetFile = File.createTempFile("thumb", null);
        Images.resize(origFile, targetFile, Asset.THUMBNAIL_WIDTH, Asset.THUMBNAIL_HEIGHT, true);
        Logger.info("Resized to thumbnail, now saving to DB ...");

        // Saving new thumbnail to the DB
        GridFSInputFile thumb = gfs.createFile(targetFile);
        thumb.setFilename(orig.getFilename());
        thumb.setContentType(orig.getContentType());
        thumb.setMetaData(new BasicDBObject(Asset.ORIGINAL_REF, orig.getId())
                          .append(Asset.THUMBNAIL_FLAG, true));
        thumb.save();
        
        // Referencing from original file to freshly created thumbnail
        orig.setMetaData(new BasicDBObject(Asset.THUMB_REF, thumb.getId()));
        orig.save();

        Logger.info("Finished thumbnail generation for %s (thumb ID: %s).", orig.getFilename(), thumb.getId());
    }

}
