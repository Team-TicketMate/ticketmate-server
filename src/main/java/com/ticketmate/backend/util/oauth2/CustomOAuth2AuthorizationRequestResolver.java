package com.ticketmate.backend.util.oauth2;

import com.ticketmate.backend.service.auth.AuthService;
import com.ticketmate.backend.util.common.CommonUtil;
import com.ticketmate.backend.util.exception.CustomException;
import com.ticketmate.backend.util.exception.ErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.stereotype.Component;

import static com.ticketmate.backend.util.common.CommonUtil.*;

@Component
@Slf4j
public class CustomOAuth2AuthorizationRequestResolver implements OAuth2AuthorizationRequestResolver {
    private final DefaultOAuth2AuthorizationRequestResolver delegate;
    private final AuthService authService;

    @Value("${spring.security.app.redirect-uri.dev}")
    private String devRedirectUri;

    @Value("${spring.security.app.redirect-uri.prod}")
    private String prodRedirectUri;

    public CustomOAuth2AuthorizationRequestResolver(
            ClientRegistrationRepository clientRegistrationRepository, AuthService authService) {
        this.delegate = new DefaultOAuth2AuthorizationRequestResolver(
                clientRegistrationRepository, "/oauth2/authorization");
        this.authService = authService;
    }

    @Override
    public OAuth2AuthorizationRequest resolve(HttpServletRequest request) {
        OAuth2AuthorizationRequest authorizationRequest = delegate.resolve(request);
        return customizeAuthorizationRequest(request, authorizationRequest);
    }

    @Override
    public OAuth2AuthorizationRequest resolve(HttpServletRequest request, String clientRegistrationId) {
        OAuth2AuthorizationRequest authorizationRequest = delegate.resolve(request, clientRegistrationId);
        return customizeAuthorizationRequest(request, authorizationRequest);
    }

    private OAuth2AuthorizationRequest customizeAuthorizationRequest(
            HttpServletRequest request, OAuth2AuthorizationRequest authorizationRequest) {
        if (authorizationRequest == null) {
            return null;
        }

        String redirectUri = request.getParameter("redirectUri");
        if (!nvl(redirectUri, "").isEmpty() && isValidRedirectUri(redirectUri)) {
            // redirectUri를 Redis에 저장하고 키를 세션에 저장
            String redirectUriKey = authService.saveRedirectUri(redirectUri);
            request.getSession().setAttribute("redirectUriKey", redirectUriKey);
        }

        return authorizationRequest; // state는 변경하지 않음
    }

    // 리다이렉트 URI 검증
    private boolean isValidRedirectUri(String redirectUri) {
        if (redirectUri.equals(devRedirectUri) || redirectUri.equals(prodRedirectUri)) {
            log.error("요청된 redirectUri가 유효하지 않습니다.");
            throw new CustomException(ErrorCode.INVALID_REDIRECT_URI);
        }
    }
}
