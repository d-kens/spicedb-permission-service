package dev.onyango.permission_service.spicedb;

import com.authzed.api.v1.*;
import com.authzed.grpcutil.BearerToken;
import com.google.protobuf.MessageOrBuilder;
import com.google.protobuf.TextFormat;
import io.grpc.ManagedChannel;
import io.grpc.StatusRuntimeException;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Component
public class SpiceDbClient {
    private static final Logger log = LoggerFactory.getLogger(SpiceDbClient.class);
    private static final long CHECK_DEADLINE_SECONDS = 2;

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

    public boolean checkPermission(Resource resource, String permission, Subject subject) {
        CheckPermissionRequest request = CheckPermissionRequest
                .newBuilder()
                .setConsistency(Consistency.newBuilder().setFullyConsistent(true).build())
                .setResource(ObjectReference.newBuilder().setObjectType(resource.type()).setObjectId(resource.id()).build())
                .setPermission(permission)
                .setSubject(SubjectReference.newBuilder()
                        .setObject(ObjectReference.newBuilder().setObjectType(subject.type()).setObjectId(subject.id()))
                        .setOptionalRelation(subject.optionalRelation() != null ? subject.optionalRelation() : ""))
                .build();

        CheckPermissionResponse response = permissionsServiceStub
                .withDeadlineAfter(CHECK_DEADLINE_SECONDS, TimeUnit.SECONDS)
                .checkPermission(request);

        boolean allowed = response.getPermissionship() == CheckPermissionResponse.Permissionship.PERMISSIONSHIP_HAS_PERMISSION;
        log.debug("Checked {} on {}:{} for {}:{} -> {}",
                permission, resource.type(), resource.id(), subject.type(), subject.id(), response.getPermissionship());

        return allowed;
    }

    public String writeRelationships(Resource resource, String relation, Subject subject) {
        WriteRelationshipsRequest request = WriteRelationshipsRequest.newBuilder()
                .addUpdates(RelationshipUpdate.newBuilder()
                        .setOperation(RelationshipUpdate.Operation.OPERATION_CREATE)
                        .setRelationship(Relationship.newBuilder()
                                .setResource(ObjectReference.newBuilder().setObjectType(resource.type()).setObjectId(resource.id()))
                                .setRelation(relation)
                                .setSubject(SubjectReference.newBuilder()
                                        .setObject(ObjectReference.newBuilder().setObjectType(subject.type()).setObjectId(subject.id()))
                                        .setOptionalRelation(subject.optionalRelation() != null ? subject.optionalRelation() : ""))))
                .build();

        log.debug("WriteRelationships request: {}", toLogString(request));
        try {
            WriteRelationshipsResponse response = permissionsServiceStub.writeRelationships(request);
            log.debug("WriteRelationships response: {}", toLogString(response));
            String token = response.getWrittenAt().getToken();
            log.info("Wrote relationship {}:{}#{}@{}:{} -> zedToken {}",
                    resource.type(), resource.id(), relation, subject.type(), subject.id(), token);
            return token;
        } catch (StatusRuntimeException e) {
            log.warn("Failed to write relationship {}:{}#{}@{}:{} -> {}",
                    resource.type(), resource.id(), relation, subject.type(), subject.id(), e.getStatus());
            throw e;
        }
    }

    /**
     * Renders a protobuf message on a single line, only when debug logging is on.
     */
    private static Object toLogString(MessageOrBuilder message) {
        return log.isDebugEnabled() ? TextFormat.printer().emittingSingleLine(true).printToString(message) : "";
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
