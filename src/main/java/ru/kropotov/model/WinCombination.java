package ru.kropotov.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import ru.kropotov.model.enums.When;

import java.util.List;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class WinCombination {
    @JsonProperty("reward_multiplier")
    private Double rewardMultiplier;
    private When when;
    @JsonProperty("count")
    private Integer count;
    @JsonProperty("group")
    private String group;
    @JsonProperty("covered_areas")
    private List<List<String>> coveredAreas;

    @JsonProperty("when")
    public void setWhen(String when) {
        this.when = When.valueOf(when.toUpperCase());
    }
}