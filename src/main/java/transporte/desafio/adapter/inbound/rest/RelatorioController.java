package transporte.desafio.adapter.inbound.rest;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import transporte.desafio.application.dto.EficienciaBalancaDto;
import transporte.desafio.application.dto.LucratividadeGraoDto;
import transporte.desafio.application.dto.VolumeFilialDto;
import transporte.desafio.application.ports.in.RelatorioUseCase;

import java.time.LocalDate;
import java.util.List;

/**
 * Endpoints de relatorios administrativos.
 */
@RestController
@RequestMapping("/api/v1/relatorios")
public class RelatorioController {

    private final RelatorioUseCase relatorioUseCase;

    public RelatorioController(RelatorioUseCase relatorioUseCase) {
        this.relatorioUseCase = relatorioUseCase;
    }

    @GetMapping("/lucratividade")
    public ResponseEntity<List<LucratividadeGraoDto>> lucratividade() {
        return ResponseEntity.ok(relatorioUseCase.lucratividadePorTipoGrao());
    }

    @GetMapping("/volume")
    public ResponseEntity<List<VolumeFilialDto>> volume(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim) {
        return ResponseEntity.ok(relatorioUseCase.volumePorFilial(inicio, fim));
    }

    @GetMapping("/eficiencia")
    public ResponseEntity<List<EficienciaBalancaDto>> eficiencia() {
        return ResponseEntity.ok(relatorioUseCase.eficienciaPorBalanca());
    }
}
