package com.backend.yourgg.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MatchInfoDto {
    private String matchId;
    private Long gameCreation;
    private Long gameDuration;
    private String gameMode;
    private String gameType;
    private String gameVersion;
    private Integer queueId;
    private String platformId;
    private Integer seasonId;
}
