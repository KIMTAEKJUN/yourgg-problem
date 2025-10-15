package com.backend.yourgg.service;

import com.backend.yourgg.dto.AccountDto;

public interface AccountService {
    AccountDto getAccountByRiotId(String gameName, String tagLine);
}
