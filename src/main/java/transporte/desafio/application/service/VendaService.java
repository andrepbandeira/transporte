package transporte.desafio.application.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import transporte.desafio.application.dto.DocaEstoqueResponse;
import transporte.desafio.application.dto.VendaRequest;
import transporte.desafio.application.dto.VendaResponse;
import transporte.desafio.application.ports.in.VendaUseCase;
import transporte.desafio.application.ports.out.DocaRepositoryPort;
import transporte.desafio.application.ports.out.TipoGraoRepositoryPort;
import transporte.desafio.domain.exception.EntidadeNaoEncontradaException;
import transporte.desafio.domain.exception.RegraDeNegocioException;
import transporte.desafio.domain.model.Doca;
import transporte.desafio.domain.model.TipoGrao;
import transporte.desafio.domain.service.CalculadoraMargemDinamica;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Servico de venda de grao na doca.
 * <p>
 * Implementa {@code .clinerules/fluxo_execucao.md} item 10: quando uma
 * quantidade de grao for vendida, o saldo disponivel da doca e reduzido
 * (venda parcial ou total).
 */
@Service
@Slf4j
public class VendaService implements VendaUseCase {

    private final DocaRepositoryPort docaRepository;
    private final TipoGraoRepositoryPort tipoGraoRepository;
    private final CalculadoraMargemDinamica calculadora;

    public VendaService(DocaRepositoryPort docaRepository,
                        TipoGraoRepositoryPort tipoGraoRepository,
                        CalculadoraMargemDinamica calculadora) {
        this.docaRepository = docaRepository;
        this.tipoGraoRepository = tipoGraoRepository;
        this.calculadora = calculadora;
    }

    @Override
    public VendaResponse vender(VendaRequest request) {
        Doca doca = docaRepository.buscarPorTipoGrao(request.tipoGraoId())
                .orElseThrow(() -> new EntidadeNaoEncontradaException("Doca", request.tipoGraoId()));

        BigDecimal quantidade = request.quantidadeKg() != null
                ? request.quantidadeKg().setScale(2, RoundingMode.HALF_UP)
                : null;
        if (quantidade == null || quantidade.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RegraDeNegocioException("Quantidade de venda deve ser maior que zero");
        }
        if (quantidade.compareTo(doca.getPesoDisponivel()) > 0) {
            throw new RegraDeNegocioException(
                    "Estoque insuficiente na doca: disponivel=" + doca.getPesoDisponivel()
                            + " kg, solicitado=" + quantidade + " kg");
        }

        // Dominio valida e reduz o saldo disponivel (venda parcial ou total)
        doca.vender(quantidade);
        doca = docaRepository.salvar(doca);

        TipoGrao tipoGrao = tipoGraoRepository.buscarPorId(request.tipoGraoId())
                .orElseThrow(() -> new EntidadeNaoEncontradaException("TipoGrao", request.tipoGraoId()));

        BigDecimal precoVenda = calculadora.calcularPrecoVenda(
                tipoGrao.getPrecoCompraPorTonelada(), doca.getPesoDisponivel());
        BigDecimal valorVenda = quantidade
                .divide(BigDecimal.valueOf(1000), 2, RoundingMode.HALF_UP)
                .multiply(precoVenda)
                .setScale(2, RoundingMode.HALF_UP);

        log.info("Venda registrada: tipoGrao={}, qtd={} kg, saldo restante={} kg",
                request.tipoGraoId(), quantidade, doca.getPesoDisponivel());

        return new VendaResponse(
                request.tipoGraoId(),
                tipoGrao.getNome(),
                quantidade,
                doca.getPesoDisponivel(),
                precoVenda,
                valorVenda);
    }

    @Override
    public List<DocaEstoqueResponse> listarEstoque() {
        Map<UUID, TipoGrao> tipoGraos = tipoGraoRepository.listar().stream()
                .collect(Collectors.toMap(TipoGrao::getId, Function.identity()));

        return docaRepository.listar().stream()
                .map(d -> {
                    TipoGrao tipoGrao = tipoGraos.get(d.getTipoGraoId());
                    String nome = tipoGrao != null ? tipoGrao.getNome() : "?";
                    BigDecimal precoCompra = tipoGrao != null
                            ? tipoGrao.getPrecoCompraPorTonelada() : BigDecimal.ZERO;
                    BigDecimal precoVenda = calculadora.calcularPrecoVenda(
                            precoCompra, d.getPesoDisponivel());
                    return new DocaEstoqueResponse(
                            d.getId(), d.getTipoGraoId(), nome,
                            d.getPesoDisponivel(), precoVenda);
                })
                .toList();
    }
}