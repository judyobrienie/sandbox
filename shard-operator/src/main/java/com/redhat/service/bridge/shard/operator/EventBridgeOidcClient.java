package com.redhat.service.bridge.shard.operator;

import java.util.HashMap;
import java.util.Map;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import com.redhat.service.bridge.infra.auth.AbstractOidcClient;
import com.redhat.service.bridge.infra.auth.OidcClientConfigUtils;

import io.quarkus.oidc.client.OidcClientConfig;
import io.quarkus.oidc.client.OidcClients;
import io.quarkus.oidc.common.runtime.OidcConstants;
import io.quarkus.scheduler.Scheduled;

@ApplicationScoped
public class EventBridgeOidcClient extends AbstractOidcClient {

    private static final String NAME = "event-bridge-sso";

    @ConfigProperty(name = "event-bridge.sso.auth-server-url")
    String serverUrl;

    @ConfigProperty(name = "event-bridge.sso.client-id")
    String clientId;

    @ConfigProperty(name = "event-bridge.sso.credentials.secret")
    String secret;

    @ConfigProperty(name = "event-bridge.sso.grant.type")
    String type;

    @ConfigProperty(name = "event-bridge.sso.grant-options.password.username")
    String username;

    @ConfigProperty(name = "event-bridge.sso.grant-options.password.password")
    String password;

    @Inject
    public EventBridgeOidcClient(OidcClients oidcClients) {
        super(NAME, oidcClients);
    }

    @Override
    protected OidcClientConfig getOidcClientConfig() {
        Map<String, Map<String, String>> grantOptions = new HashMap<>();
        Map<String, String> passwordConfig = new HashMap<>();
        passwordConfig.put(OidcConstants.PASSWORD_GRANT_USERNAME, username);
        passwordConfig.put(OidcConstants.PASSWORD_GRANT_PASSWORD, password);
        grantOptions.put(OidcConstants.PASSWORD_GRANT, passwordConfig);

        OidcClientConfig oidcClientConfig = new OidcClientConfig();
        oidcClientConfig.setId(NAME);
        oidcClientConfig.setAuthServerUrl(serverUrl);
        oidcClientConfig.setClientId(clientId);
        oidcClientConfig.grant.setType(OidcClientConfigUtils.getGrantType(type));
        oidcClientConfig.setGrantOptions(grantOptions);
        oidcClientConfig.getCredentials().setSecret(secret);
        oidcClientConfig.setRefreshTokenTimeSkew(AbstractOidcClient.REFRESH_TOKEN_TIME_SKEW);

        return oidcClientConfig;
    }

    @Override
    @Scheduled(every = AbstractOidcClient.SCHEDULER_TIME)
    protected void scheduledLoop() {
        super.checkAndRefresh();
    }
}
