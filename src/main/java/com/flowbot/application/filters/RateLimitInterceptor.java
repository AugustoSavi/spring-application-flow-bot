package com.flowbot.application.filters;

import com.flowbot.application.configs.properties.RateLimitProperties;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.Cache;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class RateLimitInterceptor implements HandlerInterceptor {

    private final Cache<String, AtomicInteger> cache;
    private final int maxRequests;

    public RateLimitInterceptor(RateLimitProperties properties) {
        this.maxRequests = properties.getMaxRequests();
        this.cache = Caffeine.newBuilder()
                .expireAfterWrite(properties.getWindowSeconds(), TimeUnit.SECONDS)
                .build();
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        var ip = resolveIp(request);
        var key = ip + ":" + request.getMethod() + ":" + request.getRequestURI();
        var counter = cache.get(key, k -> new AtomicInteger(0));

        if (counter.incrementAndGet() > maxRequests) {
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.getWriter().write("Too many requests");
            return false;
        }

        return true;
    }

    private String resolveIp(HttpServletRequest request) {
        return request.getRemoteAddr();
    }
}
