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
import java.util.List;

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
    public String name;

    @Required
    public String displayName;

    /**
     * Allows to group multiple content types together
     * (for example to distinguish between content and site related types).
     */
    public String group;

    /**
     * The sort key is only used for sorting in the same group,
     * i.e. when displaying content types in the back-office.
     */
    public String sortkey;

    /**
     * A short description about what this content type should be used for.
     */
    public String description;

    /**
     * The field definition of the content type as JSON.
     */
    @Required
    @MaxSize(20000)
    public String jsonForm;

    // ~~

    public ContentType(String name, String displayName, String jsonForm) {
        this.name = name;
        this.displayName = displayName;
        this.jsonForm = jsonForm;
    }

    // ~~

    public static ContentType findByName(final String name) {
        return ContentType.find("name", name).first();
    }

    public static List<ContentType> findByGroup(final String group) {
        return ContentType.find("group", group).order("sortkey").asList();
    }


    private boolean validateJson() {
        return false; // TODO implement validation
    }

    /**
     * Called by Bootstrap definition.
     */
    public void setJsonFormFromFile(String filename) throws IOException {
        File inputFile = Play.getFile(filename);
        Logger.info("   ~ read form schema definition from %s", inputFile.getAbsolutePath());
        jsonForm = IOUtils.toString(new FileReader(inputFile));
    }

    @Override
    public String toString() {
        return displayName;
    }

}
