package com.ticketmate.backend.util.oauth2;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.security.oauth2.client.OAuth2ClientProperties;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.stereotype.Component;

/**
 * 서비스별 OAuth2 클라이언트 등록 정보를 가지는 클래스
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class SocialClientRegistration {

    private final OAuth2ClientProperties properties;

    // 네이버
    public ClientRegistration naverClientRegistration() {
        OAuth2ClientProperties.Registration registration = properties.getRegistration().get("naver");
        OAuth2ClientProperties.Provider provider = properties.getProvider().get("naver");

        return ClientRegistration.withRegistrationId(registration.getClientName())
                .clientId(registration.getClientId())
                .clientSecret(registration.getClientSecret())
                .redirectUri(registration.getRedirectUri())
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .scope(registration.getScope())
                .authorizationUri(provider.getAuthorizationUri())
                .tokenUri(provider.getTokenUri())
                .userInfoUri(provider.getUserInfoUri())
                .userNameAttributeName(provider.getUserNameAttribute())
                .build();
    }


    // 카카오
    public ClientRegistration kakaoClientRegistration() {
        OAuth2ClientProperties.Registration registration = properties.getRegistration().get("kakao");
        OAuth2ClientProperties.Provider provider = properties.getProvider().get("kakao");

        return ClientRegistration.withRegistrationId(registration.getClientName())
                .clientId(registration.getClientId())
                .clientSecret(registration.getClientSecret())
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_POST)
                .redirectUri(registration.getRedirectUri())
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .scope(registration.getScope())
                .authorizationUri(provider.getAuthorizationUri())
                .tokenUri(provider.getTokenUri())
                .userInfoUri(provider.getUserInfoUri())
                .userNameAttributeName(provider.getUserNameAttribute())
                .build();
    }
}
