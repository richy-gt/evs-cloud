package cl.duoc.pedidos360.admin.dto;

import java.util.List;
import java.util.Map;

public record ConsumerGroupDetailResponse(
        String groupId,
        String state,
        String protocolType,
        String coordinator,
        List<MemberInfo> members,
        Map<String, PartitionLagInfo> partitionOffsets
) {
    public record MemberInfo(
            String memberId,
            String clientId,
            String host,
            List<String> assignedPartitions
    ) {}

    public record PartitionLagInfo(
            String topic,
            int partition,
            long currentOffset,
            long logEndOffset,
            long lag
    ) {}
}
