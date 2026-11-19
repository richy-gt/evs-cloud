package cl.duoc.pedidos360.admin.dto;

import jakarta.validation.constraints.Min;

public record UpdatePartitionsRequest(
        @Min(value = 1, message = "El número total de particiones debe ser mayor que 0")
        int totalPartitions
) {}
