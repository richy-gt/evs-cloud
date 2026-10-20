package cl.duoc.pedidos360.messaging;

public record PedidoCreatedEvent(
        String id,
        String numeroPedido,
        String clienteNombre,
        String clienteEmail,
        Double total,
        String fechaCreacion
) {
}