package dev.onyango.permission_service.dto;

import java.util.List;

public class SchemaDefinitionDto {

    private String name;
    private List<String> relations;
    private List<String> permissions;

    public SchemaDefinitionDto(String name, List<String> relations, List<String> permissions) {
        this.name = name;
        this.relations = relations;
        this.permissions = permissions;
    }

    public String getName() {
        return name;
    }

    public List<String> getRelations() {
        return relations;
    }

    public List<String> getPermissions() {
        return permissions;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setRelations(List<String> relations) {
        this.relations = relations;
    }

    public void setPermissions(List<String> permissions) {
        this.permissions = permissions;
    }
}
