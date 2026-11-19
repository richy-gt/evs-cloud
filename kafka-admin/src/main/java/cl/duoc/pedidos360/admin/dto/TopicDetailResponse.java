package cl.duoc.pedidos360.admin.dto;

import java.util.List;
import java.util.Map;

public record TopicDetailResponse(
        String name,
        int partitions,
        int replicationFactor,
        boolean isInternal,
        Map<String, String> configs,
        List<PartitionInfo> partitionsInfo
) {
    public record PartitionInfo(
            int partition,
            int leaderId,
            List<Integer> replicas,
            List<Integer> isr
    ) {}
}
