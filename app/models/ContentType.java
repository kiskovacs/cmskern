package models;

import com.google.code.morphia.annotations.Entity;
import com.google.code.morphia.annotations.Indexed;
import org.apache.commons.io.IOUtils;
import play.Logger;
import play.Play;
import play.data.validation.MaxSize;
import play.data.validation.Required;
import play.modules.morphia.Model;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;

/**
 * The type model class which stores one content type definition
 * in a JSON blueprint which is used to construct the form to
 * create and update concrete content nodes.
 *
 * @author Niko Schmuck
 * @since 21.01.2012
 */
@Entity(value = "contentTypes", noClassnameStored = true)
@Model.AutoTimestamp
public class ContentType extends Model {

    @Required
    @Indexed(unique = true)
    public String slug;

    @Required
    public String displayName;
    
    public String description;
    
    @Required
    @MaxSize(20000)
    public String jsonForm;

    // ~~

    public ContentType(String slug, String displayName, String jsonForm) {
        this.slug = slug;
        this.displayName = displayName;
        this.jsonForm = jsonForm;
    }

    // ~~

    public static ContentType findByName(String slug) {
        return ContentType.find("slug", slug).first();
    }
    
    private boolean validateJson() {
        return false; // TODO implement validation
    }

    public void setJsonFormFromFile(String filename) throws IOException {
        File inputFile = Play.getFile(filename);
        Logger.info("Read JSON Form from %s", inputFile.getAbsolutePath());
        jsonForm = IOUtils.toString(new FileReader(inputFile));
    }

    @Override
    public String toString() {
        return displayName;
    }

}
