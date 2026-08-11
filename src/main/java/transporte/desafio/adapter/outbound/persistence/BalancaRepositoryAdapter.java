package transporte.desafio.adapter.outbound.persistence;

import org.springframework.stereotype.Component;
import transporte.desafio.adapter.outbound.persistence.entity.BalancaJpaEntity;
import transporte.desafio.adapter.outbound.persistence.repository.BalancaJpaRepository;
import transporte.desafio.application.ports.out.BalancaRepositoryPort;
import transporte.desafio.domain.model.Balanca;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class BalancaRepositoryAdapter implements BalancaRepositoryPort {

    private final BalancaJpaRepository repository;

    public BalancaRepositoryAdapter(BalancaJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public Balanca salvar(Balanca balanca) {
        return toDomain(repository.save(toEntity(balanca)));
    }

    @Override
    public Optional<Balanca> buscarPorId(UUID id) {
        return repository.findById(id).map(this::toDomain);
    }

    @Override
    public Optional<Balanca> buscarPorCodigo(String codigo) {
        return repository.findByCodigo(codigo).map(this::toDomain);
    }

    @Override
    public List<Balanca> listar() {
        return repository.findAll().stream().map(this::toDomain).toList();
    }

    @Override
    public List<Balanca> listarPorFilial(UUID filialId) {
        return repository.findByFilialId(filialId).stream().map(this::toDomain).toList();
    }

    @Override
    public boolean existsById(UUID id) {
        return repository.existsById(id);
    }

    @Override
    public void deleteById(UUID id) {
        repository.deleteById(id);
    }

    private BalancaJpaEntity toEntity(Balanca d) {
        return new BalancaJpaEntity(d.getId(), d.getCodigo(), d.getPassword(), d.getNome(),
                d.getFilialId(), d.getAtiva(), d.getCreatedAt(), d.getCreatedAt());
    }

    private Balanca toDomain(BalancaJpaEntity e) {
        return new Balanca(e.getId(), e.getCodigo(), e.getPassword(), e.getNome(),
                e.getFilialId(), e.getAtiva(), e.getCreatedAt());
    }
}
