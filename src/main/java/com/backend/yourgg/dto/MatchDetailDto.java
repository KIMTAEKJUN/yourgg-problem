package com.backend.yourgg.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MatchDetailDto {
    private MatchInfoDto matchInfo;
    private List<ParticipantDto> participants;
    private List<TeamDto> teams;
    private ParticipantDto targetSummoner; // 요청한 소환사의 정보
}
