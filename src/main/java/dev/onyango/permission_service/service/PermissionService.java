package dev.onyango.permission_service.service;

import dev.onyango.permission_service.dto.PermissionCheckItem;
import dev.onyango.permission_service.dto.PermissionCheckResultItem;
import dev.onyango.permission_service.spicedb.Resource;
import dev.onyango.permission_service.spicedb.SpiceDbClient;
import dev.onyango.permission_service.dto.PermissionAssignment;
import dev.onyango.permission_service.spicedb.Subject;
import org.springframework.stereotype.Service;

@Service
public class PermissionService {

    private final SpiceDbClient spiceDbClient;

    public PermissionService(SpiceDbClient spiceDbClient) {
        this.spiceDbClient = spiceDbClient;
    }

    public PermissionCheckResultItem checkPermission(PermissionCheckItem item) {
        var resource = new Resource(item.resourceType(), item.resourceId());
        var subject = new Subject(item.subjectType(), item.subjectId(), null);
        boolean authorized = spiceDbClient.checkPermission(resource, item.permission(), subject);
        return new PermissionCheckResultItem(
                item.resourceType(),
                item.resourceId(),
                item.permission(),
                item.subjectType(),
                item.subjectId(),
                authorized
        );
    }

    public String assignPermission(PermissionAssignment request) {
        var resource = new Resource(request.resourceType(), request.resourceId());
        var subject = new Subject(request.subjectType(), request.subjectId(), request.subjectRelation());
        return spiceDbClient.writeRelationships(resource, request.relation(), subject);
    }
}
