package dev.onyango.permission_service.service;

import dev.onyango.permission_service.spicedb.SchemaDefinition;
import dev.onyango.permission_service.spicedb.SpiceDbClient;
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

    private final SpiceDbClient spiceDbClient;

    public SchemaService(SpiceDbClient spiceDbClient) {
        this.spiceDbClient = spiceDbClient;
    }

    public List<SchemaDefinition> reflectSchema() {
        return spiceDbClient.reflectSchema();
    }

    public void applySchemaFromResources() throws IOException {
        String schema;
        try (InputStream in = new ClassPathResource(SCHEMA_PATH).getInputStream()) {
            schema = new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }

        spiceDbClient.writeSchema(schema);
        log.info("Applied schema from {}", SCHEMA_PATH);
    }
}
