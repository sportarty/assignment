package ru.kropotov;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import ru.kropotov.model.RequestConfig;
import ru.kropotov.model.Symbol;
import ru.kropotov.model.WinCombination;
import ru.kropotov.model.enums.Type;
import ru.kropotov.model.enums.When;

import java.util.*;
import java.util.stream.Collectors;

import static java.util.Objects.nonNull;
import static ru.kropotov.model.enums.When.LINEAR_SYMBOLS;

@Slf4j
@Getter
@AllArgsConstructor
class RewardCalculator {
    private final String[][] matrix;
    private final RequestConfig config;
    private final int bettingAmount;
    private final Map<String, List<String>> appliedWinningCombinations = new HashMap<>();
    private List<String> appliedBonusSymbols = new ArrayList<>();
    private final Map<String, Symbol> standardSymbols;
    private final Map<String, Symbol> bonusSymbols;

    public RewardCalculator(String[][] matrix, RequestConfig config, int bettingAmount) {
        this.matrix = matrix;
        this.config = config;
        this.bettingAmount = bettingAmount;
        this.standardSymbols = config.getSymbols().entrySet()
                .stream()
                .filter(e -> Type.STANDARD.equals(e.getValue().getType()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        this.bonusSymbols = config.getSymbols().entrySet()
                .stream()
                .filter(e -> Type.BONUS.equals(e.getValue().getType()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    public double calculateReward() {
        Map<String, Double> symbolRewards = calculateSymbolRewards();
        double totalReward = calculateTotalReward(symbolRewards);
        double finalReward = applyBonusSymbols(totalReward);
        log.debug("Total reward: {}", finalReward);
        return finalReward;
    }

    private Map<String, Double> calculateSymbolRewards() {
        Map<String, Double> symbolRewards = new HashMap<>();
        calculateSameSymbolRewards(symbolRewards);
        calculateLinearSymbolRewards(symbolRewards);
        return symbolRewards;
    }

    private void calculateSameSymbolRewards(Map<String, Double> symbolRewards) {
        HashMap<String, Integer> eachSymbolAmount = new HashMap<>();
        for (String[] rows : matrix) {
            for (String cell : rows) {
                if (standardSymbols.containsKey(cell)) {
                    eachSymbolAmount.merge(cell, 1, (oldValue, newValue) -> oldValue + 1);
                }
            }
        }
        config.getWinCombinations().entrySet().stream()
                .filter(v -> When.SAME_SYMBOLS.equals(v.getValue().getWhen()))
                .forEach(combination ->
                        eachSymbolAmount.forEach((symbol, count) -> {
                            if (combination.getValue().getCount().equals(count)) {
                                addSymbolReward(symbol, combination, symbolRewards);
                            }
                        }));
    }

    private void calculateLinearSymbolRewards(Map<String, Double> symbolRewards) {
        config.getWinCombinations().entrySet().stream()
                .filter(v -> LINEAR_SYMBOLS.equals(v.getValue().getWhen()))
                .forEach(combination ->
                        combination.getValue().getCoveredAreas().forEach(area -> {
                            String symbol = checkLinearSymbols(area);
                            if (nonNull(symbol)) {
                                addSymbolReward(symbol, combination, symbolRewards);
                            }
                        }));
    }

    private void addSymbolReward(String symbol, Map.Entry<String, WinCombination> combination,
                                 Map<String, Double> symbolRewards) {
        Symbol symbolConfig = config.getSymbols().get(symbol);
        if (symbolConfig != null && symbolConfig.getRewardMultiplier() != null) {
            double symbolReward = symbolConfig.getRewardMultiplier();
            double reward = bettingAmount * symbolReward * combination.getValue().getRewardMultiplier();
            log.debug("Reward name: {}. Reward details: {} x {}", combination.getValue().getGroup(), symbol, reward);
            symbolRewards.merge(symbol, reward, (oldValue, newValue) ->
                    oldValue * combination.getValue().getRewardMultiplier());
        } else {
            log.warn("Symbol {} or its reward multiplier is null", symbol);
        }
        appliedWinningCombinations.computeIfAbsent(symbol, k -> new ArrayList<>()).add(combination.getKey());
    }

    private double calculateTotalReward(Map<String, Double> symbolRewards) {
        return symbolRewards.values().stream().mapToDouble(Double::doubleValue).sum();
    }

    private double applyBonusSymbols(double totalReward) {
        if (totalReward == 0.0) {
            return totalReward;
        }
        double bonusMultiplier = 1.0;
        double extraBonus = 0.0;

        for (int row = 0; row < matrix.length; row++) {
            for (int col = 0; col < matrix[row].length; col++) {
                String symbol = matrix[row][col];
                Symbol symbolConfig = bonusSymbols.get(symbol);
                if (symbolConfig != null) {
                    Double rewardMultiplier = symbolConfig.getRewardMultiplier();
                    switch (symbolConfig.getImpact()) {
                        case MULTIPLY_REWARD:
                            if (rewardMultiplier != null) {
                                bonusMultiplier *= rewardMultiplier;
                                log.debug("Reward name: multiply_reward. Reward details: multiplier x{}", rewardMultiplier);
                                appliedBonusSymbols.add(symbol);
                            } else {
                                log.warn("Reward multiplier for symbol {} is null", symbol);
                            }
                            break;
                        case EXTRA_BONUS:
                            extraBonus += symbolConfig.getExtra();
                            log.debug("Reward name: extra_bonus. Reward details: add {} extra", symbolConfig.getExtra());
                            appliedBonusSymbols.add(symbol);
                            break;
                    }
                }
            }
        }

        return totalReward * bonusMultiplier + extraBonus;
    }

    private String checkLinearSymbols(List<String> coveredArea) {
        Set<String> symbols = new HashSet<>();
        for (String position : coveredArea) {
            String[] parts = position.split(":");
            int row = Integer.parseInt(parts[0]);
            int col = Integer.parseInt(parts[1]);
            symbols.add(matrix[row][col]);
        }
        if (symbols.size() == 1) {
            return symbols.iterator().next();
        } else {
            return null;
        }
    }
}