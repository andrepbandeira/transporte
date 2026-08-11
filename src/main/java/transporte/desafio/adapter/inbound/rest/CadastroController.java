package transporte.desafio.adapter.inbound.rest;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import transporte.desafio.application.dto.*;
import transporte.desafio.application.ports.in.CadastroUseCase;
import transporte.desafio.domain.model.*;

import java.net.URI;
import java.util.List;

/**
 * Endpoints de cadastro administrativo.
 */
@RestController
@RequestMapping("/api/v1/cadastros")
public class CadastroController {

    private final CadastroUseCase cadastroUseCase;

    public CadastroController(CadastroUseCase cadastroUseCase) {
        this.cadastroUseCase = cadastroUseCase;
    }

    // --------- Filial ---------
    @PostMapping("/filial")
    public ResponseEntity<Filial> criarFilial(@Valid @RequestBody FilialRequest request) {
        Filial f = cadastroUseCase.cadastrarFilial(request);
        return ResponseEntity.created(URI.create("/api/v1/cadastros/filial/" + f.getId())).body(f);
    }

    @GetMapping("/filiais")
    public ResponseEntity<List<Filial>> listarFiliais() {
        return ResponseEntity.ok(cadastroUseCase.listarFiliais());
    }

    // --------- Balanca ---------
    @PostMapping("/balanca")
    public ResponseEntity<Balanca> criarBalanca(@Valid @RequestBody BalancaRequest request) {
        Balanca b = cadastroUseCase.cadastrarBalanca(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(b);
    }

    @GetMapping("/balancas")
    public ResponseEntity<List<Balanca>> listarBalancas() {
        return ResponseEntity.ok(cadastroUseCase.listarBalancas());
    }

    // --------- TipoGrao ---------
    @PostMapping("/tipo-grao")
    public ResponseEntity<TipoGrao> criarTipoGrao(@Valid @RequestBody TipoGraoRequest request) {
        TipoGrao t = cadastroUseCase.cadastrarTipoGrao(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(t);
    }

    @GetMapping("/tipos-grao")
    public ResponseEntity<List<TipoGrao>> listarTiposGrao() {
        return ResponseEntity.ok(cadastroUseCase.listarTiposGraos());
    }

    // --------- Caminhao ---------
    @PostMapping("/caminhao")
    public ResponseEntity<Caminhao> criarCaminhao(@Valid @RequestBody CaminhaoRequest request) {
        Caminhao c = cadastroUseCase.cadastrarCaminhao(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(c);
    }

    @GetMapping("/caminhoes")
    public ResponseEntity<List<Caminhao>> listarCaminhoes() {
        return ResponseEntity.ok(cadastroUseCase.listarCaminhoes());
    }

    // --------- Transacao ---------
    @PostMapping("/transacao")
    public ResponseEntity<TransacaoTransporte> iniciarTransacao(
            @Valid @RequestBody IniciarTransacaoRequest request) {
        TransacaoTransporte t = cadastroUseCase.iniciarTransacao(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(t);
    }
}
