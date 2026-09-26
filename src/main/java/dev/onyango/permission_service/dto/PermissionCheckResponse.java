package dev.onyango.permission_service.dto;

public record PermissionCheckResponse(
        String resourceType,
        String resourceId,
        String permission,
        String subjectType,
        String subjectId,
        boolean authorized
) {
}
