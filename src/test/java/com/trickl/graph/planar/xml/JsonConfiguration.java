package com.trickl.graph.planar.xml;

import com.fasterxml.jackson.databind.AnnotationIntrospector;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.module.jaxb.JaxbAnnotationIntrospector;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JsonConfiguration {
    
    @Bean
    public ObjectMapper getObjectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        AnnotationIntrospector annotationIntrospector =
                new JaxbAnnotationIntrospector(TypeFactory.defaultInstance());
        objectMapper.setAnnotationIntrospector(annotationIntrospector);
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        
        return objectMapper;
    }
}
