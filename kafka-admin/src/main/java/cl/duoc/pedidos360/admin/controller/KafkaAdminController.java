package cl.duoc.pedidos360.admin.controller;

import cl.duoc.pedidos360.admin.dto.*;
import cl.duoc.pedidos360.admin.service.KafkaAdminService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Validated
@RestController
@RequestMapping
public class KafkaAdminController {

    private final KafkaAdminService kafkaAdminService;

    public KafkaAdminController(KafkaAdminService kafkaAdminService) {
        this.kafkaAdminService = kafkaAdminService;
    }

    @GetMapping("/topics")
    public ResponseEntity<List<TopicSummaryResponse>> listTopics() throws Exception {
        return ResponseEntity.ok(kafkaAdminService.listTopics());
    }

    @GetMapping("/topics/{name}")
    public ResponseEntity<TopicDetailResponse> getTopicDetail(@PathVariable("name") @NotBlank String name) throws Exception {
        return ResponseEntity.ok(kafkaAdminService.getTopicDetail(name));
    }

    @PostMapping("/topics")
    public ResponseEntity<Map<String, Object>> createTopic(@Valid @RequestBody CreateTopicRequest request) throws Exception {
        kafkaAdminService.createTopic(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "message", "Tópico Kafka creado exitosamente",
                "name", request.name(),
                "partitions", request.partitions(),
                "replicationFactor", request.replicationFactor(),
                "configs", request.configs()
        ));
    }

    @PutMapping("/topics/{name}/configs")
    public ResponseEntity<Map<String, Object>> updateTopicConfigs(
            @PathVariable("name") @NotBlank String name,
            @Valid @RequestBody UpdateTopicConfigRequest request
    ) throws Exception {
        kafkaAdminService.updateTopicConfigs(name, request.configs());
        return ResponseEntity.ok(Map.of(
                "message", "Configuraciones del tópico actualizadas exitosamente",
                "name", name,
                "updatedConfigs", request.configs()
        ));
    }

    @PutMapping("/topics/{name}/partitions")
    public ResponseEntity<Map<String, Object>> updateTopicPartitions(
            @PathVariable("name") @NotBlank String name,
            @Valid @RequestBody UpdatePartitionsRequest request
    ) throws Exception {
        kafkaAdminService.updateTopicPartitions(name, request.totalPartitions());
        return ResponseEntity.ok(Map.of(
                "message", "Particiones del tópico actualizadas exitosamente",
                "name", name,
                "newTotalPartitions", request.totalPartitions()
        ));
    }

    @DeleteMapping("/topics/{name}")
    public ResponseEntity<Void> deleteTopic(@PathVariable("name") @NotBlank String name) throws Exception {
        kafkaAdminService.deleteTopic(name);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/consumer-groups")
    public ResponseEntity<List<ConsumerGroupSummaryResponse>> listConsumerGroups() throws Exception {
        return ResponseEntity.ok(kafkaAdminService.listConsumerGroups());
    }

    @GetMapping("/consumer-groups/{groupId}")
    public ResponseEntity<ConsumerGroupDetailResponse> getConsumerGroupDetail(@PathVariable("groupId") @NotBlank String groupId) throws Exception {
        return ResponseEntity.ok(kafkaAdminService.getConsumerGroupDetail(groupId));
    }
}
