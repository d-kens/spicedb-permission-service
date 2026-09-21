package dev.onyango.permission_service.service;

import dev.onyango.permission_service.spicedb.SpiceDbClient;
import dev.onyango.permission_service.dto.WriteRelationshipRequest;
import org.springframework.stereotype.Service;

@Service
public class PermissionService {

    private final SpiceDbClient spiceDbClient;

    public PermissionService(SpiceDbClient spiceDbClient) {
        this.spiceDbClient = spiceDbClient;
    }

    public String writeRelationship(WriteRelationshipRequest request) {
        return "";
    }
}
