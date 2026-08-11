package transporte.desafio.adapter.inbound.rest;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import transporte.desafio.application.dto.PesagemRequest;
import transporte.desafio.application.ports.in.ReceberPesagemUseCase;

/**
 * Endpoint de ingestao (ESP32 / balancas).
 * Recebe leituras e responde 202 Accepted imediatamente.
 */
@RestController
@RequestMapping("/api/v1/pesagens")
public class PesagemController {

    private final ReceberPesagemUseCase receberPesagemUseCase;

    public PesagemController(ReceberPesagemUseCase receberPesagemUseCase) {
        this.receberPesagemUseCase = receberPesagemUseCase;
    }

    @PostMapping
    public ResponseEntity<Void> receber(@Valid @RequestBody PesagemRequest request) {
        receberPesagemUseCase.receber(request);
        return ResponseEntity.accepted().build();
    }
}
