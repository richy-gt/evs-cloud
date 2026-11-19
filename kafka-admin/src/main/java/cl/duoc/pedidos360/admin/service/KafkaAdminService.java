package cl.duoc.pedidos360.admin.service;

import cl.duoc.pedidos360.admin.dto.*;
import org.apache.kafka.clients.admin.*;
import org.apache.kafka.common.ConsumerGroupState;
import org.apache.kafka.common.KafkaFuture;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.TopicPartitionInfo;
import org.apache.kafka.common.config.ConfigResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

@Service
public class KafkaAdminService {

    private static final Logger log = LoggerFactory.getLogger(KafkaAdminService.class);
    private static final int TIMEOUT_SECONDS = 10;

    private final AdminClient adminClient;

    public KafkaAdminService(AdminClient adminClient) {
        this.adminClient = adminClient;
    }

    public List<TopicSummaryResponse> listTopics() throws ExecutionException, InterruptedException, TimeoutException {
        ListTopicsResult listTopicsResult = adminClient.listTopics(new ListTopicsOptions().listInternal(true));
        Map<String, TopicListing> listings = listTopicsResult.namesToListings().get(TIMEOUT_SECONDS, TimeUnit.SECONDS);

        DescribeTopicsResult describeResult = adminClient.describeTopics(listings.keySet());
        Map<String, TopicDescription> descriptions = describeResult.allTopicNames().get(TIMEOUT_SECONDS, TimeUnit.SECONDS);

        return descriptions.values().stream().map(desc -> {
            int replicationFactor = desc.partitions().isEmpty() ? 0 : desc.partitions().get(0).replicas().size();
            return new TopicSummaryResponse(
                    desc.name(),
                    desc.partitions().size(),
                    replicationFactor,
                    desc.isInternal()
            );
        }).sorted(Comparator.comparing(TopicSummaryResponse::name)).collect(Collectors.toList());
    }

    public TopicDetailResponse getTopicDetail(String topicName) throws ExecutionException, InterruptedException, TimeoutException {
        DescribeTopicsResult descResult = adminClient.describeTopics(Collections.singletonList(topicName));
        TopicDescription desc = descResult.allTopicNames().get(TIMEOUT_SECONDS, TimeUnit.SECONDS).get(topicName);
        if (desc == null) {
            throw new NoSuchElementException("Tópico no encontrado: " + topicName);
        }

        ConfigResource resource = new ConfigResource(ConfigResource.Type.TOPIC, topicName);
        DescribeConfigsResult configResult = adminClient.describeConfigs(Collections.singleton(resource));
        org.apache.kafka.clients.admin.Config config = configResult.all().get(TIMEOUT_SECONDS, TimeUnit.SECONDS).get(resource);

        Map<String, String> configMap = new HashMap<>();
        if (config != null) {
            config.entries().forEach(entry -> {
                if (!entry.isDefault()) {
                    configMap.put(entry.name(), entry.value());
                }
            });
            // Asegurar que siempre se expongan configuraciones clave evaluadas
            if (!configMap.containsKey("retention.ms") && config.get("retention.ms") != null) {
                configMap.put("retention.ms", config.get("retention.ms").value());
            }
            if (!configMap.containsKey("cleanup.policy") && config.get("cleanup.policy") != null) {
                configMap.put("cleanup.policy", config.get("cleanup.policy").value());
            }
        }

        List<TopicDetailResponse.PartitionInfo> partitionInfos = desc.partitions().stream().map(p ->
                new TopicDetailResponse.PartitionInfo(
                        p.partition(),
                        p.leader() != null ? p.leader().id() : -1,
                        p.replicas().stream().map(org.apache.kafka.common.Node::id).collect(Collectors.toList()),
                        p.isr().stream().map(org.apache.kafka.common.Node::id).collect(Collectors.toList())
                )
        ).collect(Collectors.toList());

        int replicationFactor = desc.partitions().isEmpty() ? 0 : desc.partitions().get(0).replicas().size();

        return new TopicDetailResponse(
                desc.name(),
                desc.partitions().size(),
                replicationFactor,
                desc.isInternal(),
                configMap,
                partitionInfos
        );
    }

    public void createTopic(CreateTopicRequest request) throws ExecutionException, InterruptedException, TimeoutException {
        NewTopic newTopic = new NewTopic(request.name(), request.partitions(), request.replicationFactor());
        if (request.configs() != null && !request.configs().isEmpty()) {
            newTopic.configs(request.configs());
        }

        adminClient.createTopics(Collections.singletonList(newTopic)).all().get(TIMEOUT_SECONDS, TimeUnit.SECONDS);
        log.info("Tópico Kafka '{}' creado exitosamente con {} particiones y replicación {}",
                request.name(), request.partitions(), request.replicationFactor());
    }

    public void updateTopicConfigs(String topicName, Map<String, String> configs) throws ExecutionException, InterruptedException, TimeoutException {
        ConfigResource resource = new ConfigResource(ConfigResource.Type.TOPIC, topicName);
        Collection<AlterConfigOp> ops = configs.entrySet().stream()
                .map(e -> new AlterConfigOp(new ConfigEntry(e.getKey(), e.getValue()), AlterConfigOp.OpType.SET))
                .collect(Collectors.toList());

        adminClient.incrementalAlterConfigs(Collections.singletonMap(resource, ops))
                .all().get(TIMEOUT_SECONDS, TimeUnit.SECONDS);
        log.info("Configuraciones del tópico '{}' actualizadas: {}", topicName, configs);
    }

    public void updateTopicPartitions(String topicName, int totalPartitions) throws ExecutionException, InterruptedException, TimeoutException {
        adminClient.createPartitions(Collections.singletonMap(topicName, NewPartitions.increaseTo(totalPartitions)))
                .all().get(TIMEOUT_SECONDS, TimeUnit.SECONDS);
        log.info("Particiones del tópico '{}' incrementadas a {}", topicName, totalPartitions);
    }

    public void deleteTopic(String topicName) throws ExecutionException, InterruptedException, TimeoutException {
        adminClient.deleteTopics(Collections.singletonList(topicName)).all().get(TIMEOUT_SECONDS, TimeUnit.SECONDS);
        log.info("Tópico Kafka '{}' eliminado exitosamente", topicName);
    }

    public List<ConsumerGroupSummaryResponse> listConsumerGroups() throws ExecutionException, InterruptedException, TimeoutException {
        Collection<ConsumerGroupListing> listings = adminClient.listConsumerGroups().all().get(TIMEOUT_SECONDS, TimeUnit.SECONDS);
        return listings.stream().map(g -> new ConsumerGroupSummaryResponse(
                g.groupId(),
                g.isSimpleConsumerGroup(),
                g.state().map(Enum::name).orElse("UNKNOWN")
        )).sorted(Comparator.comparing(ConsumerGroupSummaryResponse::groupId)).collect(Collectors.toList());
    }

    public ConsumerGroupDetailResponse getConsumerGroupDetail(String groupId) throws ExecutionException, InterruptedException, TimeoutException {
        DescribeConsumerGroupsResult descResult = adminClient.describeConsumerGroups(Collections.singletonList(groupId));
        ConsumerGroupDescription desc = descResult.all().get(TIMEOUT_SECONDS, TimeUnit.SECONDS).get(groupId);
        if (desc == null) {
            throw new NoSuchElementException("Consumer Group no encontrado: " + groupId);
        }

        List<ConsumerGroupDetailResponse.MemberInfo> members = desc.members().stream().map(m ->
                new ConsumerGroupDetailResponse.MemberInfo(
                        m.consumerId(),
                        m.clientId(),
                        m.host(),
                        m.assignment() != null ? m.assignment().topicPartitions().stream()
                                .map(tp -> tp.topic() + "-" + tp.partition())
                                .collect(Collectors.toList()) : Collections.emptyList()
                )
        ).collect(Collectors.toList());

        Map<TopicPartition, org.apache.kafka.clients.consumer.OffsetAndMetadata> offsets =
                adminClient.listConsumerGroupOffsets(groupId).partitionsToOffsetAndMetadata().get(TIMEOUT_SECONDS, TimeUnit.SECONDS);

        Map<String, ConsumerGroupDetailResponse.PartitionLagInfo> partitionOffsets = new HashMap<>();
        offsets.forEach((tp, om) -> {
            String key = tp.topic() + "-p" + tp.partition();
            partitionOffsets.put(key, new ConsumerGroupDetailResponse.PartitionLagInfo(
                    tp.topic(),
                    tp.partition(),
                    om != null ? om.offset() : 0,
                    om != null ? om.offset() : 0, // Estimado sin log end offset
                    0L
            ));
        });

        String coordinator = desc.coordinator() != null ? desc.coordinator().host() + ":" + desc.coordinator().port() : "N/A";

        return new ConsumerGroupDetailResponse(
                desc.groupId(),
                desc.state() != null ? desc.state().name() : "UNKNOWN",
                desc.partitionAssignor(),
                coordinator,
                members,
                partitionOffsets
        );
    }
}
