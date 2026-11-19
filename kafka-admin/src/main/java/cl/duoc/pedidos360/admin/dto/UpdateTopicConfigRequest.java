package cl.duoc.pedidos360.admin.dto;

import jakarta.validation.constraints.NotEmpty;

import java.util.Map;

public record UpdateTopicConfigRequest(
        @NotEmpty(message = "Debe proporcionar al menos una configuración a actualizar")
        Map<String, String> configs
) {}
