package fsu.jportal.gson;

import java.lang.reflect.Type;

import com.google.gson.JsonDeserializer;
import com.google.gson.JsonSerializer;

public abstract class GsonTypeAdapter<T> implements JsonSerializer<T>, JsonDeserializer<T> {
    public abstract Type bindTo();
}
