package dev.onyango.permission_service.dto;

import jakarta.validation.constraints.NotBlank;

public record PermissionCheckRequest(
    @NotBlank(message = "resourceType is required") String resourceType,
    @NotBlank(message = "resourceId is required") String resourceId,
    @NotBlank(message = "permission is required") String permission,
    @NotBlank(message = "subjectType is required") String subjectType,
    @NotBlank(message = "subjectId is required") String subjectId
) {}
