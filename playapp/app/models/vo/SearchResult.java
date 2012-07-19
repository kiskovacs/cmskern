package models.vo;

import com.mongodb.DBObject;

import java.io.Serializable;
import java.util.List;

/**
 * Data container for holding search result.
 *
 * @author Niko Schmuck
 * @since 19.07.2012
 */
public class SearchResult implements Serializable {

    public List<DBObject> objects;
    public int totalCount;

    public SearchResult(List<DBObject> objects, int totalCount) {
        this.objects = objects;
        this.totalCount = totalCount;
    }

}
