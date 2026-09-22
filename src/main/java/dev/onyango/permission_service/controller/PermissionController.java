package dev.onyango.permission_service.controller;


import dev.onyango.permission_service.dto.PermissionAssignment;
import dev.onyango.permission_service.service.PermissionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/permissions")
@Tag(name = "Permissions", description = "Assign and revoke SpiceDB relationships representing permissions")
public class PermissionController {
    private final PermissionService permissionService;

    public PermissionController(PermissionService permissionService) {
        this.permissionService = permissionService;
    }

    @PostMapping
    @Operation(summary = "Assign permissions", description = "Creates or updates one or more relationships (upsert), written atomically")
    public String assignPermission(@Valid @RequestBody List<PermissionAssignment> requests) {
        return permissionService.assignPermission(requests);
    }

    @DeleteMapping
    @Operation(summary = "Revoke permissions", description = "Deletes one or more relationships, written atomically")
    public String revokePermission(@Valid @RequestBody List<PermissionAssignment> requests) {
        return permissionService.revokePermission(requests);
    }
}
