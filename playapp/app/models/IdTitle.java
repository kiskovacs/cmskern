package models;

import java.io.Serializable;

public class IdTitle implements Serializable {

    public final Long id;
    public final String title;

    public IdTitle(final Long id, final String title) {
        this.id = id;
        this.title = title;
    }

}
