package com.kafka;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Aplicacion Spring Boot para tutorial de Kafka.
 * Demuestra como funcionan topics, particiones y offsets en Apache Kafka.
 */
@SpringBootApplication
public class KafkaTutorialApplication {

    public static void main(String[] args) {
        SpringApplication.run(KafkaTutorialApplication.class, args);
    }
}
