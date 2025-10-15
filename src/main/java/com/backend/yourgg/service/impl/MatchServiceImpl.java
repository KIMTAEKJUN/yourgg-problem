package com.backend.yourgg.service.impl;

import com.backend.yourgg.config.RiotApiConfig;
import com.backend.yourgg.dto.*;
import com.backend.yourgg.service.MatchService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service("matchService")
@RequiredArgsConstructor
public class MatchServiceImpl implements MatchService {

    private final WebClient webClient;
    private final RiotApiConfig riotApiConfig;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public List<MatchInfoDto> getRecentMatches(String puuid, int count) {
        try {
            log.info("매치 목록 요청: puuid={}, count={}", puuid, count);

            String url = riotApiConfig.getBaseUrl() + "/lol/match/v5/matches/by-puuid/" + puuid + "/ids";

            String response = webClient.get()
                    .uri(url + "?start=0&count=" + count)
                    .header(HttpHeaders.USER_AGENT, "YourggProblem/1.0")
                    .header("X-Riot-Token", riotApiConfig.getApiKey())
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            // 매치 ID 목록을 파싱
            JsonNode jsonNode = objectMapper.readTree(response);
            List<String> matchIds = new ArrayList<>();

            if (jsonNode.isArray()) {
                for (JsonNode matchIdNode : jsonNode) {
                    matchIds.add(matchIdNode.asText());
                }
            }

            // 매치 정보를 가져오기 위해 각 매치 ID에 대해 상세 정보를 조회
            List<MatchInfoDto> matchInfos = new ArrayList<>();
            for (String matchId : matchIds) {
                try {
                    MatchInfoDto matchInfo = getMatchBasicInfo(matchId);
                    if (isSummonersRiftMatch(matchInfo)) {
                        matchInfos.add(matchInfo);
                    }
                } catch (Exception e) {
                    log.warn("매치 정보 조회 실패: {}", matchId);
                }
            }

            log.info("매치 목록 조회 성공: {} 개", matchInfos.size());
            return matchInfos;

        } catch (WebClientResponseException e) {
            log.error("매치 목록 조회 실패: {}", e.getMessage());
            throw new RuntimeException("매치 목록을 가져올 수 없습니다.");
        } catch (Exception e) {
            log.error("매치 목록 조회 중 오류 발생: {}", e.getMessage());
            throw new RuntimeException("매치 목록 조회 중 오류가 발생했습니다.");
        }
    }

    @Override
    public MatchDetailDto getMatchDetails(String matchId, String summonerName) {
        try {
            log.info("매치 상세 정보 요청: matchId={}, summonerName={}", matchId, summonerName);

            String url = riotApiConfig.getBaseUrl() + "/lol/match/v5/matches/" + matchId;

            String response = webClient.get()
                    .uri(url)
                    .header(HttpHeaders.USER_AGENT, "YourggProblem/1.0")
                    .header("X-Riot-Token", riotApiConfig.getApiKey())
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            JsonNode matchData = objectMapper.readTree(response);

            // 매치 기본 정보 파싱
            MatchInfoDto matchInfo = parseMatchInfo(matchData);

            // 참가자 정보 파싱
            List<ParticipantDto> participants = parseParticipants(matchData);

            // 팀 정보 파싱
            List<TeamDto> teams = parseTeams(matchData);

            // 요청한 소환사의 정보 찾기 (게임네임으로만 비교)
            ParticipantDto targetSummoner = participants.stream()
                    .filter(p -> summonerName.equals(p.getSummonerName()))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("소환사를 매치에서 찾을 수 없습니다. 소환사명: " + summonerName));

            MatchDetailDto matchDetail = MatchDetailDto.builder()
                    .matchInfo(matchInfo)
                    .participants(participants)
                    .teams(teams)
                    .targetSummoner(targetSummoner)
                    .build();

            log.info("매치 상세 정보 조회 성공");
            return matchDetail;

        } catch (WebClientResponseException e) {
            log.error("매치 상세 정보 조회 실패: {}", e.getMessage(), e);
            throw new RuntimeException("매치 상세 정보를 가져올 수 없습니다.");
        } catch (Exception e) {
            log.error("매치 상세 정보 조회 중 오류 발생: {}", e.getMessage(), e);
            throw new RuntimeException("매치 상세 정보 조회 중 오류가 발생했습니다.");
        }
    }

    private MatchInfoDto getMatchBasicInfo(String matchId) throws Exception {
        String url = riotApiConfig.getBaseUrl() + "/lol/match/v5/matches/" + matchId;

        String response = webClient.get()
                .uri(url)
                .header(HttpHeaders.USER_AGENT, "YourggProblem/1.0")
                .header("X-Riot-Token", riotApiConfig.getApiKey())
                .retrieve()
                .bodyToMono(String.class)
                .block();

        JsonNode matchData = objectMapper.readTree(response);
        return parseMatchInfo(matchData);
    }

    private MatchInfoDto parseMatchInfo(JsonNode matchData) {
        JsonNode info = matchData.get("info");

        return MatchInfoDto.builder()
                .matchId(matchData.get("metadata").get("matchId").asText())
                .gameCreation(info.get("gameCreation").asLong())
                .gameDuration(info.get("gameDuration").asLong())
                .gameMode(info.get("gameMode").asText())
                .gameType(info.get("gameType").asText())
                .gameVersion(info.get("gameVersion").asText())
                .queueId(info.get("queueId").asInt())
                .platformId(info.get("platformId").asText())
                .seasonId(info.has("seasonId") ? info.get("seasonId").asInt() : null)
                .build();
    }

    private List<ParticipantDto> parseParticipants(JsonNode matchData) {
        List<ParticipantDto> participants = new ArrayList<>();
        JsonNode participantsNode = matchData.get("info").get("participants");

        for (JsonNode participantNode : participantsNode) {
            ParticipantDto participant = ParticipantDto.builder()
                    .summonerName(participantNode.get("riotIdGameName").asText())
                    .championName(participantNode.get("championName").asText())
                    .teamPosition(participantNode.get("teamPosition").asText())
                    .teamId(participantNode.get("teamId").asInt())
                    .win(participantNode.get("win").asBoolean())
                    .kills(participantNode.get("kills").asInt())
                    .deaths(participantNode.get("deaths").asInt())
                    .assists(participantNode.get("assists").asInt())
                    .totalMinionsKilled(participantNode.get("totalMinionsKilled").asInt())
                    .visionScore(participantNode.get("visionScore").asInt())
                    .goldEarned(participantNode.get("goldEarned").asInt())
                    .totalDamageDealtToChampions(participantNode.get("totalDamageDealtToChampions").asInt())
                    .totalDamageTaken(participantNode.get("totalDamageTaken").asInt())
                    .build();
            participants.add(participant);
        }

        return participants;
    }

    private List<TeamDto> parseTeams(JsonNode matchData) {
        List<TeamDto> teams = new ArrayList<>();
        JsonNode teamsNode = matchData.get("info").get("teams");

        for (JsonNode teamNode : teamsNode) {
            TeamDto team = TeamDto.builder()
                    .teamId(teamNode.get("teamId").asInt())
                    .win(teamNode.get("win").asBoolean())
                    .towerKills(teamNode.get("objectives").get("tower").get("kills").asInt())
                    .dragonKills(teamNode.get("objectives").get("dragon").get("kills").asInt())
                    .baronKills(teamNode.get("objectives").get("baron").get("kills").asInt())
                    .riftHeraldKills(teamNode.get("objectives").get("riftHerald").get("kills").asInt())
                    .inhibitorKills(teamNode.get("objectives").get("inhibitor").get("kills").asInt())
                    .championKills(teamNode.get("objectives").get("champion").get("kills").asInt())
                    .totalGold(teamNode.get("objectives").get("champion").get("kills").asLong())
                    .totalDamageDealtToChampions(teamNode.get("objectives").get("champion").get("kills").asLong())
                    .build();
            teams.add(team);
        }

        return teams;
    }

    private boolean isSummonersRiftMatch(MatchInfoDto matchInfo) {
        // 소환사의 협곡 큐 ID들 (일반, 솔랭, 자랭만 필터링)
        List<Integer> summonersRiftQueues = List.of(
                420, // 솔로 랭크
                430, // 일반
                440 // 자유 랭크
        );

        return summonersRiftQueues.contains(matchInfo.getQueueId());
    }

    /**
     * 큐 타입을 한글로 변환
     */
    public String getQueueTypeName(Integer queueId) {
        return switch (queueId) {
            case 420 -> "솔로 랭크";
            case 430 -> "일반";
            case 440 -> "자유 랭크";
            default -> "기타 (" + queueId + ")";
        };
    }
}
