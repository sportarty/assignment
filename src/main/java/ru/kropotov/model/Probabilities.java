package ru.kropotov.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.List;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class Probabilities {
    @JsonProperty("standard_symbols")
    private List<StandardSymbol> standardSymbols;
    @JsonProperty("bonus_symbols")
    private BonusSymbols bonusSymbols;
}