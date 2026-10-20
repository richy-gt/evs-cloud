package cl.duoc.pedidos360.admin.dto;

import jakarta.validation.constraints.NotBlank;

import java.util.Map;

public record CreateQueueRequest(
        @NotBlank(message = "El nombre de la cola es obligatorio") String name,
        Boolean durable,
        Boolean exclusive,
        Boolean autoDelete,
        Map<String, Object> arguments
) {
    public CreateQueueRequest {
        durable = durable == null || durable;
        exclusive = exclusive != null && exclusive;
        autoDelete = autoDelete != null && autoDelete;
        arguments = arguments == null ? Map.of() : Map.copyOf(arguments);
    }
}