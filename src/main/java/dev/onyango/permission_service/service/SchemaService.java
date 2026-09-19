package dev.onyango.permission_service.service;

import com.authzed.api.v1.*;
import dev.onyango.permission_service.dto.SchemaDefinitionDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Service
public class SchemaService {
    private static final Logger log = LoggerFactory.getLogger(SchemaService.class);
    private static final String SCHEMA_PATH = "schema/schema.zed";

    private final SchemaServiceGrpc.SchemaServiceBlockingStub schemaStub;

    public SchemaService(SchemaServiceGrpc.SchemaServiceBlockingStub schemaStub) {
        this.schemaStub = schemaStub;
    }

    public List<SchemaDefinitionDto> reflectSchema() {
        ReflectSchemaRequest request = ReflectSchemaRequest.newBuilder()
                // Fully consistent: always read the latest committed schema.
                .setConsistency(Consistency.newBuilder().setFullyConsistent(true).build())
                .build();

        ReflectSchemaResponse response = schemaStub.reflectSchema(request);

        return response.getDefinitionsList().stream()
                .map(def -> new SchemaDefinitionDto(
                        def.getName(),
                        def.getRelationsList().stream().map(ReflectionRelation::getName).toList(),
                        def.getPermissionsList().stream().map(ReflectionPermission::getName).toList()
                ))
                .toList();
    }

    public void applySchemaFromResources() throws IOException {
        String schema;
        try (InputStream in = new ClassPathResource(SCHEMA_PATH).getInputStream()) {
            schema = new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }

        schemaStub.writeSchema(
                WriteSchemaRequest.newBuilder()
                        .setSchema(schema)
                        .build()
        );
        log.info("Applied schema from {}", SCHEMA_PATH);
    }
}
