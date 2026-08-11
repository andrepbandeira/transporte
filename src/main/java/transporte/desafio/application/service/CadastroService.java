package transporte.desafio.application.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import transporte.desafio.application.dto.BalancaRequest;
import transporte.desafio.application.dto.CaminhaoRequest;
import transporte.desafio.application.dto.FilialRequest;
import transporte.desafio.application.dto.IniciarTransacaoRequest;
import transporte.desafio.application.dto.TipoGraoRequest;
import transporte.desafio.application.ports.in.CadastroUseCase;
import transporte.desafio.application.ports.out.BalancaRepositoryPort;
import transporte.desafio.application.ports.out.CaminhaoRepositoryPort;
import transporte.desafio.application.ports.out.FilialRepositoryPort;
import transporte.desafio.application.ports.out.TipoGraoRepositoryPort;
import transporte.desafio.application.ports.out.TransacaoRepositoryPort;
import transporte.desafio.domain.exception.EntidadeNaoEncontradaException;
import transporte.desafio.domain.exception.RegraDeNegocioException;
import transporte.desafio.domain.model.Balanca;
import transporte.desafio.domain.model.Caminhao;
import transporte.desafio.domain.model.Filial;
import transporte.desafio.domain.model.TipoGrao;
import transporte.desafio.domain.model.TransacaoTransporte;

import java.util.List;
import java.util.UUID;

/**
 * Servico de cadastro: gerencia filiais, balancas, tipos de grao,
 * caminhoes e transacoes de transporte.
 */
@Service
@Slf4j
public class CadastroService implements CadastroUseCase {

    private final FilialRepositoryPort filialRepository;
    private final BalancaRepositoryPort balancaRepository;
    private final TipoGraoRepositoryPort tipoGraoRepository;
    private final CaminhaoRepositoryPort caminhaoRepository;
    private final TransacaoRepositoryPort transacaoRepository;

    public CadastroService(
            FilialRepositoryPort filialRepository,
            BalancaRepositoryPort balancaRepository,
            TipoGraoRepositoryPort tipoGraoRepository,
            CaminhaoRepositoryPort caminhaoRepository,
            TransacaoRepositoryPort transacaoRepository) {
        this.filialRepository = filialRepository;
        this.balancaRepository = balancaRepository;
        this.tipoGraoRepository = tipoGraoRepository;
        this.caminhaoRepository = caminhaoRepository;
        this.transacaoRepository = transacaoRepository;
    }

    @Override
    public Filial cadastrarFilial(FilialRequest request) {
        Filial filial = Filial.novo(request.nome(), request.cidade(), request.estado());
        return filialRepository.salvar(filial);
    }

    @Override
    public List<Filial> listarFiliais() {
        return filialRepository.listar();
    }

    @Override
    public Filial buscarFilial(UUID id) {
        return filialRepository.buscarPorId(id)
                .orElseThrow(() -> new EntidadeNaoEncontradaException("Filial", id));
    }

    @Override
    public Balanca cadastrarBalanca(BalancaRequest request) {
        filialRepository.buscarPorId(request.filialId())
                .orElseThrow(() -> new EntidadeNaoEncontradaException("Filial", request.filialId()));
        Balanca balanca = Balanca.nova(request.codigo(), request.password(), request.nome(), request.filialId());
        try {
            return balancaRepository.salvar(balanca);
        } catch (DataIntegrityViolationException e) {
            throw new RegraDeNegocioException("Codigo de balanca ja existe: " + request.codigo());
        }
    }

    @Override
    public List<Balanca> listarBalancas() {
        return balancaRepository.listar();
    }

    @Override
    public Balanca buscarBalanca(UUID id) {
        return balancaRepository.buscarPorId(id)
                .orElseThrow(() -> new EntidadeNaoEncontradaException("Balanca", id));
    }

    @Override
    public TipoGrao cadastrarTipoGrao(TipoGraoRequest request) {
        TipoGrao tipoGrao = TipoGrao.novo(request.nome(), request.precoCompraPorTonelada());
        try {
            return tipoGraoRepository.salvar(tipoGrao);
        } catch (DataIntegrityViolationException e) {
            throw new RegraDeNegocioException("Tipo de grao ja existe: " + request.nome());
        }
    }

    @Override
    public List<TipoGrao> listarTiposGraos() {
        return tipoGraoRepository.listar();
    }

    @Override
    public TipoGrao buscarTipoGrao(UUID id) {
        return tipoGraoRepository.buscarPorId(id)
                .orElseThrow(() -> new EntidadeNaoEncontradaException("TipoGrao", id));
    }

    @Override
    public Caminhao cadastrarCaminhao(CaminhaoRequest request) {
        Caminhao caminhao = Caminhao.novo(request.placa(), request.tara(), request.descricao());
        try {
            return caminhaoRepository.salvar(caminhao);
        } catch (DataIntegrityViolationException e) {
            throw new RegraDeNegocioException("Placa de caminhao ja existe: " + request.placa());
        }
    }

    @Override
    public List<Caminhao> listarCaminhoes() {
        return caminhaoRepository.listar();
    }

    @Override
    public Caminhao buscarCaminhao(UUID id) {
        return caminhaoRepository.buscarPorId(id)
                .orElseThrow(() -> new EntidadeNaoEncontradaException("Caminhao", id));
    }

    @Override
    public TransacaoTransporte iniciarTransacao(IniciarTransacaoRequest request) {
        caminhaoRepository.buscarPorId(request.caminhaoId())
                .orElseThrow(() -> new EntidadeNaoEncontradaException("Caminhao", request.caminhaoId()));
        filialRepository.buscarPorId(request.filialId())
                .orElseThrow(() -> new EntidadeNaoEncontradaException("Filial", request.filialId()));
        tipoGraoRepository.buscarPorId(request.tipoGraoId())
                .orElseThrow(() -> new EntidadeNaoEncontradaException("TipoGrao", request.tipoGraoId()));

        UUID balancaId = request.balancaId();
        if (balancaId == null) {
            balancaId = balancaRepository.listarPorFilial(request.filialId()).stream()
                    .findFirst()
                    .map(Balanca::getId)
                    .orElseThrow(() -> new RegraDeNegocioException(
                            "Nenhuma balanca encontrada para a filial " + request.filialId()));
        }
        final UUID balancaIdFinal = balancaId;
        balancaRepository.buscarPorId(balancaId)
                .orElseThrow(() -> new EntidadeNaoEncontradaException("Balanca", balancaIdFinal));

        TransacaoTransporte transacao = TransacaoTransporte.iniciar(
                request.caminhaoId(), request.filialId(), request.tipoGraoId(), balancaId);
        return transacaoRepository.salvar(transacao);
    }
}
