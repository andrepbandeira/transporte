package transporte.desafio.adapter.outbound.persistence;

import org.springframework.stereotype.Component;
import transporte.desafio.adapter.outbound.persistence.entity.FilialJpaEntity;
import transporte.desafio.adapter.outbound.persistence.repository.FilialJpaRepository;
import transporte.desafio.application.ports.out.FilialRepositoryPort;
import transporte.desafio.domain.model.Filial;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class FilialRepositoryAdapter implements FilialRepositoryPort {

    private final FilialJpaRepository repository;

    public FilialRepositoryAdapter(FilialJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public Filial salvar(Filial filial) {
        return toDomain(repository.save(toEntity(filial)));
    }

    @Override
    public Optional<Filial> buscarPorId(UUID id) {
        return repository.findById(id).map(this::toDomain);
    }

    @Override
    public List<Filial> listar() {
        return repository.findAll().stream().map(this::toDomain).toList();
    }

    @Override
    public boolean existsById(UUID id) {
        return repository.existsById(id);
    }

    private FilialJpaEntity toEntity(Filial d) {
        return new FilialJpaEntity(d.getId(), d.getNome(), d.getCidade(), d.getEstado(),
                d.getAtivo(), d.getCreatedAt(), d.getCreatedAt());
    }

    private Filial toDomain(FilialJpaEntity e) {
        return new Filial(e.getId(), e.getNome(), e.getCidade(), e.getEstado(),
                e.getAtivo(), e.getCreatedAt());
    }
}
