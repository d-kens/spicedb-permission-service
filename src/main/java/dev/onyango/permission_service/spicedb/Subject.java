package dev.onyango.permission_service.spicedb;

public record Subject(String type, String id, String optionalRelation) {

    public Subject(String type, String id) {
        this(type, id, null);
    }
}
