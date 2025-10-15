package com.backend.yourgg.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TeamDto {
    private Integer teamId;
    private Boolean win;
    private Integer towerKills;
    private Integer dragonKills;
    private Integer baronKills;
    private Integer riftHeraldKills;
    private Integer inhibitorKills;
    private Integer championKills;
    private Long totalGold;
    private Long totalDamageDealtToChampions;
}
