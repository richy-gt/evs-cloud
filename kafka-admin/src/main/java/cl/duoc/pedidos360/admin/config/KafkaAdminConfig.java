package cl.duoc.pedidos360.admin.config;

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.KafkaAdmin;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaAdminConfig {

    @Value("${spring.kafka.bootstrap-servers:localhost:9092}")
    private String bootstrapServers;

    @Value("${app.kafka.topics.pedidos-stream.name:pedidos.streaming.eventos}")
    private String streamTopicName;

    @Value("${app.kafka.topics.pedidos-audit.name:pedidos.auditoria.log}")
    private String auditTopicName;

    @Bean
    public KafkaAdmin kafkaAdmin() {
        Map<String, Object> configs = new HashMap<>();
        configs.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        return new KafkaAdmin(configs);
    }

    @Bean
    public AdminClient adminClient() {
        Map<String, Object> configs = new HashMap<>();
        configs.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        return AdminClient.create(configs);
    }

    // Topico 1: Streaming de eventos de pedidos con retencion de 7 dias y particionado
    @Bean
    public NewTopic pedidosStreamTopic() {
        return TopicBuilder.name(streamTopicName)
                .partitions(3)
                .replicas(1)
                .config("retention.ms", "604800000") // 7 dias
                .config("cleanup.policy", "delete")
                .build();
    }

    // Topico 2: Auditoria operacional con retencion extendida de 30 dias
    @Bean
    public NewTopic pedidosAuditTopic() {
        return TopicBuilder.name(auditTopicName)
                .partitions(3)
                .replicas(1)
                .config("retention.ms", "2592000000") // 30 dias
                .config("cleanup.policy", "delete")
                .build();
    }

    // DLT para streaming
    @Bean
    public NewTopic pedidosStreamDltTopic() {
        return TopicBuilder.name(streamTopicName + ".DLT")
                .partitions(3)
                .replicas(1)
                .build();
    }

    // DLT para auditoria
    @Bean
    public NewTopic pedidosAuditDltTopic() {
        return TopicBuilder.name(auditTopicName + ".DLT")
                .partitions(3)
                .replicas(1)
                .build();
    }
}
