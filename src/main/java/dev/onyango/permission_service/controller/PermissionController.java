package dev.onyango.permission_service.controller;

import dev.onyango.permission_service.dto.PermissionCheckRequest;
import dev.onyango.permission_service.dto.PermissionCheckResponse;
import dev.onyango.permission_service.dto.PermissionAssignmentRequest;
import dev.onyango.permission_service.service.PermissionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PostMapping;



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

    @PostMapping
    @Operation(summary = "Assign permissions", description = "Creates or updates one or more relationships (upsert), written atomically")
    public String assignPermission(@Valid @RequestBody PermissionAssignmentRequest request) {
        return permissionService.assignPermission(request);
    }
}
