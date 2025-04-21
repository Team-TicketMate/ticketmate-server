package com.ticketmate.backend.util.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

import static com.ticketmate.backend.util.common.CommonUtil.nvl;

@Slf4j
public class RedirectUriFilter extends OncePerRequestFilter {

    @Value("${spring.security.app.redirect-uri.dev}")
    private String devRedirectUri;

    @Value("${spring.security.app.redirect-uri.prod}")
    private String prodRedirectUri;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String redirectUri = request.getParameter("redirectUri");

        if (!nvl(redirectUri, "").isEmpty()) {
            log.debug("리다이랙트 URL이 지정되지 않아 기본경로로 설정합니다: {}", prodRedirectUri);
            request.getSession().setAttribute("redirectUri", prodRedirectUri);
        } else {
            log.debug("리다이랙트 URL이 요청되었습니다: {}", redirectUri);
        }
        filterChain.doFilter(request, response);
    }
}
