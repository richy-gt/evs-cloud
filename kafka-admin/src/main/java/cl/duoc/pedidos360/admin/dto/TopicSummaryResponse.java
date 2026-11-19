package cl.duoc.pedidos360.admin.dto;

public record TopicSummaryResponse(
        String name,
        int partitions,
        int replicationFactor,
        boolean isInternal
) {}
