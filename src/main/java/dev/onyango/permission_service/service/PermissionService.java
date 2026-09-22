package dev.onyango.permission_service.service;

import dev.onyango.permission_service.spicedb.RelationshipWrite;
import dev.onyango.permission_service.spicedb.Resource;
import dev.onyango.permission_service.spicedb.SpiceDbClient;
import dev.onyango.permission_service.dto.PermissionAssignment;
import dev.onyango.permission_service.spicedb.Subject;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PermissionService {

    private final SpiceDbClient spiceDbClient;

    public PermissionService(SpiceDbClient spiceDbClient) {
        this.spiceDbClient = spiceDbClient;
    }

    public String assignPermission(List<PermissionAssignment> requests) {
        var writes = requests.stream()
                .map(request -> toWrite(request, RelationshipWrite.Operation.CREATE_UPDATE))
                .toList();
        return spiceDbClient.writeRelationships(writes);
    }

    public String revokePermission(List<PermissionAssignment> requests) {
        var writes = requests.stream()
                .map(request -> toWrite(request, RelationshipWrite.Operation.DELETE))
                .toList();
        return spiceDbClient.writeRelationships(writes);
    }

    private RelationshipWrite toWrite(PermissionAssignment request, RelationshipWrite.Operation operation) {
        var resource = new Resource(request.resourceType(), request.resourceId());
        var subject = new Subject(request.subjectType(), request.subjectId(), request.subjectRelation());
        return new RelationshipWrite(resource, request.relation(), subject, operation);
    }
}
