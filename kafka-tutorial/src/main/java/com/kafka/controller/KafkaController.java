package com.kafka.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controlador REST que envia mensajes a Kafka.
 * Demuestra dos conceptos esenciales:
 * 1. Sin Key: Round Robin (distribuye entre particiones 0, 1, 2...)
 * 2. Con Key: Sticky Partitioning (todos los mensajes con la misma clave van a la misma particion)
 */
@RestController
@RequestMapping("/api/kafka")
public class KafkaController {

    private final KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    public KafkaController(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    /**
     * Endpoint para enviar mensajes a Kafka (soporta GET y POST).
     *
     * Ejemplos de uso:
     *
     * SIN KEY (Round Robin - distribuye entre particiones):
     * GET http://<host>:8888/api/kafka/send?message=Mensaje1
     *
     * CON KEY (Sticky Partitioning - misma particion para la misma key):
     * GET http://<host>:8888/api/kafka/send?message=Paso1-Creada&key=orden-555
     */
    @RequestMapping(value = "/send", method = {RequestMethod.GET, RequestMethod.POST})
    public String sendMessage(
            @RequestParam String message,
            @RequestParam(required = false) String key) {

        String topicName = "tutorial-particiones";

        if (key != null && !key.trim().isEmpty()) {
            // Con Key: Kafka calcula hash(key) para determinar la particion.
            // Todos los mensajes con la misma key van a la MISMA particion garantizando el orden.
            kafkaTemplate.send(topicName, key, message);
            return "Mensaje enviado al topic '" + topicName
                    + "' con KEY: " + key
                    + " | MENSAJE: " + message;
        } else {
            // Sin Key: Kafka usa Round Robin (o Sticky partitioner por batch).
            // Distribuye equitativamente entre las particiones disponibles (0, 1, 2).
            kafkaTemplate.send(topicName, message);
            return "Mensaje enviado al topic '" + topicName
                    + "' (sin key - distribucion automatica) | MENSAJE: " + message;
        }
    }

    /**
     * Endpoint de verificacion de salud
     */
    @GetMapping("/health")
    public String health() {
        return "Aplicación de Kafka Tutorial está corriendo en puerto 8888";
    }
}
