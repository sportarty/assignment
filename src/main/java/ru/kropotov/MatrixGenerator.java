package ru.kropotov;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import ru.kropotov.model.RequestConfig;
import ru.kropotov.model.StandardSymbol;

import java.util.*;

@Slf4j
public class MatrixGenerator {
    private final RequestConfig config;
    private final double bonusPercentage;
    private final Random random = new Random();
    private final Map<Integer, Map<Integer, Map<String, Integer>>> symbolWeightsByPosition = new HashMap<>();

    public MatrixGenerator(RequestConfig config, double bonusPercentage) {
        this.config = config;
        this.bonusPercentage = bonusPercentage;
        initializeSymbolWeights();
    }

    private void initializeSymbolWeights() {
        List<StandardSymbol> standardSymbols = config.getProbabilities().getStandardSymbols();
        for (StandardSymbol ss : standardSymbols) {
            symbolWeightsByPosition
                    .computeIfAbsent(ss.getRow(), k -> new HashMap<>())
                    .put(ss.getColumn(), ss.getSymbols());
        }
    }

    public String[][] generateMatrix() {
        String[][] matrix = new String[config.getRows()][config.getColumns()];

        // Fill the matrix with standard symbols based on probabilities
        for (int row = 0; row < config.getRows(); row++) {
            for (int col = 0; col < config.getColumns(); col++) {
                matrix[row][col] = getRandomSymbol(row, col);
            }
        }

        // Add bonus symbols based on their probabilities
        addBonusSymbols(matrix, config.getProbabilities().getBonusSymbols().getSymbols());

        return matrix;
    }

    private String getRandomSymbol(int row, int col) {
        Map<String, Integer> symbolWeights = symbolWeightsByPosition
                .getOrDefault(row, Collections.emptyMap())
                .getOrDefault(col, symbolWeightsByPosition.get(0).get(0));

        List<SymbolProbability> cumulativeDistribution = createCumulativeDistribution(symbolWeights);
        return getSymbolFromCumulativeDistribution(cumulativeDistribution);
    }

    private void addBonusSymbols(String[][] matrix, Map<String, Integer> bonusSymbols) {
        List<SymbolProbability> cumulativeDistribution = createCumulativeDistribution(bonusSymbols);
        int totalCells = config.getRows() * config.getColumns();

        for (int i = 0; i < totalCells; i++) {
            int row = i / config.getColumns();
            int col = i % config.getColumns();
            if (random.nextDouble() < bonusPercentage) {
                String bonusSymbol = getSymbolFromCumulativeDistribution(cumulativeDistribution);
                matrix[row][col] = bonusSymbol;
            }
        }
    }

    private List<SymbolProbability> createCumulativeDistribution(Map<String, Integer> symbolWeights) {
        List<SymbolProbability> cumulativeDistribution = new ArrayList<>();
        int cumulativeWeight = 0;
        for (Map.Entry<String, Integer> entry : symbolWeights.entrySet()) {
            cumulativeWeight += entry.getValue();
            cumulativeDistribution.add(new SymbolProbability(entry.getKey(), cumulativeWeight));
        }
        return cumulativeDistribution;
    }

    private String getSymbolFromCumulativeDistribution(List<SymbolProbability> cumulativeDistribution) {
        int totalWeight = cumulativeDistribution.get(cumulativeDistribution.size() - 1).getCumulativeWeight();
        int randomWeight = random.nextInt(totalWeight);
        for (SymbolProbability symbolProbability : cumulativeDistribution) {
            if (randomWeight < symbolProbability.getCumulativeWeight()) {
                return symbolProbability.getSymbol();
            }
        }
        return cumulativeDistribution.get(cumulativeDistribution.size() - 1).getSymbol();
    }

    @Getter
    @AllArgsConstructor
    private static class SymbolProbability {
        private final String symbol;
        private final int cumulativeWeight;
    }
}