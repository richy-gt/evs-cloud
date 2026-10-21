package com.kafka.consumer;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

/**
 * Consumidor de Kafka que escucha el topic 'tutorial-particiones'.
 * Muestra en tiempo real:
 * - Particion: En cual de las 3 particiones llego el mensaje.
 * - Offset: Posicion secuencial unica dentro de esa particion.
 * - Key: La clave del mensaje (null si no se envio).
 * - Mensaje: El contenido (payload).
 */
@Service
public class KafkaConsumer {

    /**
     * Escucha todos los mensajes del topic 'tutorial-particiones' para el grupo 'mi-grupo-educativo'.
     *
     * Los parametros @Header extraen metadatos nativos de Kafka:
     * - RECEIVED_PARTITION: Numero de particion (0, 1 o 2).
     * - OFFSET: Posicion secuencial del mensaje en esa particion.
     * - RECEIVED_KEY: La clave recibida (puede ser null).
     */
    @KafkaListener(topics = "tutorial-particiones", groupId = "mi-grupo-educativo")
    public void listen(
            @Payload String message,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            @Header(value = KafkaHeaders.RECEIVED_KEY, required = false) String key) {

        System.out.println("\n" + "=".repeat(100));
        System.out.println(" MENSAJE RECIBIDO");
        System.out.printf(" 📍 Partición: %d%n", partition);
        System.out.printf(" Offset: %d%n", offset);
        System.out.printf(" Key: %s%n", key != null ? key : "(sin key)");
        System.out.printf(" Mensaje: %s%n", message);
        System.out.println("=".repeat(100) + "\n");
    }
}
