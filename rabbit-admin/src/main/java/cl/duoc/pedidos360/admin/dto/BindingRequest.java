package cl.duoc.pedidos360.admin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

import java.util.Map;

public record BindingRequest(
        @NotBlank(message = "El destino es obligatorio") String destination,
        @NotBlank(message = "El tipo de destino es obligatorio")
        @Pattern(regexp = "(?i)QUEUE|EXCHANGE", message = "El tipo de destino debe ser QUEUE o EXCHANGE")
        String destinationType,
        @NotBlank(message = "El exchange es obligatorio") String exchange,
        String routingKey,
        Map<String, Object> arguments
) {
    public BindingRequest {
        routingKey = routingKey == null ? "" : routingKey;
        arguments = arguments == null ? Map.of() : Map.copyOf(arguments);
    }
}