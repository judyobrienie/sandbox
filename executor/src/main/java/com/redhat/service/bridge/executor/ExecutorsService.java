package com.redhat.service.bridge.executor;

import java.util.concurrent.CompletionStage;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.reactive.messaging.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.redhat.service.bridge.infra.BridgeCloudEventExtension;
import com.redhat.service.bridge.infra.utils.CloudEventUtils;

import io.cloudevents.CloudEvent;
import io.cloudevents.core.provider.ExtensionProvider;
import io.quarkus.runtime.StartupEvent;

@ApplicationScoped
public class ExecutorsService {

    /**
     * Kafka Topic that we expect to have configured for receiving events.
     */
    public static final String EVENTS_IN_TOPIC = "events-in";

    private static final Logger LOG = LoggerFactory.getLogger(ExecutorsService.class);

    @Inject
    ExecutorsProvider executorsProvider;

    public void init(@Observes StartupEvent ev) {
        ExtensionProvider.getInstance().registerExtension(BridgeCloudEventExtension.class, BridgeCloudEventExtension::new);
    }

    @Incoming(EVENTS_IN_TOPIC)
    public CompletionStage<Void> processBridgeEvent(final Message<String> message) {
        try {
            CloudEvent cloudEvent = CloudEventUtils.decode(message.getPayload());
            BridgeCloudEventExtension bridgeCloudEventExtension = ExtensionProvider.getInstance().parseExtension(BridgeCloudEventExtension.class, cloudEvent);
            String bridgeId = bridgeCloudEventExtension.getBridgeId();
            Executor executor = executorsProvider.getExecutor();
            if (executor.getProcessor().getBridgeId().equals(bridgeId)) {
                try {
                    executor.onEvent(cloudEvent);
                } catch (Throwable t) {
                    // Inner Throwable catch is to provide more specific context around which Executor failed to handle the Event, rather than a generic failure
                    LOG.error("Processor with id '{}' on bridge '{}' failed to handle Event. The message is acked anyway.", executor.getProcessor().getId(),
                            executor.getProcessor().getBridgeId(), t);
                }
            }
        } catch (Throwable t) {
            LOG.error("Failed to handle Event received on Bridge. The message is acked anyway.", t);
        }

        return message.ack();
    }
}
