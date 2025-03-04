package com.redhat.service.bridge.external.ansiblegateway;

import javax.inject.Singleton;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.cloudevents.jackson.JsonFormat;

@Singleton
public class ObjectMapperCustomizer implements io.quarkus.jackson.ObjectMapperCustomizer {

    public void customize(ObjectMapper mapper) {
        mapper.registerModule(JsonFormat.getCloudEventJacksonModule());
    }
}