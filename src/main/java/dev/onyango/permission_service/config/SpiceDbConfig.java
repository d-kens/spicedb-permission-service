package dev.onyango.permission_service.config;

import com.authzed.api.v1.PermissionsServiceGrpc;
import com.authzed.api.v1.SchemaServiceGrpc;
import com.authzed.grpcutil.BearerToken;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import jakarta.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SpiceDbConfig {

    @Value("${spicedb.target}")
    private String target;

    @Value("${spicedb.token}")
    private String token;

    @Value("${spicedb.use-tls:false}")
    private boolean useTls;

    private ManagedChannel spiceDbChannel;

    /**
     * Opens the actual network connection to SpiceDB.
     * This is the pipe - one connection, reused by every stub below; instead of opening a new connection per call or per sub
     * useTls decides plaintext (local dev) vs TLS (real deployments).
     */
    @Bean
    public ManagedChannel spiceDbChannel() {
        ManagedChannelBuilder<?> builder = ManagedChannelBuilder.forTarget(target);
        spiceDbChannel = (useTls ? builder.useTransportSecurity() : builder.usePlaintext()).build();
        return spiceDbChannel;
    }


    /**
     * Typed client for permission operations: checkPermission, writeRelationships, deleteRelationships, lookupResources, etc.
     * Built on top of a shared channel above.
     */
    @Bean
    public PermissionsServiceGrpc.PermissionsServiceBlockingStub permissionsService(ManagedChannel spiceDbChannel) {
        return PermissionsServiceGrpc.newBlockingStub(spiceDbChannel)
                .withCallCredentials(new BearerToken(token));
    }


    /**
     * Typed client for schema operations: writeSchema, readSchema. Built on the same shared channel.
     */
    @Bean
    public SchemaServiceGrpc.SchemaServiceBlockingStub schemaServiceStub(ManagedChannel spiceDbChannel) {
        return SchemaServiceGrpc.newBlockingStub(spiceDbChannel)
                .withCallCredentials(new BearerToken(token));
    }

    /**
     * Runs when Spring shuts down this bean. Closes the channel cleanly instead of leaving the connection to SpiceDB open and dangling.
     */
    @PreDestroy
    public void shutdown() {
        spiceDbChannel.shutdown();
    }


}
