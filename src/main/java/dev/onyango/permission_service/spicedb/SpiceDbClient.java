package dev.onyango.permission_service.spicedb;

import com.authzed.api.v1.Consistency;
import com.authzed.api.v1.PermissionsServiceGrpc;
import com.authzed.api.v1.ReflectSchemaRequest;
import com.authzed.api.v1.ReflectSchemaResponse;
import com.authzed.api.v1.ReflectionPermission;
import com.authzed.api.v1.ReflectionRelation;
import com.authzed.api.v1.SchemaServiceGrpc;
import com.authzed.api.v1.WriteSchemaRequest;
import com.authzed.grpcutil.BearerToken;
import io.grpc.ManagedChannel;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class SpiceDbClient {
    private static final Logger log = LoggerFactory.getLogger(SpiceDbClient.class);

    private final ManagedChannel spiceDbChannel;
    private final PermissionsServiceGrpc.PermissionsServiceBlockingStub permissionsServiceStub;
    private final SchemaServiceGrpc.SchemaServiceBlockingStub schemaServiceStub;

    public SpiceDbClient(AuthzedConfiguration authzedConfiguration, AuthzedProperties authzedProperties) {
        this.spiceDbChannel = authzedConfiguration.managedChannel();
        BearerToken credentials = new BearerToken(authzedProperties.getToken());

        this.permissionsServiceStub = PermissionsServiceGrpc.newBlockingStub(spiceDbChannel)
                .withCallCredentials(credentials);
        this.schemaServiceStub = SchemaServiceGrpc.newBlockingStub(spiceDbChannel)
                .withCallCredentials(credentials);
    }

    /**
     * Closes the channel cleanly on shutdown instead of leaving the connection to SpiceDB open and dangling.
     */
    @PreDestroy
    public void shutdown() {
        spiceDbChannel.shutdown();
    }

    public String writeRelationship(Resource resource, String relation, Subject subject) {
        return "";
    }

    public List<SchemaDefinition> reflectSchema() {
        ReflectSchemaRequest request = ReflectSchemaRequest.newBuilder()
                // Fully consistent: always read the latest committed schema.
                .setConsistency(Consistency.newBuilder().setFullyConsistent(true).build())
                .build();

        ReflectSchemaResponse response = schemaServiceStub.reflectSchema(request);

        return response.getDefinitionsList().stream()
                .map(def -> new SchemaDefinition(
                        def.getName(),
                        def.getRelationsList().stream().map(ReflectionRelation::getName).toList(),
                        def.getPermissionsList().stream().map(ReflectionPermission::getName).toList()
                ))
                .toList();
    }

    public void writeSchema(String schema) {
        schemaServiceStub.writeSchema(
                WriteSchemaRequest.newBuilder()
                        .setSchema(schema)
                        .build()
        );
        log.info("Wrote schema to SpiceDB");
    }
}
