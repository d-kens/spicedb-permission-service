package dev.onyango.permission_service.service;

import dev.onyango.permission_service.dto.BulkPermissionCheckRequest;
import dev.onyango.permission_service.dto.PermissionCheckRequest;
import dev.onyango.permission_service.dto.PermissionCheckResponse;
import dev.onyango.permission_service.spicedb.SpiceDbClient;
import dev.onyango.permission_service.dto.PermissionAssignmentRequest;
import dev.onyango.permission_service.dto.ResourcePermissionResult;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PermissionService {

    private final SpiceDbClient spiceDbClient;

    public PermissionService(SpiceDbClient spiceDbClient) {
        this.spiceDbClient = spiceDbClient;
    }

    public PermissionCheckResponse checkPermission(PermissionCheckRequest permissionCheckRequest) {


        boolean authorized = spiceDbClient.checkPermission(
                permissionCheckRequest.resourceId(),
                permissionCheckRequest.resourceType(),
                permissionCheckRequest.permission(),
                permissionCheckRequest.subjectId(),
                permissionCheckRequest.subjectType()
        );

        return new PermissionCheckResponse(
                permissionCheckRequest.resourceType(),
                permissionCheckRequest.resourceId(),
                permissionCheckRequest.permission(),
                permissionCheckRequest.subjectType(),
                permissionCheckRequest.subjectId(),
                authorized
        );
    }

    public List<ResourcePermissionResult> checkBulkPermissions(BulkPermissionCheckRequest bulkPermissionCheckRequest) {
        return spiceDbClient.checkBulkPermissions(
                bulkPermissionCheckRequest.subjectId(),
                bulkPermissionCheckRequest.subjectType(),
                bulkPermissionCheckRequest.items()
        );
    }

    public String assignPermission(PermissionAssignmentRequest permissionAssignmentRequest) {
        return spiceDbClient.writeRelationships(
                permissionAssignmentRequest.resourceId(),
                permissionAssignmentRequest.resourceType(),
                permissionAssignmentRequest.relation(),
                permissionAssignmentRequest.subjectId(),
                permissionAssignmentRequest.subjectType(),
                permissionAssignmentRequest.optionalSubjectRelation()
        );
    }
}
