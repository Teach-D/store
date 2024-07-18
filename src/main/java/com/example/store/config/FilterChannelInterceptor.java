package com.example.store.config;

import com.example.store.jwt.provider.JwtAuthenticationProvider;
import com.example.store.jwt.util.IfLogin;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Objects;

@Slf4j
@Component
@NoArgsConstructor
@Order(Ordered.HIGHEST_PRECEDENCE + 99)
public class FilterChannelInterceptor implements ChannelInterceptor, HandlerInterceptor {

    @Autowired
    private JwtAuthenticationProvider jwtAuthenticationProvider;

    public FilterChannelInterceptor(JwtAuthenticationProvider jwtAuthenticationProvider) {
        this.jwtAuthenticationProvider = jwtAuthenticationProvider;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        log.info(request.getRequestURI());
        return true;
    }

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor headerAccessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        log.info(">>>>>> headerAccessor : {}", headerAccessor);

        assert headerAccessor != null;
        log.info(">>>>> headAccessorHeaders : {}", headerAccessor.getCommand());
        if (Objects.equals(headerAccessor.getCommand(), StompCommand.CONNECT)
                || Objects.equals(headerAccessor.getCommand(), StompCommand.SEND)) { // 문제 발생 예상 지/점
            String token = removeBrackets(String.valueOf(headerAccessor.getNativeHeader("Authorization")));
            log.info("token : {}", token);
/*            log.info(">>>>>> Token resolved : {}", token);
            try {
                Authentication authentication = jwtTokenProvider.getAuthentication(token);
                Long accountId = ((UserDetailsImpl)authentication.getPrincipal()).getId();
                headerAccessor.addNativeHeader("AccountId", String.valueOf(accountId));
                log.info(">>>>>> AccountId is set to header : {}", accountId);
            } catch (Exception e) {
                log.warn(">>>>> Authentication Failed in FilterChannelInterceptor : ", e);
            }*/
        }
        return message;
    }

    @Override
    public void postSend(Message<?> message, MessageChannel channel, boolean sent) {
        ChannelInterceptor.super.postSend(message, channel, sent);
    }

    private String removeBrackets(String token) {
        if (token.startsWith("[") && token.endsWith("]")) {
            return token.substring(1, token.length() - 1);
        }
        return token;
    }
}