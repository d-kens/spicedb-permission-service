package dev.onyango.permission_service.dto;

import jakarta.validation.constraints.NotBlank;

public record LookupResourcesRequest(
    @NotBlank(message = "resourceType is required") String resourceType,
    @NotBlank(message = "permission is required") String permission,
    @NotBlank(message = "subjectType is required") String subjectType,
    @NotBlank(message = "subjectId is required") String subjectId
) {}
