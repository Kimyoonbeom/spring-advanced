package org.example.expert.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;
import java.util.Objects;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class LoggingAspect {

    private final ObjectMapper objectMapper;

    @Pointcut("execution(* org.example.expert.domain.user.controller.UserAdminController.*(..))")
    public void controller(){
    }

    @Around("controller()")
    public Object loggingAdminApi(ProceedingJoinPoint joinPoint) throws Throwable{
        HttpServletRequest request = ((ServletRequestAttributes) Objects
                .requireNonNull(RequestContextHolder.getRequestAttributes()))
                .getRequest();

        // JWT 필터에서 설정한 속성 추출
        Long userId = (Long) request.getAttribute("userId");
        String email = (String) request.getAttribute("email");
        String userRole = (String) request.getAttribute("userRole");

        log.info("Request User ID = {}", userId);
        log.info("Request Email = {}", email);
        log.info("Request User Role = {}", userRole);
        log.info("Request Time = {}", LocalDateTime.now());
        log.info("Request URL = {}", request.getRequestURL());

        //Request Body
        for(Object arg : joinPoint.getArgs()){
            if(!(arg instanceof HttpServletRequest) && !(arg instanceof HttpServletResponse)){
                continue;
            }

            try {
                String requestBodyJson = new ObjectMapper().writeValueAsString(arg);
                log.info("RequestBody = {}", requestBodyJson);
            } catch (Exception e) {
                log.warn("Failed to serialize request body: {}", arg.getClass().getName());
            }
        }

        //메서드 실행
        Object result = joinPoint.proceed();

        //ResponseBody
        try{
            String responseBody = objectMapper.writeValueAsString(result);
            log.info("ResponseBody ={}", responseBody);
        } catch (Exception e) {
            log.warn("Failed to serialize response body: {}", result);
        }

        return result;
    }
}
