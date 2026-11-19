package cl.duoc.pedidos360.kafka.dto;

import java.util.List;

public record PedidoEventKafkaDto(
        String id,
        String numeroPedido,
        String clienteNombre,
        String clienteEmail,
        String clienteTelefono,
        String direccionEnvio,
        String ciudad,
        String fechaCreacion,
        String fechaEntregaEstimada,
        String estado,
        String prioridad,
        String canalVenta,
        Double total,
        String tipoEvento,
        String timestamp,
        List<ItemKafkaDto> items
) {
    public record ItemKafkaDto(
            String id,
            String producto,
            String sku,
            Integer cantidad,
            Double precioUnitario,
            Double subtotal
    ) {}
}
