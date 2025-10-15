package com.backend.yourgg.service;

import com.backend.yourgg.dto.MatchDetailDto;
import com.backend.yourgg.dto.MatchInfoDto;

import java.util.List;

public interface MatchService {
    List<MatchInfoDto> getRecentMatches(String puuid, int count);

    MatchDetailDto getMatchDetails(String matchId, String summonerName);

    String getQueueTypeName(Integer queueId);
}
