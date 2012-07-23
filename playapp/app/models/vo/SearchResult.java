package models.vo;

import java.io.Serializable;
import java.util.List;

/**
 * Data container for holding search result.
 *
 * @author Niko Schmuck
 * @since 19.07.2012
 */
public class SearchResult<T> implements Serializable {

    public List<T> objects;
    public int totalCount;

    public SearchResult(List<T> objects, int totalCount) {
        this.objects = objects;
        this.totalCount = totalCount;
    }

}
