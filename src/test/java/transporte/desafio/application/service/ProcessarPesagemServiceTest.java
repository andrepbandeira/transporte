package transporte.desafio.application.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import transporte.desafio.application.dto.PesagemEvent;
import transporte.desafio.application.ports.out.BalancaRepositoryPort;
import transporte.desafio.application.ports.out.CaminhaoRepositoryPort;
import transporte.desafio.application.ports.out.DocaRepositoryPort;
import transporte.desafio.application.ports.out.PesagemRepositoryPort;
import transporte.desafio.application.ports.out.TipoGraoRepositoryPort;
import transporte.desafio.application.ports.out.TransacaoRepositoryPort;
import transporte.desafio.domain.exception.EntidadeNaoEncontradaException;
import transporte.desafio.domain.model.*;
import transporte.desafio.domain.enums.StatusTransacao;
import transporte.desafio.domain.service.AlgoritmoEstabilizacao;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Testes do fluxo de processamento de pesagens (consumidor Kafka).
 */
@ExtendWith(MockitoExtension.class)
class ProcessarPesagemServiceTest {

    private static final UUID BALANCA_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final UUID CAMINHAO_ID = UUID.fromString("00000000-0000-0000-0000-000000000002");
    private static final UUID FILIAL_ID = UUID.fromString("00000000-0000-0000-0000-000000000003");
    private static final UUID TIPO_GRAO_ID = UUID.fromString("00000000-0000-0000-0000-000000000004");
    private static final UUID TRANSACAO_ID = UUID.fromString("00000000-0000-0000-0000-000000000005");
    private static final UUID DOCA_ID = UUID.fromString("00000000-0000-0000-0000-000000000006");
    private static final String PLACA = "ABC-1234";

    @Mock private BalancaRepositoryPort balancaRepository;
    @Mock private TransacaoRepositoryPort transacaoRepository;
    @Mock private CaminhaoRepositoryPort caminhaoRepository;
    @Mock private TipoGraoRepositoryPort tipoGraoRepository;
    @Mock private DocaRepositoryPort docaRepository;
    @Mock private PesagemRepositoryPort pesagemRepository;

    private ProcessarPesagemService service;
    private TransacaoTransporte transacao;

    @BeforeEach
    void setUp() {
        AlgoritmoEstabilizacao algoritmo = new AlgoritmoEstabilizacao(5, 2.0, 200);
        service = new ProcessarPesagemService(balancaRepository, transacaoRepository,
                caminhaoRepository, tipoGraoRepository, docaRepository,
                pesagemRepository, algoritmo);

        Balanca balanca = new Balanca(BALANCA_ID, "BAL-001", "secret", "Balanca 1",
                FILIAL_ID, true, LocalDateTime.now());
        Caminhao caminhao = new Caminhao(CAMINHAO_ID, PLACA, BigDecimal.valueOf(5000),
                "Volvo", true, LocalDateTime.now());
        TipoGrao tipoGrao = new TipoGrao(TIPO_GRAO_ID, "Soja", BigDecimal.valueOf(180),
                true, LocalDateTime.now());
        transacao = new TransacaoTransporte(TRANSACAO_ID, CAMINHAO_ID, FILIAL_ID,
                TIPO_GRAO_ID, BALANCA_ID, LocalDateTime.now(), null,
                StatusTransacao.EM_ANDAMENTO, null, LocalDateTime.now());
        Doca doca = new Doca(DOCA_ID, TIPO_GRAO_ID, BigDecimal.valueOf(50000), LocalDateTime.now());

        when(balancaRepository.buscarPorId(BALANCA_ID)).thenReturn(Optional.of(balanca));
        lenient().when(caminhaoRepository.buscarPorId(CAMINHAO_ID)).thenReturn(Optional.of(caminhao));
        lenient().when(tipoGraoRepository.buscarPorId(TIPO_GRAO_ID)).thenReturn(Optional.of(tipoGrao));
        lenient().when(docaRepository.buscarPorTipoGrao(TIPO_GRAO_ID)).thenReturn(Optional.of(doca));
    }

    private PesagemEvent evento(Instant t, double peso) {
        return new PesagemEvent(BALANCA_ID, PLACA, peso, t);
    }

    private Instant base() {
        return Instant.parse("2026-08-11T10:00:00Z");
    }

    @Test
    @DisplayName("rejeita balanca nao cadastrada")
    void rejeitaBalancaNaoCadastrada() {
        when(balancaRepository.buscarPorId(BALANCA_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.processar(evento(base(), 1000.0)))
                .isInstanceOf(EntidadeNaoEncontradaException.class);
    }

    @Test
    @DisplayName("persiste pesagem quando o peso estabiliza")
    void persistePesagemQuandoEstabiliza() {
        when(transacaoRepository.buscarTransacaoAtivaPorBalanca(BALANCA_ID))
                .thenReturn(Optional.of(transacao));

        Instant t = base();
        service.processar(evento(t.plusMillis(100), 1000.0));
        service.processar(evento(t.plusMillis(200), 1000.1));
        service.processar(evento(t.plusMillis(300), 1000.0));
        service.processar(evento(t.plusMillis(400), 1000.2));
        service.processar(evento(t.plusMillis(500), 1000.1));

        verify(pesagemRepository, times(1)).salvar(any(Pesagem.class));
        verify(transacaoRepository, times(1)).salvar(transacao);
    }

    @Test
    @DisplayName("ignora leituras apos a estabilizacao (idempotencia)")
    void ignoraLeiturasAposEstabilizacao() {
        when(transacaoRepository.buscarTransacaoAtivaPorBalanca(BALANCA_ID))
                .thenReturn(Optional.of(transacao));

        Instant t = base();
        service.processar(evento(t.plusMillis(100), 1000.0));
        service.processar(evento(t.plusMillis(200), 1000.1));
        service.processar(evento(t.plusMillis(300), 1000.0));
        service.processar(evento(t.plusMillis(400), 1000.2));
        service.processar(evento(t.plusMillis(500), 1000.1));
        verify(pesagemRepository, times(1)).salvar(any(Pesagem.class));

        service.processar(evento(t.plusMillis(600), 1000.3));
        service.processar(evento(t.plusMillis(700), 1000.2));
        verify(pesagemRepository, times(1)).salvar(any(Pesagem.class));
    }

    @Test
    @DisplayName("ignora leituras quando a transacao ja foi finalizada")
    void ignoraLeiturasQuandoTransacaoJaFinalizada() {
        transacao.finalizar("Pesagem ja concluida");
        when(transacaoRepository.buscarTransacaoAtivaPorBalanca(BALANCA_ID))
                .thenReturn(Optional.of(transacao));

        service.processar(evento(base(), 1000.0));

        verify(pesagemRepository, never()).salvar(any(Pesagem.class));
        verify(transacaoRepository, never()).salvar(any(TransacaoTransporte.class));
    }
}
