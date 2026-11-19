package cl.duoc.pedidos360.admin.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

import java.util.Map;

public record CreateTopicRequest(
        @NotBlank(message = "El nombre del tópico es obligatorio")
        String name,

        @Min(value = 1, message = "Debe tener al menos 1 partición")
        int partitions,

        @Min(value = 1, message = "El factor de replicación debe ser al menos 1")
        short replicationFactor,

        Map<String, String> configs // Ej: retention.ms, cleanup.policy
) {
    public CreateTopicRequest {
        if (partitions <= 0) partitions = 3;
        if (replicationFactor <= 0) replicationFactor = 1;
        if (configs == null) configs = Map.of();
    }
}
