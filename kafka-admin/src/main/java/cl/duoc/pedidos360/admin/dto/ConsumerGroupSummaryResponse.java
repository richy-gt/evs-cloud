package cl.duoc.pedidos360.admin.dto;

public record ConsumerGroupSummaryResponse(
        String groupId,
        boolean isSimpleConsumerGroup,
        String state
) {}
