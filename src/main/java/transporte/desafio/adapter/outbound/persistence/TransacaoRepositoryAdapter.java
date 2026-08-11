package transporte.desafio.adapter.outbound.persistence;

import org.springframework.stereotype.Component;
import transporte.desafio.adapter.outbound.persistence.entity.TransacaoTransporteJpaEntity;
import transporte.desafio.adapter.outbound.persistence.repository.TransacaoTransporteJpaRepository;
import transporte.desafio.application.ports.out.TransacaoRepositoryPort;
import transporte.desafio.domain.enums.StatusTransacao;
import transporte.desafio.domain.model.TransacaoTransporte;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class TransacaoRepositoryAdapter implements TransacaoRepositoryPort {

    private static final List<StatusTransacao> ATIVOS =
            List.of(StatusTransacao.EM_ANDAMENTO);

    private final TransacaoTransporteJpaRepository repository;

    public TransacaoRepositoryAdapter(TransacaoTransporteJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public TransacaoTransporte salvar(TransacaoTransporte transacao) {
        return toDomain(repository.save(toEntity(transacao)));
    }

    @Override
    public Optional<TransacaoTransporte> buscarPorId(UUID id) {
        return repository.findById(id).map(this::toDomain);
    }

    @Override
    public Optional<TransacaoTransporte> buscarTransacaoAtiva(UUID balancaId, String placa) {
        return repository.findTransacaoAtivaPorBalancaEPlaca(balancaId, placa, ATIVOS)
                .map(this::toDomain);
    }

    @Override
    public Optional<TransacaoTransporte> buscarTransacaoAtivaPorBalanca(UUID balancaId) {
        return repository.findTransacaoAtivaPorBalanca(balancaId, ATIVOS).stream()
                .findFirst()
                .map(this::toDomain);
    }

    @Override
    public List<TransacaoTransporte> listar() {
        return repository.findAll().stream().map(this::toDomain).toList();
    }

    @Override
    public boolean existsById(UUID id) {
        return repository.existsById(id);
    }

    private TransacaoTransporteJpaEntity toEntity(TransacaoTransporte d) {
        return new TransacaoTransporteJpaEntity(
                d.getId(), d.getCaminhaoId(), d.getFilialId(), d.getTipoGraoId(),
                d.getBalancaId(), d.getDataInicio(), d.getDataFim(), d.getStatus(),
                d.getObservacao(), d.getCreatedAt(), d.getCreatedAt());
    }

    private TransacaoTransporte toDomain(TransacaoTransporteJpaEntity e) {
        TransacaoTransporte t = new TransacaoTransporte(
                e.getId(), e.getCaminhaoId(), e.getFilialId(), e.getTipoGraoId(),
                e.getBalancaId(), e.getDataInicio(), e.getDataFim(), e.getStatus(),
                e.getObservacao(), e.getCreatedAt());
        return t;
    }
}
