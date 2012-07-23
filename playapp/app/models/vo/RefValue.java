package models.vo;

import java.io.Serializable;

/**
 * Holds data allowing to refer to a fully qualified (inclusive array position) model
 * name on the client side, which are used inside the call-outs.
 *
 * @author Niko Schmuck
 * @since 25.06.2012
 */
public class RefValue implements Serializable {

    public String targetFQName;
    public Object value;

    public RefValue(String targetFQName, Object value) {
        this.value = value;
        this.targetFQName = targetFQName;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("RefValue");
        sb.append("{targetFQName='").append(targetFQName).append('\'');
        sb.append(", value='").append(value).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
