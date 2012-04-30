package utils;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import play.data.binding.Global;
import play.data.binding.TypeBinder;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

/**
 * Allows a Play! controller to bind a JSON representation
 * to a POJO.
 *
 * @author Niko Schmuck
 * @since 20.01.2012
 */
@Global
public class JsonObjectBinder implements TypeBinder<JsonObject> {

    @Override
    public Object bind(String name, Annotation[] annotations,
                       String value, Class actualClass, Type genericType) throws Exception {
        return new JsonParser().parse(value);
    }

}

