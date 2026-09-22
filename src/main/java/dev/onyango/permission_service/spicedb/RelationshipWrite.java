package dev.onyango.permission_service.spicedb;

public record RelationshipWrite(Resource resource, String relation, Subject subject, Operation operation) {

    public enum Operation {
        CREATE_UPDATE,
        DELETE
    }
}
