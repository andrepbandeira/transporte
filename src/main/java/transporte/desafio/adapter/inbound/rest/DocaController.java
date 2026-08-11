package transporte.desafio.adapter.inbound.rest;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import transporte.desafio.application.dto.DocaEstoqueResponse;
import transporte.desafio.application.dto.VendaRequest;
import transporte.desafio.application.dto.VendaResponse;
import transporte.desafio.application.ports.in.VendaUseCase;

import java.util.List;

/**
 * Endpoints de estoque da doca e venda de grao.
 * <p>
 * Atende {@code .clinerules/fluxo_execucao.md} item 10 (venda parcial ou total):
 * a venda reduz o saldo disponivel da doca.
 */
@RestController
@RequestMapping("/api/v1/doca")
public class DocaController {

    private final VendaUseCase vendaUseCase;

    public DocaController(VendaUseCase vendaUseCase) {
        this.vendaUseCase = vendaUseCase;
    }

    @GetMapping
    public ResponseEntity<List<DocaEstoqueResponse>> estoque() {
        return ResponseEntity.ok(vendaUseCase.listarEstoque());
    }

    @PostMapping("/venda")
    public ResponseEntity<VendaResponse> vender(@Valid @RequestBody VendaRequest request) {
        VendaResponse response = vendaUseCase.vender(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}