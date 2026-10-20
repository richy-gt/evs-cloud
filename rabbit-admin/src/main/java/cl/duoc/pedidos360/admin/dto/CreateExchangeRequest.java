package cl.duoc.pedidos360.admin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

import java.util.Map;

public record CreateExchangeRequest(
        @NotBlank(message = "El nombre del exchange es obligatorio") String name,
        @NotBlank(message = "El tipo del exchange es obligatorio")
        @Pattern(regexp = "(?i)direct|topic|fanout|headers", message = "El tipo debe ser direct, topic, fanout o headers")
        String type,
        Boolean durable,
        Boolean autoDelete,
        Boolean internal,
        Map<String, Object> arguments
) {
    public CreateExchangeRequest {
        durable = durable == null || durable;
        autoDelete = autoDelete != null && autoDelete;
        internal = internal != null && internal;
        arguments = arguments == null ? Map.of() : Map.copyOf(arguments);
    }
}