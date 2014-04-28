package com.softmotions.ncms.jaxrs;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;

import javax.ws.rs.Consumes;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

/**
 * @author Adamansky Anton (adamansky@gmail.com)
 */
@Provider
@Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_FORM_URLENCODED})
public class NcmsJsonNodeReader implements MessageBodyReader<JsonNode> {

    final ObjectMapper mapper;

    @Inject
    public NcmsJsonNodeReader(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    public boolean isReadable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        //noinspection ObjectEquality
        return (type == JsonNode.class);
    }

    public JsonNode readFrom(Class<JsonNode> type, Type genericType,
                             Annotation[] annotations,
                             MediaType mediaType,
                             MultivaluedMap<String, String> httpHeaders,
                             InputStream entityStream) throws IOException, WebApplicationException {
        return mapper.readTree(entityStream);
    }
}