package com.ticketmate.backend.util.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

import static com.ticketmate.backend.util.common.CommonUtil.nvl;

@Slf4j
public class RedirectUriFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String redirectUri = request.getParameter("redirectUri");

        if (!nvl(redirectUri, "").isEmpty()) {
            request.getSession().setAttribute("redirectUri", redirectUri);
        }
        filterChain.doFilter(request, response);
    }
}
