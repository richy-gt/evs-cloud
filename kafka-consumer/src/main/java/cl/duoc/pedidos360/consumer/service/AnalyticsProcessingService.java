package cl.duoc.pedidos360.consumer.service;

import cl.duoc.pedidos360.kafka.dto.PedidoEventKafkaDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.DoubleAdder;

@Service
public class AnalyticsProcessingService {

    private static final Logger log = LoggerFactory.getLogger(AnalyticsProcessingService.class);

    private final AtomicInteger totalEventosProcesados = new AtomicInteger(0);
    private final DoubleAdder montoTotalAcumulado = new DoubleAdder();
    private final ConcurrentHashMap<String, AtomicInteger> eventosPorEstado = new ConcurrentHashMap<>();

    public void procesarEventoPedido(PedidoEventKafkaDto event) {
        if (event == null) {
            throw new IllegalArgumentException("El evento del pedido no puede ser nulo");
        }
        if (event.numeroPedido() == null || event.numeroPedido().isBlank()) {
            throw new IllegalArgumentException("El número de pedido es obligatorio para analítica");
        }
        if (event.total() != null && event.total() < 0) {
            throw new IllegalArgumentException("El monto total del pedido no puede ser negativo");
        }

        totalEventosProcesados.incrementAndGet();
        if (event.total() != null) {
            montoTotalAcumulado.add(event.total());
        }
        String estado = event.estado() != null ? event.estado() : "DESCONOCIDO";
        eventosPorEstado.computeIfAbsent(estado, k -> new AtomicInteger(0)).incrementAndGet();

        int itemsCount = (event.items() != null) ? event.items().size() : 0;

        log.info("📊 [ANALITICA] Procesado evento '{}' para pedido '{}' | Cliente: '{}' | Total: ${} | Items: {} | Ciudad: '{}'",
                event.tipoEvento(), event.numeroPedido(), event.clienteNombre(), event.total(), itemsCount, event.ciudad());
        log.info("📈 [METRICAS STREAMING] Total Eventos: {} | Monto acumulado: ${} | Estado '{}': {}",
                totalEventosProcesados.get(), montoTotalAcumulado.sum(), estado, eventosPorEstado.get(estado).get());
    }

    public int getTotalEventosProcesados() {
        return totalEventosProcesados.get();
    }

    public double getMontoTotalAcumulado() {
        return montoTotalAcumulado.sum();
    }
}
