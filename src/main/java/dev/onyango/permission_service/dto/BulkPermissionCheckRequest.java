package dev.onyango.permission_service.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record BulkPermissionCheckRequest(
    @NotBlank(message = "subjectType is required") String subjectType,
    @NotBlank(message = "subjectId is required") String subjectId,
    @NotEmpty(message = "items must not be empty") List<@Valid ResourcePermissionItem> items
) {}
