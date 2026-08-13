package transporte.desafio.application.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import transporte.desafio.application.dto.ScaleStabilizedEvent;
import transporte.desafio.application.dto.WeightReadingDto;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class WeightStabilizationService {

    private final int bufferSize;
    private final double maxVariationKg;
    private final double minTruckWeightKg;

    private final Map<String, EvictingQueue<WeightReadingDto>> scaleBuffers = new ConcurrentHashMap<>();
    private final Set<String> processedPlates = Collections.newSetFromMap(new ConcurrentHashMap<>());

    public WeightStabilizationService(
            @Value("${app.stabilization.buffer-size:20}") int bufferSize,
            @Value("${app.stabilization.max-variation-kg:5.0}") double maxVariationKg,
            @Value("${app.stabilization.min-truck-weight-kg:1000.0}") double minTruckWeightKg) {
        this.bufferSize = bufferSize;
        this.maxVariationKg = maxVariationKg;
        this.minTruckWeightKg = minTruckWeightKg;
    }

    public Optional<ScaleStabilizedEvent> processReading(WeightReadingDto reading) {
        String scaleId = reading.id();
        
        if (reading.weight() == null || reading.weight().doubleValue() < minTruckWeightKg) {
            scaleBuffers.remove(scaleId);
            return Optional.empty();
        }

        EvictingQueue<WeightReadingDto> buffer = scaleBuffers.computeIfAbsent(
            scaleId, k -> new EvictingQueue<>(bufferSize)
        );

        if (!buffer.isEmpty() && reading.plate() != null && !buffer.peekLast().plate().equalsIgnoreCase(reading.plate())) {
            String previousPlate = buffer.peekLast().plate();
            buffer.clear();
            processedPlates.remove(scaleId + ":" + previousPlate);
        }

        buffer.add(reading);

        if (buffer.size() == bufferSize) {
            String readingKey = scaleId + ":" + reading.plate();

            if (processedPlates.contains(readingKey)) {
                return Optional.empty();
            }

            if (isStabilized(buffer)) {
                BigDecimal averageWeight = calculateAverageWeight(buffer);
                processedPlates.add(readingKey);

                return Optional.of(new ScaleStabilizedEvent(
                    scaleId,
                    reading.plate(),
                    averageWeight,
                    LocalDateTime.now()
                ));
            }
        }

        return Optional.empty();
    }

    private boolean isStabilized(Collection<WeightReadingDto> readings) {
        double min = Double.MAX_VALUE;
        double max = Double.MIN_VALUE;

        for (WeightReadingDto r : readings) {
            double val = r.weight().doubleValue();
            if (val < min) min = val;
            if (val > max) max = val;
        }

        return (max - min) <= maxVariationKg;
    }

    private BigDecimal calculateAverageWeight(Collection<WeightReadingDto> readings) {
        double sum = readings.stream()
            .mapToDouble(r -> r.weight().doubleValue())
            .sum();

        double avg = sum / readings.size();
        return BigDecimal.valueOf(avg).setScale(2, RoundingMode.HALF_UP);
    }

    public void clearBuffers() {
        scaleBuffers.clear();
        processedPlates.clear();
    }

    public int getBufferSize() {
        return bufferSize;
    }

    public double getMaxVariationKg() {
        return maxVariationKg;
    }

    public double getMinTruckWeightKg() {
        return minTruckWeightKg;
    }

    private static class EvictingQueue<T> extends LinkedList<T> {
        private final int maxSize;

        public EvictingQueue(int maxSize) {
            this.maxSize = maxSize;
        }

        @Override
        public boolean add(T item) {
            if (size() >= maxSize) {
                removeFirst();
            }
            return super.add(item);
        }

        public T peekLast() {
            return isEmpty() ? null : getLast();
        }
    }
}
