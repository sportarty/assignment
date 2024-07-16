package ru.kropotov.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import ru.kropotov.model.enums.Impact;
import ru.kropotov.model.enums.Type;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class Symbol {
    @JsonProperty("reward_multiplier")
    private Double rewardMultiplier;
    @JsonProperty("extra")
    private Integer extra;
    private Type type;
    private Impact impact;

    @JsonProperty("type")
    public void setType(String type) {
        this.type = Type.valueOf(type.toUpperCase());
    }

    @JsonProperty("impact")
    public void setImpact(String impact) {
        this.impact = Impact.valueOf(impact.toUpperCase());
    }
}