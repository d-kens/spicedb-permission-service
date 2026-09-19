package dev.onyango.permission_service.controller;

import dev.onyango.permission_service.service.SchemaService;
import io.grpc.StatusRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequestMapping("/schema")
public class SchemaController {
    private static final Logger log = LoggerFactory.getLogger(SchemaController.class);

    private final SchemaService schemaService;

    public SchemaController(SchemaService schemaService) {
        this.schemaService = schemaService;
    }

    @PostMapping
    public ResponseEntity<String> updateSchema() {
        try {
            schemaService.applySchemaFromResources();
            return ResponseEntity.ok("Schema applied");
        } catch (IOException e) {
            log.error("Failed to read schema resource", e);
            return ResponseEntity.internalServerError().body("Could not read schema file");
        } catch (StatusRuntimeException e) {
            log.error("SpiceDB rejected schema write: {}", e.getStatus(), e);
            return ResponseEntity.status(502).body("SpiceDB rejected the schema: " + e.getStatus().getDescription());
        }
    }
}
