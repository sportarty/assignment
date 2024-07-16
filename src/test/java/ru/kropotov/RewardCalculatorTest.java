package ru.kropotov;

import static org.junit.jupiter.api.Assertions.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.kropotov.model.RequestConfig;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

class RewardCalculatorTest {
    private RequestConfig config;


    @BeforeEach
    public void setUp() throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        config = objectMapper.readValue(new File("src/test/resources/config.json"), RequestConfig.class);
    }

    @Test
    void testCalculateRewardWithSameSymbols() {
        String[][] matrix = new String[][] {
                {"A", "A", "B"},
                {"A", "C", "B"},
                {"A", "A", "B"}
        };
        RewardCalculator rewardCalculator = new RewardCalculator(matrix, config, 100);
        double reward = rewardCalculator.calculateReward();
        assertEquals(2600, reward); // Based on provided matrix and example configuration
    }

    @Test
    void testCalculateRewardWithBonusSymbols() {
        String[][] matrix = new String[][] {
                {"A", "A", "B"},
                {"A", "+1000", "B"},
                {"A", "A", "B"}
        };
        RewardCalculator rewardCalculator = new RewardCalculator(matrix, config, 100);
        double reward = rewardCalculator.calculateReward();
        assertEquals(3600, reward);
    }

    @Test
    void lostGameCalculateRewardTest() {
        String[][] matrix = new String[][] {
                {"A", "B", "C"},
                {"E", "B", "5x"},
                {"F", "D", "C"}
        };
        RewardCalculator rewardCalculator = new RewardCalculator(matrix, config, 100);
        double reward = rewardCalculator.calculateReward();
        assertEquals(0, reward);
    }

    @Test
    void testCalculateRewardWithSameLinearAndBonusSymbols() {
        String[][] matrix = new String[][] {
                {"D", "A", "+1000"},
                {"D", "D", "E"},
                {"C", "F", "D"}
        };
        RewardCalculator rewardCalculator = new RewardCalculator(matrix, config, 100);
        double reward = rewardCalculator.calculateReward();
        assertEquals(2500, reward);
    }

    @Test
    void testGetAppliedWinningCombinations() {
        String[][] matrix = new String[][] {
                {"A", "A", "B"},
                {"A", "C", "B"},
                {"A", "A", "B"}
        };
        RewardCalculator rewardCalculator = new RewardCalculator(matrix, config, 100);
        rewardCalculator.calculateReward();
        Map<String, List<String>> appliedCombinations = rewardCalculator.getAppliedWinningCombinations();
        assertAll(
                () -> assertTrue(appliedCombinations.containsKey("A")),
                () -> assertTrue(appliedCombinations.containsKey("B"))
        );
    }

    @Test
    void testGetAppliedBonusSymbol() {
        String[][] matrix = new String[][] {
                {"A", "A", "B"},
                {"A", "5x", "B"},
                {"A", "A", "+1000"}
        };
        RewardCalculator rewardCalculator = new RewardCalculator(matrix, config, 100);
        rewardCalculator.calculateReward();
        List<String> appliedBonusSymbols = rewardCalculator.getAppliedBonusSymbols();
        assertAll(
                () -> assertEquals(2, appliedBonusSymbols.size()),
                () -> assertTrue(appliedBonusSymbols.contains("5x"))
        );
    }
}
