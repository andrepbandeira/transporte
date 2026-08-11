package transporte.desafio.application.ports.in;

import transporte.desafio.application.dto.DocaEstoqueResponse;
import transporte.desafio.application.dto.VendaRequest;
import transporte.desafio.application.dto.VendaResponse;

import java.util.List;

/**
 * Use case: operacoes de venda de grao na doca e consulta de estoque.
 * <p>
 * Atende {@code .clinerules/fluxo_execucao.md} item 10 (venda parcial ou
 * total do grao): a venda reduz o saldo disponivel na doca.
 */
public interface VendaUseCase {

    /**
     * Registra uma venda de grao, reduzindo o saldo disponivel na doca.
     *
     * @param request dados da venda (tipo de grao e quantidade em kg)
     * @return resultado da venda com saldo restante e valor calculado
     */
    VendaResponse vender(VendaRequest request);

    /**
     * Lista o estoque disponivel de todos os tipos de grao na doca.
     *
     * @return lista de estoque com saldo e preco de venda estimado
     */
    List<DocaEstoqueResponse> listarEstoque();
}