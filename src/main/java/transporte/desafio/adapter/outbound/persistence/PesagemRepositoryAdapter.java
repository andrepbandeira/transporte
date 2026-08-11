package transporte.desafio.adapter.outbound.persistence;

import org.springframework.stereotype.Component;
import transporte.desafio.adapter.outbound.persistence.entity.PesagemJpaEntity;
import transporte.desafio.adapter.outbound.persistence.repository.PesagemJpaRepository;
import transporte.desafio.application.ports.out.PesagemRepositoryPort;
import transporte.desafio.domain.model.Pesagem;

import java.util.List;
import java.util.UUID;

@Component
public class PesagemRepositoryAdapter implements PesagemRepositoryPort {

    private final PesagemJpaRepository repository;

    public PesagemRepositoryAdapter(PesagemJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public Pesagem salvar(Pesagem pesagem) {
        return toDomain(repository.save(toEntity(pesagem)));
    }

    @Override
    public List<Pesagem> listar() {
        return repository.findAll().stream().map(this::toDomain).toList();
    }

    @Override
    public List<Pesagem> listarPorTransacao(UUID transacaoId) {
        return repository.findByTransacaoId(transacaoId).stream().map(this::toDomain).toList();
    }

    @Override
    public List<Pesagem> listarPorTipoGrao(UUID tipoGraoId) {
        return repository.findByTipoGraoId(tipoGraoId).stream().map(this::toDomain).toList();
    }

    private PesagemJpaEntity toEntity(Pesagem d) {
        return new PesagemJpaEntity(
                d.getId(), d.getTransacaoId(), d.getBalancaId(), d.getCaminhaoId(),
                d.getTipoGraoId(), d.getPlaca(), d.getPesoBrutoEstabilizado(),
                d.getPesoLiquido(), d.getDataHoraPesagem(), d.getCustoCarga(),
                d.getStatusEstabilidade(), d.getObservacao(), d.getCreatedAt(), d.getCreatedAt());
    }

    private Pesagem toDomain(PesagemJpaEntity e) {
        return new Pesagem(
                e.getId(), e.getTransacaoId(), e.getBalancaId(), e.getCaminhaoId(),
                e.getTipoGraoId(), e.getPlaca(), e.getPesoBrutoEstabilizado(),
                e.getPesoLiquido(), e.getDataHoraPesagem(), e.getCustoCarga(),
                e.getStatusEstabilidade(), e.getObservacao(), e.getCreatedAt());
    }
}
