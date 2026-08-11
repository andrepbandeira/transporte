package transporte.desafio.adapter.outbound.persistence;

import org.springframework.stereotype.Component;
import transporte.desafio.adapter.outbound.persistence.entity.TipoGraoJpaEntity;
import transporte.desafio.adapter.outbound.persistence.repository.TipoGraoJpaRepository;
import transporte.desafio.application.ports.out.TipoGraoRepositoryPort;
import transporte.desafio.domain.model.TipoGrao;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class TipoGraoRepositoryAdapter implements TipoGraoRepositoryPort {

    private final TipoGraoJpaRepository repository;

    public TipoGraoRepositoryAdapter(TipoGraoJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public TipoGrao salvar(TipoGrao tipoGrao) {
        return toDomain(repository.save(toEntity(tipoGrao)));
    }

    @Override
    public Optional<TipoGrao> buscarPorId(UUID id) {
        return repository.findById(id).map(this::toDomain);
    }

    @Override
    public Optional<TipoGrao> buscarPorNome(String nome) {
        return repository.findByNome(nome).map(this::toDomain);
    }

    @Override
    public List<TipoGrao> listar() {
        return repository.findAll().stream().map(this::toDomain).toList();
    }

    @Override
    public boolean existsById(UUID id) {
        return repository.existsById(id);
    }

    private TipoGraoJpaEntity toEntity(TipoGrao d) {
        return new TipoGraoJpaEntity(d.getId(), d.getNome(), d.getPrecoCompraPorTonelada(),
                d.getAtivo(), d.getCreatedAt(), d.getCreatedAt());
    }

    private TipoGrao toDomain(TipoGraoJpaEntity e) {
        return new TipoGrao(e.getId(), e.getNome(), e.getPrecoCompraPorTonelada(),
                e.getAtivo(), e.getCreatedAt());
    }
}
