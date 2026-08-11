package transporte.desafio.common.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import transporte.desafio.domain.exception.BalancaNaoAutorizadaException;
import transporte.desafio.domain.exception.EntidadeNaoEncontradaException;
import transporte.desafio.domain.exception.RegraDeNegocioException;
import transporte.desafio.domain.exception.TransacaoJaEstabilizadaException;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Tratamento global de erros da API.
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(EntidadeNaoEncontradaException.class)
    public ResponseEntity<Map<String, Object>> naoEncontrado(EntidadeNaoEncontradaException ex) {
        log.warn("Entidade nao encontrada: {}", ex.getMessage());
        return montar(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(BalancaNaoAutorizadaException.class)
    public ResponseEntity<Map<String, Object>> naoAutorizada(BalancaNaoAutorizadaException ex) {
        log.warn("Balanca nao autorizada: {}", ex.getMessage());
        return montar(HttpStatus.UNAUTHORIZED, ex.getMessage());
    }

    @ExceptionHandler(TransacaoJaEstabilizadaException.class)
    public ResponseEntity<Map<String, Object>> jaEstabilizada(TransacaoJaEstabilizadaException ex) {
        log.warn("Transacao ja estabilizada: {}", ex.getMessage());
        return montar(HttpStatus.CONFLICT, ex.getMessage());
    }

    @ExceptionHandler(RegraDeNegocioException.class)
    public ResponseEntity<Map<String, Object>> regraDeNegocio(RegraDeNegocioException ex) {
        log.warn("Regra de negocio violada: {}", ex.getMessage());
        return montar(HttpStatus.UNPROCESSABLE_CONTENT, ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> validacao(MethodArgumentNotValidException ex) {
        Map<String, Object> corpo = corpo(HttpStatus.BAD_REQUEST, "Requisicao invalida");
        Map<String, String> detalhes = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(e -> detalhes.put(e.getField(), e.getDefaultMessage()));
        corpo.put("detalhes", detalhes);
        return ResponseEntity.badRequest().body(corpo);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> erroInesperado(Exception ex) {
        log.error("Erro inesperado", ex);
        return montar(HttpStatus.INTERNAL_SERVER_ERROR, "Erro interno do servidor");
    }

    private ResponseEntity<Map<String, Object>> montar(HttpStatus status, String mensagem) {
        return ResponseEntity.status(status).body(corpo(status, mensagem));
    }

    private Map<String, Object> corpo(HttpStatus status, String mensagem) {
        Map<String, Object> corpo = new LinkedHashMap<>();
        corpo.put("timestamp", LocalDateTime.now());
        corpo.put("status", status.value());
        corpo.put("error", status.getReasonPhrase());
        corpo.put("message", mensagem);
        return corpo;
    }
}
