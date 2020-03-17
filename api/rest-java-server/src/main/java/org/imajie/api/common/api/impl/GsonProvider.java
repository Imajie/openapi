package org.imajie.api.common.api.impl;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;
import java.io.*;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

@Provider
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class GsonProvider<T> implements MessageBodyReader<T>, MessageBodyWriter<T> {
    private static final Logger log = LoggerFactory.getLogger(GsonProvider.class);

    private static final Gson GSON = new GsonBuilder()
            .enableComplexMapKeySerialization()
            .create();

    @Override
    public boolean isReadable(final Class<?> aClass, final Type type, final Annotation[] annotations, final MediaType mediaType) {
        return true;
    }

    @Override
    public T readFrom(final Class<T> aClass, final Type type, final Annotation[] annotations, final MediaType mediaType, final MultivaluedMap<String, String> multivaluedMap, final InputStream inputStream) throws IOException, WebApplicationException {
        try (InputStreamReader reader = new InputStreamReader(inputStream, "UTF-8")) {
            return GSON.fromJson(reader, type);
        }
    }

    @Override
    public boolean isWriteable(final Class<?> aClass, final Type type, final Annotation[] annotations, final MediaType mediaType) {
        return true;
    }

    @Override
    public void writeTo(final T t, final Class<?> aClass, final Type type, final Annotation[] annotations, final MediaType mediaType, final MultivaluedMap<String, Object> multivaluedMap, final OutputStream outputStream) throws IOException, WebApplicationException {
        try (OutputStreamWriter writer = new OutputStreamWriter(outputStream)) {
            Type jsonType = aClass.equals(type) ? aClass : type;
            log.info("Serializing to: " + GSON.toJson(t, jsonType));
            GSON.toJson(t, jsonType, writer);
            writer.flush();
        }
    }
}
