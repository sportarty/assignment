package ru.kropotov.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.Map;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class RequestConfig {
    @JsonProperty("columns")
    private int columns;
    @JsonProperty("rows")
    private int rows;
    @JsonProperty("symbols")
    private Map<String, Symbol> symbols;
    @JsonProperty("probabilities")
    private Probabilities probabilities;
    @JsonProperty("win_combinations")
    private Map<String, WinCombination> winCombinations;

}


