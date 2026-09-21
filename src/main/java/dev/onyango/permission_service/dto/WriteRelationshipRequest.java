package dev.onyango.permission_service.dto;


public record WriteRelationshipRequest(
        String resourceType,
        String resourceId,
        String relation,
        String subjectType,
        String subjectId,
        String subjectRelation
) {
}
