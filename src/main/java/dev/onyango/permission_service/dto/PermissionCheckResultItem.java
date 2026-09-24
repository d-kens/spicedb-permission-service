package dev.onyango.permission_service.dto;

public record PermissionCheckResultItem(
        String resourceType,
        String resourceId,
        String permission,
        String subjectType,
        String subjectId,
        boolean authorized
) {
}
