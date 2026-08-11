package transporte.desafio.adapter.outbound.persistence;

import org.springframework.stereotype.Component;
import transporte.desafio.adapter.outbound.persistence.entity.CaminhaoJpaEntity;
import transporte.desafio.adapter.outbound.persistence.repository.CaminhaoJpaRepository;
import transporte.desafio.application.ports.out.CaminhaoRepositoryPort;
import transporte.desafio.domain.model.Caminhao;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class CaminhaoRepositoryAdapter implements CaminhaoRepositoryPort {

    private final CaminhaoJpaRepository repository;

    public CaminhaoRepositoryAdapter(CaminhaoJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public Caminhao salvar(Caminhao caminhao) {
        return toDomain(repository.save(toEntity(caminhao)));
    }

    @Override
    public Optional<Caminhao> buscarPorId(UUID id) {
        return repository.findById(id).map(this::toDomain);
    }

    @Override
    public Optional<Caminhao> buscarPorPlaca(String placa) {
        return repository.findByPlaca(placa).map(this::toDomain);
    }

    @Override
    public List<Caminhao> listar() {
        return repository.findAll().stream().map(this::toDomain).toList();
    }

    @Override
    public boolean existsById(UUID id) {
        return repository.existsById(id);
    }

    private CaminhaoJpaEntity toEntity(Caminhao d) {
        return new CaminhaoJpaEntity(d.getId(), d.getPlaca(), d.getTara(), d.getDescricao(),
                d.getAtivo(), d.getCreatedAt(), d.getCreatedAt());
    }

    private Caminhao toDomain(CaminhaoJpaEntity e) {
        return new Caminhao(e.getId(), e.getPlaca(), e.getTara(), e.getDescricao(),
                e.getAtivo(), e.getCreatedAt());
    }
}
