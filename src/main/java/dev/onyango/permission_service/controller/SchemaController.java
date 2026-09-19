package dev.onyango.permission_service.controller;

import com.authzed.api.v1.ReflectSchemaResponse;
import com.authzed.api.v1.ReflectionPermission;
import com.authzed.api.v1.ReflectionRelation;
import dev.onyango.permission_service.dto.SchemaDefinitionDto;
import dev.onyango.permission_service.service.SchemaService;
import io.grpc.StatusRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/schema")
public class SchemaController {
    private static final Logger log = LoggerFactory.getLogger(SchemaController.class);

    private final SchemaService schemaService;

    public SchemaController(SchemaService schemaService) {
        this.schemaService = schemaService;
    }

    @GetMapping("/reflect")
    public List<SchemaDefinitionDto> reflectSchema() {
        return schemaService.reflectSchema();
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
