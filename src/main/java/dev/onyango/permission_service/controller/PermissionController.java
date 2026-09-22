package dev.onyango.permission_service.controller;


import dev.onyango.permission_service.dto.PermissionAssignment;
import dev.onyango.permission_service.service.PermissionService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/permissions")
public class PermissionController {
    private final PermissionService permissionService;

    public PermissionController(PermissionService permissionService) {
        this.permissionService = permissionService;
    }

    @PostMapping
    public String assignPermission(@Valid @RequestBody List<PermissionAssignment> requests) {
        return permissionService.assignPermission(requests);
    }

    @DeleteMapping
    public String revokePermission(@Valid @RequestBody List<PermissionAssignment> requests) {
        return permissionService.revokePermission(requests);
    }
}
