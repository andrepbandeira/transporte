package transporte.desafio.application.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import transporte.desafio.application.dto.DocaEstoqueResponse;
import transporte.desafio.application.dto.VendaRequest;
import transporte.desafio.application.dto.VendaResponse;
import transporte.desafio.application.ports.out.DocaRepositoryPort;
import transporte.desafio.application.ports.out.TipoGraoRepositoryPort;
import transporte.desafio.domain.exception.EntidadeNaoEncontradaException;
import transporte.desafio.domain.exception.RegraDeNegocioException;
import transporte.desafio.domain.model.Doca;
import transporte.desafio.domain.model.TipoGrao;
import transporte.desafio.domain.service.CalculadoraMargemDinamica;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Testes do servico de venda de grao na doca
 * ({@code .clinerules/fluxo_execucao.md} item 10).
 */
@ExtendWith(MockitoExtension.class)
class VendaServiceTest {

    private static final UUID TIPO_GRAO_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final UUID DOCA_ID = UUID.fromString("00000000-0000-0000-0000-000000000002");

    @Mock private DocaRepositoryPort docaRepository;
    @Mock private TipoGraoRepositoryPort tipoGraoRepository;

    private VendaService service;

    @BeforeEach
    void setUp() {
        CalculadoraMargemDinamica calculadora =
                new CalculadoraMargemDinamica(0.05, 0.20, BigDecimal.valueOf(1000));
        service = new VendaService(docaRepository, tipoGraoRepository, calculadora);
    }

    private Doca docaComSaldo(BigDecimal saldoKg) {
        return new Doca(DOCA_ID, TIPO_GRAO_ID, saldoKg, LocalDateTime.now());
    }

    private TipoGrao tipoGrao() {
        return new TipoGrao(TIPO_GRAO_ID, "Soja", BigDecimal.valueOf(180), true, LocalDateTime.now());
    }

    @Test
    @DisplayName("venda parcial reduz o saldo disponivel da doca")
    void vendaReduzSaldo() {
        Doca doca = docaComSaldo(BigDecimal.valueOf(10000));
        when(docaRepository.buscarPorTipoGrao(TIPO_GRAO_ID)).thenReturn(Optional.of(doca));
        when(tipoGraoRepository.buscarPorId(TIPO_GRAO_ID)).thenReturn(Optional.of(tipoGrao()));
        when(docaRepository.salvar(any(Doca.class))).thenAnswer(inv -> inv.getArgument(0));

        VendaResponse resp = service.vender(new VendaRequest(TIPO_GRAO_ID, BigDecimal.valueOf(2000)));

        assertThat(resp.tipoGrao()).isEqualTo("Soja");
        assertThat(resp.quantidadeVendidaKg()).isEqualByComparingTo("2000");
        assertThat(resp.saldoRestanteKg()).isEqualByComparingTo("8000");
        assertThat(doca.getPesoDisponivel()).isEqualByComparingTo("8000");
        assertThat(resp.valorVenda()).isNotNull();
        verify(docaRepository).salvar(doca);
    }

    @Test
    @DisplayName("venda total zera o saldo disponivel da doca")
    void vendaTotalZeraSaldo() {
        Doca doca = docaComSaldo(BigDecimal.valueOf(5000));
        when(docaRepository.buscarPorTipoGrao(TIPO_GRAO_ID)).thenReturn(Optional.of(doca));
        when(tipoGraoRepository.buscarPorId(TIPO_GRAO_ID)).thenReturn(Optional.of(tipoGrao()));
        when(docaRepository.salvar(any(Doca.class))).thenAnswer(inv -> inv.getArgument(0));

        VendaResponse resp = service.vender(new VendaRequest(TIPO_GRAO_ID, BigDecimal.valueOf(5000)));

        assertThat(resp.saldoRestanteKg()).isEqualByComparingTo("0");
        assertThat(doca.getPesoDisponivel()).isEqualByComparingTo("0");
    }

    @Test
    @DisplayName("rejeita venda com estoque insuficiente")
    void vendaEstoqueInsuficiente() {
        Doca doca = docaComSaldo(BigDecimal.valueOf(500));
        when(docaRepository.buscarPorTipoGrao(TIPO_GRAO_ID)).thenReturn(Optional.of(doca));

        assertThatThrownBy(() -> service.vender(new VendaRequest(TIPO_GRAO_ID, BigDecimal.valueOf(600))))
                .isInstanceOf(RegraDeNegocioException.class);

        assertThat(doca.getPesoDisponivel()).isEqualByComparingTo("500");
        verify(docaRepository, never()).salvar(any(Doca.class));
    }

    @Test
    @DisplayName("rejeita venda quando nao existe doca para o tipo de grao")
    void vendaDocaNaoExiste() {
        when(docaRepository.buscarPorTipoGrao(TIPO_GRAO_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.vender(new VendaRequest(TIPO_GRAO_ID, BigDecimal.valueOf(100))))
                .isInstanceOf(EntidadeNaoEncontradaException.class);
    }

    @Test
    @DisplayName("lista o estoque com preco de venda estimado")
    void listaEstoque() {
        Doca doca = docaComSaldo(BigDecimal.valueOf(10000));
        when(docaRepository.listar()).thenReturn(List.of(doca));
        when(tipoGraoRepository.listar()).thenReturn(List.of(tipoGrao()));

        List<DocaEstoqueResponse> resp = service.listarEstoque();

        assertThat(resp).hasSize(1);
        assertThat(resp.get(0).tipoGrao()).isEqualTo("Soja");
        assertThat(resp.get(0).pesoDisponivelKg()).isEqualByComparingTo("10000");
        assertThat(resp.get(0).precoVendaPorTonelada()).isNotNull();
    }
}