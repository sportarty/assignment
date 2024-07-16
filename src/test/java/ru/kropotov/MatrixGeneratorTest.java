package ru.kropotov;

import static org.junit.jupiter.api.Assertions.*;
import static ru.kropotov.Main.DEFAULT_BONUS_PERCENTAGE;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.kropotov.model.RequestConfig;
import ru.kropotov.model.enums.Type;

import java.io.File;
import java.io.IOException;

class MatrixGeneratorTest {
    private RequestConfig config;

    @BeforeEach
    void setUp() throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        config = objectMapper.readValue(new File("src/test/resources/config.json"), RequestConfig.class);
    }

    @Test
    void testGenerateMatrix() {
        MatrixGenerator matrixGenerator = new MatrixGenerator(config, DEFAULT_BONUS_PERCENTAGE);
        String[][] matrix = matrixGenerator.generateMatrix();
        assertNotNull(matrix);
        assertEquals(config.getRows(), matrix.length);
        assertEquals(config.getColumns(), matrix[0].length);
    }

    @Test
    void testAddBonusSymbols() {
        MatrixGenerator matrixGenerator = new MatrixGenerator(config, DEFAULT_BONUS_PERCENTAGE);
        String[][] matrix = matrixGenerator.generateMatrix();
        int bonusSymbolCount = 0;
        for (String[] strings : matrix) {
            for (String string : strings) {
                if (config.getSymbols().get(string) != null
                        && config.getSymbols().get(string).getType().equals(Type.BONUS)) {
                    bonusSymbolCount++;
                }
            }
        }
        assertTrue(bonusSymbolCount > 0);
    }
}
