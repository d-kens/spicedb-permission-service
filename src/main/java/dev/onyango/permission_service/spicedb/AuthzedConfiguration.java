package dev.onyango.permission_service.spicedb;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AuthzedConfiguration {

    private static final Logger log = LoggerFactory.getLogger(AuthzedConfiguration.class);

    private final AuthzedProperties authzedProperties;

    public AuthzedConfiguration(AuthzedProperties authzedProperties) {
        this.authzedProperties = authzedProperties;
    }

    /**
     * Builds a gRPC Managed Channel connected to Authzed SpiceDB.
     */
    public ManagedChannel managedChannel() {
        log.info("Initializing gRPC channel with Authzed");

        log.debug("Building managed channel pointing to Authzed at '{}'", authzedProperties.getTarget());

        var managedChannel = ManagedChannelBuilder
                .forTarget(authzedProperties.getTarget())
                .usePlaintext()
                .build();

        log.debug("Managed channel built, current connectivity state: {}", managedChannel.getState(false));

        log.info("Completed Authzed gRPC channel initialization");
        return managedChannel;
    }
}
