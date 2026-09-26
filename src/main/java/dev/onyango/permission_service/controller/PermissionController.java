package dev.onyango.permission_service.controller;

import dev.onyango.permission_service.dto.BulkPermissionCheckRequest;
import dev.onyango.permission_service.dto.PermissionCheckRequest;
import dev.onyango.permission_service.dto.PermissionCheckResponse;
import dev.onyango.permission_service.dto.PermissionAssignmentRequest;
import dev.onyango.permission_service.dto.ResourcePermissionResult;
import dev.onyango.permission_service.service.PermissionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;



@RestController
@RequestMapping("/permissions")
@Tag(name = "Permissions", description = "Assign and revoke SpiceDB relationships representing permissions")
public class PermissionController {
    private final PermissionService permissionService;

    public PermissionController(PermissionService permissionService) {
        this.permissionService = permissionService;
    }

    @PostMapping("/check")
    public PermissionCheckResponse checkPermission(
            @Valid @RequestBody final PermissionCheckRequest permissionCheckRequest
    ) {
        return permissionService.checkPermission(permissionCheckRequest);
    }

    @PostMapping("/check/bulk")
    @Operation(summary = "Check permissions in bulk", description = "Checks several resource permissions for one subject in a single call")
    public List<ResourcePermissionResult> checkBulkPermissions(
            @Valid @RequestBody final BulkPermissionCheckRequest bulkPermissionCheckRequest
    ) {
        return permissionService.checkBulkPermissions(bulkPermissionCheckRequest);
    }

    @PostMapping
    @Operation(summary = "Assign permissions", description = "Creates or updates one or more relationships (upsert), written atomically")
    public String assignPermission(@Valid @RequestBody PermissionAssignmentRequest request) {
        return permissionService.assignPermission(request);
    }
}
