package transporte.desafio.application.ports.in;

import transporte.desafio.application.dto.*;
import transporte.desafio.domain.model.*;

import java.util.List;
import java.util.UUID;

/**
 * Use case: operacoes de cadastro (filial, balanca, tipo de grao,
 * caminhao, transacao de transporte).
 */
public interface CadastroUseCase {

    // ---- Criacao ----
    Filial cadastrarFilial(FilialRequest request);
    Balanca cadastrarBalanca(BalancaRequest request);
    TipoGrao cadastrarTipoGrao(TipoGraoRequest request);
    Caminhao cadastrarCaminhao(CaminhaoRequest request);
    TransacaoTransporte iniciarTransacao(IniciarTransacaoRequest request);

    // ---- Listagem ----
    List<Filial> listarFiliais();
    List<Balanca> listarBalancas();
    List<TipoGrao> listarTiposGraos();
    List<Caminhao> listarCaminhoes();

    // ---- Busca ----
    Filial buscarFilial(UUID id);
    Balanca buscarBalanca(UUID id);
    TipoGrao buscarTipoGrao(UUID id);
    Caminhao buscarCaminhao(UUID id);
}
