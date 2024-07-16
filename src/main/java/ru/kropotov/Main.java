package ru.kropotov;


import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.cli.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.kropotov.model.RequestConfig;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    private static final String CONFIG = "config";
    private static final String BETTING_AMOUNT = "betting-amount";
    private static final String BONUS_PERCENTAGE = "bonus-percentage";
    public static final double DEFAULT_BONUS_PERCENTAGE = 0.5;

    public static void main(String[] args) {
        Options options = createOptions();
        CommandLine cmd = parseCommandLineArguments(options, args);

        if (cmd == null || !validateArguments(cmd, options)) {
            return;
        }

        String configPath = cmd.getOptionValue(CONFIG);
        int bettingAmount = getBettingAmount(cmd.getOptionValue(BETTING_AMOUNT));
        double bonusPercentage = getBonusPercentage(cmd.getOptionValue(BONUS_PERCENTAGE));

        if (bettingAmount < 0 || !isValidConfigFile(configPath)) {
            return;
        }

        RequestConfig config = readConfigFile(configPath);
        if (config == null) {
            return;
        }

        processGame(config, bettingAmount, bonusPercentage);
    }

    private static Options createOptions() {
        Options options = new Options();
        options.addOption("c", CONFIG, true, "Path to config file");
        options.addOption("b", BETTING_AMOUNT, true, "Betting amount");
        options.addOption("p", BONUS_PERCENTAGE, true, "Bonus symbol hit percentage");
        return options;
    }

    private static CommandLine parseCommandLineArguments(Options options, String[] args) {
        CommandLineParser parser = new DefaultParser();
        try {
            return parser.parse(options, args);
        } catch (ParseException e) {
            logger.error("Error parsing command line arguments", e);
            printHelp(options);
            return null;
        }
    }

    private static boolean validateArguments(CommandLine cmd, Options options) {
        if (!cmd.hasOption("c") || !cmd.hasOption("b")) {
            logger.error("Missing required options: config file path (-c) and betting amount (-b) are required.");
            printHelp(options);
            return false;
        }
        return true;
    }

    private static int getBettingAmount(String bettingAmountStr) {
        try {
            return Integer.parseInt(bettingAmountStr);
        } catch (NumberFormatException e) {
            logger.error("Invalid betting amount. It must be a number.");
            return -1;
        }
    }

    private static double getBonusPercentage(String bonusPercentageString) {
        if (bonusPercentageString == null) {
            return DEFAULT_BONUS_PERCENTAGE;
        }
        try {
            double bonusPercentage = Double.parseDouble(bonusPercentageString) / 100.0;
            if (bonusPercentage < 0 || bonusPercentage > 1) {
                logInvalidBonusPercentage();
                return DEFAULT_BONUS_PERCENTAGE;
            }
            return bonusPercentage;
        } catch (NumberFormatException e) {
            logInvalidBonusPercentage();
            return DEFAULT_BONUS_PERCENTAGE;
        }
    }

    private static void logInvalidBonusPercentage() {
        logger.warn("Invalid bonus symbol hit percentage. It must be a number from 0 to 100."
                + " Default value (50%) is set");
    }

    private static boolean isValidConfigFile(String configPath) {
        File configFile = new File(configPath);
        if (!configFile.exists() || !configFile.isFile()) {
            logger.error("Config file does not exist or is not a file: " + configPath);
            return false;
        }
        return true;
    }

    private static RequestConfig readConfigFile(String configPath) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.readValue(new File(configPath), RequestConfig.class);
        } catch (IOException e) {
            logger.error("Error reading config file", e);
            return null;
        }
    }

    private static void processGame(RequestConfig config, int bettingAmount, double bonusPercentage) {
        double reward = 0.0;
        String[][] finalMatrix = null;
        Map<String, List<String>> appliedWinningCombinations = new HashMap<>();
        List<String> appliedBonusSymbols = new ArrayList<>();
        int count = 0;
        while (reward <= 0) {
            MatrixGenerator matrixGenerator = new MatrixGenerator(config, bonusPercentage);
            String[][] matrix = matrixGenerator.generateMatrix();

            RewardCalculator rewardCalculator = new RewardCalculator(matrix, config, bettingAmount);
            reward = rewardCalculator.calculateReward();
            logger.info("Generated matrix and calculated reward: {}", reward);

            if (reward > 0) {
                finalMatrix = matrix;
                appliedWinningCombinations = rewardCalculator.getAppliedWinningCombinations();
                appliedBonusSymbols = rewardCalculator.getAppliedBonusSymbols();
            }
            count++;
        }
        logger.info("count: " + count);

        printResult(finalMatrix, reward, appliedWinningCombinations, appliedBonusSymbols);
    }

    private static void printResult(String[][] finalMatrix, double reward,
                                    Map<String, List<String>> appliedWinningCombinations,
                                    List<String> appliedBonusSymbols) {
        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("matrix", finalMatrix);
        result.put("reward", reward);
        result.put("applied_winning_combinations", appliedWinningCombinations);
        result.put("applied_bonus_symbol", appliedBonusSymbols);

        try {
            String jsonResult = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(result);
            logger.info("Final Result: {}", jsonResult);
        } catch (IOException e) {
            logger.error("Error writing JSON result", e);
        }
    }

    private static void printHelp(Options options) {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("java -jar <your-jar-file>", options);
    }
}