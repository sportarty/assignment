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
public class StandardSymbol {
    @JsonProperty("column")
    private int column;
    @JsonProperty("row")
    private int row;
    @JsonProperty("symbols")
    private Map<String, Integer> symbols;
}