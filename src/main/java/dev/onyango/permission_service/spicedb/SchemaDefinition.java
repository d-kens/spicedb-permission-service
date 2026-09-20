package dev.onyango.permission_service.spicedb;

import java.util.List;

public record SchemaDefinition(String name, List<String> relations, List<String> permissions) {
}
