package cl.duoc.pedidos360.admin.service;

import cl.duoc.pedidos360.admin.dto.CreateTopicRequest;
import org.apache.kafka.clients.admin.*;
import org.apache.kafka.common.KafkaFuture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class KafkaAdminServiceTest {

    private AdminClient adminClient;
    private KafkaAdminService service;

    @BeforeEach
    void setUp() {
        adminClient = mock(AdminClient.class);
        service = new KafkaAdminService(adminClient);
    }

    @Test
    void createTopic_DebeInvocarAdminClientConParametrosCorrectos() throws Exception {
        CreateTopicsResult mockResult = mock(CreateTopicsResult.class);
        when(mockResult.all()).thenReturn(KafkaFuture.completedFuture(null));
        when(adminClient.createTopics(anyCollection())).thenReturn(mockResult);

        CreateTopicRequest request = new CreateTopicRequest(
                "pedidos.custom.topic",
                3,
                (short) 1,
                Map.of("retention.ms", "86400000", "cleanup.policy", "delete")
        );

        service.createTopic(request);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<Collection<NewTopic>> captor = ArgumentCaptor.forClass(Collection.class);
        verify(adminClient, times(1)).createTopics(captor.capture());

        Collection<NewTopic> newTopics = captor.getValue();
        assertEquals(1, newTopics.size());
        NewTopic topic = newTopics.iterator().next();
        assertEquals("pedidos.custom.topic", topic.name());
        assertEquals(3, topic.numPartitions());
        assertEquals((short) 1, topic.replicationFactor());
        assertEquals("86400000", topic.configs().get("retention.ms"));
    }

    @Test
    void deleteTopic_DebeInvocarDeleteTopics() throws Exception {
        DeleteTopicsResult mockResult = mock(DeleteTopicsResult.class);
        when(mockResult.all()).thenReturn(KafkaFuture.completedFuture(null));
        when(adminClient.deleteTopics(anyCollection())).thenReturn(mockResult);

        service.deleteTopic("pedidos.custom.topic");

        verify(adminClient, times(1)).deleteTopics(Collections.singletonList("pedidos.custom.topic"));
    }

    @Test
    void updateTopicPartitions_DebeInvocarCreatePartitions() throws Exception {
        CreatePartitionsResult mockResult = mock(CreatePartitionsResult.class);
        when(mockResult.all()).thenReturn(KafkaFuture.completedFuture(null));
        when(adminClient.createPartitions(anyMap())).thenReturn(mockResult);

        service.updateTopicPartitions("pedidos.custom.topic", 6);

        verify(adminClient, times(1)).createPartitions(anyMap());
    }

    @Test
    void updateTopicConfigs_DebeInvocarIncrementalAlterConfigs() throws Exception {
        AlterConfigsResult mockResult = mock(AlterConfigsResult.class);
        when(mockResult.all()).thenReturn(KafkaFuture.completedFuture(null));
        when(adminClient.incrementalAlterConfigs(anyMap())).thenReturn(mockResult);

        service.updateTopicConfigs("pedidos.custom.topic", Map.of("retention.ms", "172800000"));

        verify(adminClient, times(1)).incrementalAlterConfigs(anyMap());
    }
}
