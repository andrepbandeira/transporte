package transporte.desafio.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record WeightReadingDto(
    @NotBlank(message = "O ID da balança é obrigatório")
    String id,
    
    @NotBlank(message = "A placa do caminhão é obrigatória")
    String plate,
    
    @NotNull(message = "O peso é obrigatório")
    BigDecimal weight
) {}