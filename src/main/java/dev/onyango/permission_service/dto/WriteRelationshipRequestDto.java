package dev.onyango.permission_service.dto;


/**
 * Request body for granting or updating access to a resource.
 */
public record WriteRelationshipRequestDto(
        String resourceType,
        String resourceId,
        String relation,
        String subjectType,
        String subjectId,
        String subjectRelation
) {
}
