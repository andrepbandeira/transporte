package transporte.desafio.application.service;

import transporte.desafio.domain.model.LeituraPeso;
import transporte.desafio.domain.model.ResultadoEstabilizacao;
import transporte.desafio.domain.service.AlgoritmoEstabilizacao;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

/**
 * Gerenciador de estado para o algoritmo de estabilizacao.
 * <p>
 * Mantem a janela de leituras e o estado atual (pendente, estabilizado)
 * para uma combinacao especifica de balanca + placa. E thread-safe.
 *
 * <p>Responsabilidades:
 * <ul>
 *   <li>Acumular leituras conforme chegam do Kafka</li>
 *   <li>Invocar o {@link AlgoritmoEstabilizacao} para verificar estabilizacao</li>
 *   <li>Ignorar leituras apos a estabilizacao (idempotencia)</li>
 *   <li>Resetar o estado quando a balanca volta a zero (leitura ~0 + placa vazia)</li>
 * </ul>
 */
public class EstadoEstabilizacao {

    /** Peso considerado "zero" — abaixo disso a balanca esta livre. */
    static final double ZERO_THRESHOLD_KG = 1.0;

    private final AlgoritmoEstabilizacao algoritmo;
    private final Deque<LeituraPeso> leituras = new ArrayDeque<>();
    private volatile boolean estabilizado = false;
    private volatile double pesoEstabilizado = 0.0;
    private volatile boolean zerado = false;

    public EstadoEstabilizacao(AlgoritmoEstabilizacao algoritmo) {
        this.algoritmo = algoritmo;
    }

    /**
     * Adiciona uma nova leitura ao estado.
     * Se ja estabilizado, apenas verifica reset (zero + placa vazia).
     *
     * @param leitura  leitura da balanca
     * @param placa    placa enviada na mesma leitura (pode ser vazia)
     * @return true se a estabilizacao foi atingida com esta leitura
     */
    public synchronized boolean adicionarLeitura(LeituraPeso leitura, String placa) {
        double peso = Math.max(0, leitura.pesoKg());

        // Se ja estabilizado, verifica se a balanca voltou a zero
        if (estabilizado) {
            if (peso <= ZERO_THRESHOLD_KG && isPlacaVazia(placa)) {
                reset();
            }
            return false;
        }

        // Se zerado (estado anterior foi resetado), mas recebe peso nao-zero,
        // significa inicio de uma nova operacao
        if (zerado && peso > ZERO_THRESHOLD_KG) {
            zerado = false;
        }

        // Adiciona leitura
        leituras.addLast(leitura);
        // Mantem apenas as ultimas 2x windowReadings leituras para economia de memoria
        int maxSize = algoritmo.getWindowReadings() * 4;
        while (leituras.size() > maxSize) {
            leituras.removeFirst();
        }

        // Verifica estabilizacao
        List<LeituraPeso> snapshot = new ArrayList<>(leituras);
        ResultadoEstabilizacao result = algoritmo.verificar(snapshot);

        if (result == ResultadoEstabilizacao.ESTABILIZADO) {
            estabilizado = true;
            pesoEstabilizado = calcularPesoEstabilizado();
            zerado = true; // Marca para permitir reset
            return true;
        }

        return false;
    }

    /**
     * Calcula o peso estabilizado como media das ultimas {@code windowReadings} leituras.
     */
    private double calcularPesoEstabilizado() {
        int count = Math.min(leituras.size(), algoritmo.getWindowReadings());
        if (count == 0) return 0.0;

        List<LeituraPeso> lastN = leituras.stream()
                .skip(Math.max(0, leituras.size() - count))
                .toList();

        return lastN.stream()
                .mapToDouble(LeituraPeso::pesoKg)
                .average()
                .orElse(0.0);
    }

    private boolean isPlacaVazia(String placa) {
        return placa == null || placa.trim().isEmpty();
    }

    public boolean isEstabilizado() {
        return estabilizado;
    }

    public double getPesoEstabilizado() {
        return pesoEstabilizado;
    }

    public boolean isZerado() {
        return zerado;
    }

    public boolean isProntoParaNovoCiclo() {
        return estabilizado && zerado;
    }

    /**
     * Reseta o estado para comecar um novo ciclo de pesagem.
     */
    public synchronized void reset() {
        leituras.clear();
        estabilizado = false;
        pesoEstabilizado = 0.0;
        zerado = false;
    }

    public boolean isEmpty() {
        return leituras.isEmpty();
    }
}
