package cl.duoc.pedidos360.kafka.dto;

public record PedidoAuditoriaKafkaDto(
        String eventoId,
        String pedidoId,
        String numeroPedido,
        String accion,
        String estadoAnterior,
        String estadoNuevo,
        String usuario,
        String canalVenta,
        Double monto,
        String timestamp
) {}
