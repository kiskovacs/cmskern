package models.vo;

import java.io.Serializable;

/**
 * Simple value object for holding referenced ID and a title,
 * instances are used for example for saving bandwidth resources
 * when transporting data for auto-complete fields.
 */
public class IdTitle implements Serializable {

    public final Long id;
    public final String title;

    public IdTitle(final Long id, final String title) {
        this.id = id;
        this.title = title;
    }

}
