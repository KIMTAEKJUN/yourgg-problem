package com.backend.yourgg.controller;

import com.backend.yourgg.dto.AccountDto;
import com.backend.yourgg.dto.MatchDetailDto;
import com.backend.yourgg.dto.MatchInfoDto;
import com.backend.yourgg.service.AccountService;
import com.backend.yourgg.service.MatchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Slf4j
@Controller
@RequiredArgsConstructor
public class MatchController {

    private final AccountService accountService;
    private final MatchService matchService;

    @GetMapping("/")
    public String index() {
        return "index";
    }

    @PostMapping("/search")
    public String searchSummoner(@RequestParam String riotId, RedirectAttributes redirectAttributes) {
        try {
            // Riot ID 파싱 (게임네임#태그라인)
            String[] parts = riotId.split("#");
            if (parts.length != 2) {
                redirectAttributes.addFlashAttribute("error", "올바른 형식을 입력해주세요. (예: YOURGG#GenG)");
                return "redirect:/";
            }

            String gameName = parts[0];
            String tagLine = parts[1];

            // 계정 정보 조회 (PUUID 획득)
            AccountDto account = accountService.getAccountByRiotId(gameName, tagLine);

            // 매치 목록 조회 (최근 10개)
            List<MatchInfoDto> recentMatches = matchService.getRecentMatches(account.getPuuid(), 10);

            if (recentMatches.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "최근 매치가 없습니다.");
                return "redirect:/";
            }

            // 가장 최근 매치 선택
            MatchInfoDto latestMatch = recentMatches.get(0);

            // 매치 상세 정보 조회 (게임네임만 사용)
            MatchDetailDto matchDetail = matchService.getMatchDetails(latestMatch.getMatchId(),
                    account.getGameName());

            redirectAttributes.addFlashAttribute("matchDetail", matchDetail);
            redirectAttributes.addFlashAttribute("account", account);
            redirectAttributes.addFlashAttribute("success", true);

            return "redirect:/match/" + latestMatch.getMatchId();

        } catch (Exception e) {
            log.error("소환사 검색 중 오류 발생: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/";
        }
    }

    @GetMapping("/match/{matchId}")
    public String matchDetail(@PathVariable String matchId, Model model) {
        // 플래시 속성에서 데이터 가져오기
        MatchDetailDto matchDetail = (MatchDetailDto) model.getAttribute("matchDetail");
        AccountDto account = (AccountDto) model.getAttribute("account");
        Boolean success = (Boolean) model.getAttribute("success");

        if (matchDetail == null || account == null || !Boolean.TRUE.equals(success)) {
            return "redirect:/";
        }

        model.addAttribute("matchDetail", matchDetail);
        model.addAttribute("account", account);

        return "match-detail";
    }
}
