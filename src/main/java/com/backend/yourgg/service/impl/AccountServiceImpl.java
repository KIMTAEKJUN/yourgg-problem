package com.backend.yourgg.service.impl;

import com.backend.yourgg.config.RiotApiConfig;
import com.backend.yourgg.dto.AccountDto;
import com.backend.yourgg.service.AccountService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

@Slf4j
@Service
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {

    private final WebClient webClient;
    private final RiotApiConfig riotApiConfig;

    @Override
    public AccountDto getAccountByRiotId(String gameName, String tagLine) {
        try {
            log.info("계정 정보 요청: {}#{}", gameName, tagLine);

            String url = riotApiConfig.getBaseUrl() + "/riot/account/v1/accounts/by-riot-id/" + gameName + "/"
                    + tagLine;

            AccountDto account = webClient.get()
                    .uri(url)
                    .header(HttpHeaders.USER_AGENT, "YourggProblem/1.0")
                    .header("X-Riot-Token", riotApiConfig.getApiKey())
                    .retrieve()
                    .bodyToMono(AccountDto.class)
                    .block();

            log.info("계정 정보 조회 성공: {}#{}", account.getGameName(), account.getTagLine());
            return account;

        } catch (WebClientResponseException e) {
            log.error("계정 정보 조회 실패: {}#{} - {}", gameName, tagLine, e.getMessage());
            throw new RuntimeException("계정을 찾을 수 없습니다: " + gameName + "#" + tagLine);
        } catch (Exception e) {
            log.error("계정 정보 조회 중 오류 발생: {}", e.getMessage());
            throw new RuntimeException("계정 정보 조회 중 오류가 발생했습니다.");
        }
    }
}
