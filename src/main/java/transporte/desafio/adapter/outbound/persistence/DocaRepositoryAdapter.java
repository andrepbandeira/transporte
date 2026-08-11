package transporte.desafio.adapter.outbound.persistence;

import org.springframework.stereotype.Component;
import transporte.desafio.adapter.outbound.persistence.entity.DocaJpaEntity;
import transporte.desafio.adapter.outbound.persistence.repository.DocaJpaRepository;
import transporte.desafio.application.ports.out.DocaRepositoryPort;
import transporte.desafio.domain.model.Doca;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class DocaRepositoryAdapter implements DocaRepositoryPort {

    private final DocaJpaRepository repository;

    public DocaRepositoryAdapter(DocaJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public Doca salvar(Doca doca) {
        DocaJpaEntity e = toEntity(doca);
        DocaJpaEntity saved = repository.save(e);
        // atualiza o saldo no objeto retornado se mudou
        Doca domain = toDomain(saved);
        return domain;
    }

    @Override
    public Optional<Doca> buscarPorId(UUID id) {
        return repository.findById(id).map(this::toDomain);
    }

    @Override
    public Optional<Doca> buscarPorTipoGrao(UUID tipoGraoId) {
        return repository.findByTipoGraoId(tipoGraoId).map(this::toDomain);
    }

    @Override
    public List<Doca> listar() {
        return repository.findAll().stream().map(this::toDomain).toList();
    }

    @Override
    public boolean existsByTipoGrao(UUID tipoGraoId) {
        return repository.existsByTipoGraoId(tipoGraoId);
    }

    private DocaJpaEntity toEntity(Doca d) {
        return new DocaJpaEntity(d.getId(), d.getTipoGraoId(), d.getPesoDisponivel(),
                d.getCreatedAt(), d.getCreatedAt());
    }

    private Doca toDomain(DocaJpaEntity e) {
        return new Doca(e.getId(), e.getTipoGraoId(), e.getPesoDisponivel(), e.getCreatedAt());
    }
}
