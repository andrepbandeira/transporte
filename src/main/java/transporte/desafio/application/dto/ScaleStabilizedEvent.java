package transporte.desafio.application.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ScaleStabilizedEvent(
    String scaleId,
    String plate,
    BigDecimal averageWeight,
    LocalDateTime timestamp
) {}
