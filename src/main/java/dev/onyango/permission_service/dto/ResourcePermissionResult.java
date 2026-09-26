package dev.onyango.permission_service.dto;

public record ResourcePermissionResult(
        String resourceType,
        String resourceId,
        String permission,
        boolean authorized
) {
}
