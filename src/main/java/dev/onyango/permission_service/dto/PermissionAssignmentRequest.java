package dev.onyango.permission_service.dto;

import jakarta.validation.constraints.NotBlank;

public record PermissionAssignmentRequest(
        @NotBlank(message = "resourceType is required") String resourceType,
        @NotBlank(message = "resourceId is required") String resourceId,
        @NotBlank(message = "relation is required") String relation,
        @NotBlank(message = "subjectType is required") String subjectType,
        @NotBlank(message = "subjectId is required") String subjectId,
        String optionalSubjectRelation
) {
}
