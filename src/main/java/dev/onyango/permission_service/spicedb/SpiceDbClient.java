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
import org.springframework.web.servlet.resource.ResourceTransformer;

import java.util.concurrent.TimeUnit;

@Component
public class SpiceDbClient {
    private static final Logger log = LoggerFactory.getLogger(SpiceDbClient.class);
    private static final long CHECK_DEADLINE_SECONDS = 2;

    private final ManagedChannel spiceDbChannel;
    private final PermissionsServiceGrpc.PermissionsServiceBlockingStub permissionsServiceStub;
    private final ResourceTransformer resourceTransformer;

    public SpiceDbClient(AuthzedConfiguration authzedConfiguration, AuthzedProperties authzedProperties, ResourceTransformer resourceTransformer) {
        this.spiceDbChannel = authzedConfiguration.managedChannel();
        BearerToken credentials = new BearerToken(authzedProperties.getToken());

        this.permissionsServiceStub = PermissionsServiceGrpc.newBlockingStub(spiceDbChannel)
                .withCallCredentials(credentials);
        this.resourceTransformer = resourceTransformer;
    }


    @PreDestroy
    public void shutdown() {
        spiceDbChannel.shutdown();
    }

    public boolean checkPermission(
            String resourceId, String resourceType, String permission, String subjectId, String subjectType
    ) {
        CheckPermissionRequest request = CheckPermissionRequest
                .newBuilder()
                .setConsistency(Consistency.newBuilder().setFullyConsistent(true).build())
                .setResource(ObjectReference.newBuilder().setObjectType(resourceType).setObjectId(resourceId).build())
                .setPermission(permission)
                .setSubject(SubjectReference.newBuilder().setObject(ObjectReference.newBuilder().setObjectType(subjectType).setObjectId(subjectId)))
                .build();

        CheckPermissionResponse response;
        try {
            response = permissionsServiceStub
                    .withDeadlineAfter(CHECK_DEADLINE_SECONDS, TimeUnit.SECONDS)
                    .checkPermission(request);
        } catch (StatusRuntimeException e) {
            throw new SpiceDbException(String.format("Failed to check %s on %s:%s for %s:%s",
                    permission, resourceType, resourceId, subjectType, subjectId), e);
        }

        boolean allowed = response.getPermissionship() == CheckPermissionResponse.Permissionship.PERMISSIONSHIP_HAS_PERMISSION;
        log.debug("Checked {} on {}:{} for {}:{} -> {}",
                permission, resourceType, resourceId, subjectType, subjectId, response.getPermissionship());

        return allowed;
    }

    public String writeRelationships(String resourceId, String resourceType, String relation, String subjectId, String subjectType, String optionalSubjectRelation) {
        WriteRelationshipsRequest request = WriteRelationshipsRequest.newBuilder()
                .addUpdates(RelationshipUpdate.newBuilder()
                        .setOperation(RelationshipUpdate.Operation.OPERATION_CREATE)
                        .setRelationship(Relationship.newBuilder()
                                .setResource(ObjectReference.newBuilder().setObjectType(resourceType).setObjectId(resourceId))
                                .setRelation(relation)
                                .setSubject(SubjectReference.newBuilder()
                                        .setObject(ObjectReference.newBuilder().setObjectType(subjectType).setObjectId(subjectId))
                                        .setOptionalRelation(optionalSubjectRelation != null ? optionalSubjectRelation : ""))))
                .build();

        log.debug("WriteRelationships request: {}", toLogString(request));
        try {
            WriteRelationshipsResponse response = permissionsServiceStub.writeRelationships(request);
            log.debug("WriteRelationships response: {}", toLogString(response));
            String token = response.getWrittenAt().getToken();
            log.info("Wrote relationship {}:{}#{}@{}:{} -> zedToken {}",
                    resourceType, resourceId, relation, subjectType, subjectId, token);
            return token;
        } catch (StatusRuntimeException e) {
            throw new SpiceDbException(String.format("Failed to write relationship %s:%s#%s@%s:%s",
                    resourceType, resourceId, relation, subjectType, subjectId), e);
        }
    }

    /**
     * Renders a protobuf message on a single line, only when debug logging is on.
     */
    private static Object toLogString(MessageOrBuilder message) {
        return log.isDebugEnabled() ? TextFormat.printer().emittingSingleLine(true).printToString(message) : "";
    }
}
