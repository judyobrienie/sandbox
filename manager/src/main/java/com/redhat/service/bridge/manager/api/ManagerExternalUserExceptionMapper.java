package com.redhat.service.bridge.manager.api;

import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.ext.Provider;

import com.redhat.service.bridge.infra.exceptions.mappers.ExternalUserExceptionMapper;

@Provider
@ApplicationScoped
public class ManagerExternalUserExceptionMapper extends ExternalUserExceptionMapper {
}
