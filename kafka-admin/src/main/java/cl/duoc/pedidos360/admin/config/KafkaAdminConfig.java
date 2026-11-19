package cl.duoc.pedidos360.admin.config;

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.KafkaAdmin;

import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableConfigurationProperties(KafkaAdminConfig.KafkaTopicsProperties.class)
public class KafkaAdminConfig {

    @ConfigurationProperties(prefix = "app.kafka")
    public record KafkaTopicsProperties(Map<String, TopicSpec> topics) {
        public record TopicSpec(String name, int partitions, short replicationFactor, Map<String, String> configs) {}
    }

    @Value("${spring.kafka.bootstrap-servers:localhost:9092}")
    private String bootstrapServers;

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

    @Bean
    public KafkaAdmin.NewTopics pedidosTopics(KafkaTopicsProperties props) {
        return new KafkaAdmin.NewTopics(props.topics().values().stream()
                .map(t -> TopicBuilder.name(t.name())
                        .partitions(t.partitions())
                        .replicas(t.replicationFactor())
                        .configs(t.configs() == null ? Map.of() : t.configs())
                        .build())
                .toArray(NewTopic[]::new));
    }
}
