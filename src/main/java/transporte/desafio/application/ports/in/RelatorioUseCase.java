package transporte.desafio.application.ports.in;

import transporte.desafio.application.dto.EficienciaBalancaDto;
import transporte.desafio.application.dto.LucratividadeGraoDto;
import transporte.desafio.application.dto.VolumeFilialDto;

import java.time.LocalDate;
import java.util.List;

/**
 * Use case: relatorios e analises administrativas.
 */
public interface RelatorioUseCase {

    /** Lucratividade media por tipo de grao (margem dinamica vs compra). */
    List<LucratividadeGraoDto> lucratividadePorTipoGrao();

    /** Volume total de graos movimentados por filial e periodo. */
    List<VolumeFilialDto> volumePorFilial(LocalDate dataInicio, LocalDate dataFim);

    /** Eficiencia operacional das balancas (tempo medio de estabilizacao). */
    List<EficienciaBalancaDto> eficienciaPorBalanca();
}
