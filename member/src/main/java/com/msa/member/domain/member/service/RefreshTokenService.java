/*
package com.msa.member.domain.member.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class RefreshTokenService {
    private final RefreshTokenRepository refreshTokenRepository;

    @Transactional
    public RefreshToken addRefreshToken(RefreshToken refreshToken) {
        return refreshTokenRepository.save(refreshToken);
    }

    @Transactional
    public void deleteRefreshToken(String refreshToken) {
        log.info(refreshToken);
        refreshTokenRepository.findByTokenName(refreshToken).ifPresent(refreshTokenRepository::delete);
    }

    @Transactional(readOnly = true)
    public Optional<RefreshToken> findRefreshToken(String refreshToken) {
        return refreshTokenRepository.findByTokenName(refreshToken);
    }
}*/
