package org.example.expert.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.time.LocalDateTime;

@Slf4j
@Component
public class LoggingInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) {
        // 요청 정보(HttpServletRequest) 사전 처리.
        String requestURI = request.getRequestURI();
        LocalDateTime requestTime = LocalDateTime.now();
        HttpSession session = request.getSession(false);

        // 어드민 인증 여부 확인, 인증되지 않은 경우 예외를 발생. `return = false`
        if (session == null || !"ADMIN".equals(session.getAttribute("ROLE"))) {
            log.warn("어드민 인증 실패 - URL: {}, IP: {}", requestURI, request.getRemoteAddr());
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            return false;
        }

        // 인증 성공 시 로깅 실시. `return = true`
        String adminId = (String) session.getAttribute("ADMIN_ID");
        log.info("어드민 접근 성공 - ID: {}, URL: {}, Time: {}", adminId, requestURI, requestTime);
        return true;
    }
}
