package dev.onyango.permission_service.dto;

import jakarta.validation.constraints.NotBlank;

public record ResourcePermissionItem(
        @NotBlank(message = "resourceId is required") String resourceId,
        @NotBlank(message = "resourceType is required") String resourceType,
        @NotBlank(message = "permission is required") String permission
) {
}
