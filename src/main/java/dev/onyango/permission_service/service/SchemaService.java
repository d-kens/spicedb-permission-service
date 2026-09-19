package dev.onyango.permission_service.service;

import com.authzed.api.v1.SchemaServiceGrpc;
import com.authzed.api.v1.WriteSchemaRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

@Service
public class SchemaService {
    private static final Logger log = LoggerFactory.getLogger(SchemaService.class);
    private static final String SCHEMA_PATH = "schema/schema.zed";

    private final SchemaServiceGrpc.SchemaServiceBlockingStub schemaStub;

    public SchemaService(SchemaServiceGrpc.SchemaServiceBlockingStub schemaStub) {
        this.schemaStub = schemaStub;
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
