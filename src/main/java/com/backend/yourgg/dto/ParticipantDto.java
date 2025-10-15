package com.backend.yourgg.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ParticipantDto {
    private String summonerName;
    private String championName;
    private String teamPosition;
    private Integer teamId; // 팀 ID (100: 블루팀, 200: 레드팀)
    private Boolean win;
    private Integer kills;
    private Integer deaths;
    private Integer assists;
    private Integer totalMinionsKilled;
    private Integer visionScore;
    private Integer goldEarned;
    private Integer totalDamageDealtToChampions;
    private Integer totalDamageTaken;
    private String items; // JSON 형태로 저장
    private String summonerSpells; // JSON 형태로 저장
    private String runes; // JSON 형태로 저장
}
