package dev.onyango.permission_service.spicedb;

import com.authzed.api.v1.CheckBulkPermissionsRequest;
import com.authzed.api.v1.CheckBulkPermissionsRequestItem;
import com.authzed.api.v1.CheckBulkPermissionsResponse;
import com.authzed.api.v1.CheckPermissionRequest;
import com.authzed.api.v1.CheckPermissionResponse;
import com.authzed.api.v1.Consistency;
import com.authzed.api.v1.DeleteRelationshipsRequest;
import com.authzed.api.v1.DeleteRelationshipsResponse;
import com.authzed.api.v1.ObjectReference;
import com.authzed.api.v1.PermissionsServiceGrpc;
import com.authzed.api.v1.Relationship;
import com.authzed.api.v1.RelationshipFilter;
import com.authzed.api.v1.RelationshipUpdate;
import com.authzed.api.v1.SubjectFilter;
import com.authzed.api.v1.SubjectReference;
import com.authzed.api.v1.WriteRelationshipsRequest;
import com.authzed.api.v1.WriteRelationshipsResponse;
import com.authzed.grpcutil.BearerToken;
import com.google.protobuf.MessageOrBuilder;
import com.google.protobuf.TextFormat;
import dev.onyango.permission_service.dto.ResourcePermissionItem;
import dev.onyango.permission_service.dto.ResourcePermissionResult;
import io.grpc.ManagedChannel;
import io.grpc.StatusRuntimeException;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Thin wrapper around the SpiceDB PermissionsService gRPC API.
 * gRPC failures are rethrown as {@link SpiceDbException} so they can be mapped to HTTP responses.
 */
@Component
public class SpiceDbClient {
    private static final Logger log = LoggerFactory.getLogger(SpiceDbClient.class);
    private static final long CHECK_DEADLINE_SECONDS = 2;
    // Longer than a single check since one call evaluates many items
    private static final long BULK_CHECK_DEADLINE_SECONDS = 5;

    private final ManagedChannel spiceDbChannel;
    private final PermissionsServiceGrpc.PermissionsServiceBlockingStub permissionsServiceStub;

    public SpiceDbClient(AuthzedConfiguration authzedConfiguration, AuthzedProperties authzedProperties) {
        this.spiceDbChannel = authzedConfiguration.managedChannel();
        BearerToken credentials = new BearerToken(authzedProperties.getToken());

        this.permissionsServiceStub = PermissionsServiceGrpc.newBlockingStub(spiceDbChannel)
                .withCallCredentials(credentials);
    }


    @PreDestroy
    public void shutdown() {
        spiceDbChannel.shutdown();
    }

    /**
     * Checks whether the subject has the permission on the resource.
     */
    public boolean checkPermission(
            String resourceId, String resourceType, String permission, String subjectId, String subjectType
    ) {
        CheckPermissionRequest request = CheckPermissionRequest
                .newBuilder()
                .setConsistency(Consistency.newBuilder().setFullyConsistent(true).build())
                .setResource(ObjectReference.newBuilder().setObjectType(resourceType).setObjectId(resourceId).build())
                .setPermission(permission)
                .setSubject(SubjectReference.newBuilder().setObject(ObjectReference.newBuilder().setObjectType(subjectType).setObjectId(subjectId)).build())
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


    /**
     * Checks several resource permissions for one subject in a single call.
     * An item that SpiceDB fails to evaluate is returned as not authorized.
     */
    public List<ResourcePermissionResult> checkBulkPermissions(String subjectId, String subjectType, List<ResourcePermissionItem> resourcePermissionItemList) {

        List<CheckBulkPermissionsRequestItem> checkBulkPermissionsRequestItems = new ArrayList<>();

        for (ResourcePermissionItem resourcePermissionItem : resourcePermissionItemList) {
            CheckBulkPermissionsRequestItem checkBulkPermissionsRequestItem = CheckBulkPermissionsRequestItem
                    .newBuilder()
                    .setResource(ObjectReference.newBuilder().setObjectId(resourcePermissionItem.resourceId()).setObjectType(resourcePermissionItem.resourceType()).build())
                    .setPermission(resourcePermissionItem.permission())
                    .setSubject(SubjectReference.newBuilder().setObject(ObjectReference.newBuilder().setObjectId(subjectId).setObjectType(subjectType).build()).build())
                    .build();
            checkBulkPermissionsRequestItems.add(checkBulkPermissionsRequestItem);
        }

        CheckBulkPermissionsRequest checkBulkPermissionsRequest = CheckBulkPermissionsRequest
                .newBuilder()
                .addAllItems(checkBulkPermissionsRequestItems)
                .setConsistency(Consistency.newBuilder().setFullyConsistent(true).build())
                .build();

        CheckBulkPermissionsResponse response;
        try {
            response = permissionsServiceStub
                    .withDeadlineAfter(BULK_CHECK_DEADLINE_SECONDS, TimeUnit.SECONDS)
                    .checkBulkPermissions(checkBulkPermissionsRequest);
        } catch (StatusRuntimeException e) {
            throw new SpiceDbException(String.format("Failed to bulk check %d permissions for %s:%s",
                    checkBulkPermissionsRequestItems.size(), subjectType, subjectId), e);
        }

        log.debug("Bulk checked {} permissions for {}:{}",
                checkBulkPermissionsRequestItems.size(), subjectType, subjectId);

        // Each pair echoes its request item, so results map back without relying on order
        return response.getPairsList()
                .stream()
                .map(pair -> new ResourcePermissionResult(
                        pair.getRequest().getResource().getObjectType(),
                        pair.getRequest().getResource().getObjectId(),
                        pair.getRequest().getPermission(),
                        // A pair holds either a result item or an error; errors count as not authorized
                        pair.hasItem() && pair.getItem().getPermissionship() == CheckPermissionResponse.Permissionship.PERMISSIONSHIP_HAS_PERMISSION
                ))
                .toList();
    }


    /**
     * Creates a relationship between the resource and the subject.
     *
     * @return the ZedToken of the write, usable for read-after-write consistency
     */
    public String writeRelationship(String resourceId, String resourceType, String relation, String subjectId, String subjectType, String optionalSubjectRelation) {
        WriteRelationshipsRequest request = WriteRelationshipsRequest.newBuilder()
                .addUpdates(RelationshipUpdate.newBuilder()
                        .setOperation(RelationshipUpdate.Operation.OPERATION_CREATE)
                        .setRelationship(Relationship.newBuilder()
                                .setResource(ObjectReference.newBuilder().setObjectType(resourceType).setObjectId(resourceId).build())
                                .setRelation(relation)
                                .setSubject(SubjectReference.newBuilder()
                                        .setObject(ObjectReference.newBuilder().setObjectType(subjectType).setObjectId(subjectId).build())
                                        // Proto strings can't be null; empty means no subject relation
                                        .setOptionalRelation(optionalSubjectRelation != null ? optionalSubjectRelation : ""))).build())
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
     * Deletes the relationship between the resource and the subject.
     *
     * @return the ZedToken of the delete, usable for read-after-write consistency
     */
    public String deleteRelationship(String resourceId, String resourceType, String relation, String subjectId, String subjectType) {
        DeleteRelationshipsRequest deleteRelationshipsRequest = DeleteRelationshipsRequest
                .newBuilder()
                .setRelationshipFilter(
                        RelationshipFilter
                                .newBuilder()
                                .setResourceType(resourceType)
                                .setOptionalResourceId(resourceId)
                                .setOptionalRelation(relation)
                                .setOptionalSubjectFilter(SubjectFilter
                                        .newBuilder()
                                        .setSubjectType(subjectType)
                                        .setOptionalSubjectId(subjectId)
                                        .build()
                                )
                                .build()
                )
                .build();

        log.debug("DeleteRelationships request: {}", toLogString(deleteRelationshipsRequest));
        try {
            DeleteRelationshipsResponse response = permissionsServiceStub.deleteRelationships(deleteRelationshipsRequest);
            log.debug("DeleteRelationships response: {}", toLogString(response));
            String token = response.getDeletedAt().getToken();
            log.info("Deleted relationship {}:{}#{}@{}:{} -> zedToken {}",
                    resourceType, resourceId, relation, subjectType, subjectId, token);
            return token;
        } catch (StatusRuntimeException e) {
            throw new SpiceDbException(String.format("Failed to delete relationship %s:%s#%s@%s:%s",
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
