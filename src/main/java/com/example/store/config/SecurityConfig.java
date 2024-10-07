package com.example.store.config;

import com.example.store.jwt.exception.CustomAuthenticationEntryPoint;
import com.example.store.jwt.filter.JwtAuthenticationFilter;
import com.example.store.jwt.provider.JwtAuthenticationProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HttpBasicConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthenticationProvider jwtAuthenticationProvider;
    private final CustomAuthenticationEntryPoint customAuthenticationEntryPoint;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        AuthenticationManagerBuilder sharedObject = http.getSharedObject(AuthenticationManagerBuilder.class);
        AuthenticationManager authenticationManager = sharedObject.build();
        http.authenticationManager(authenticationManager);

        http.csrf(csrf -> csrf.disable())
                .sessionManagement(configurer ->
                        configurer.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(HttpBasicConfigurer::disable)
                .addFilterBefore(new JwtAuthenticationFilter(authenticationManager), UsernamePasswordAuthenticationFilter.class)
                        .authenticationProvider(jwtAuthenticationProvider)
                                .exceptionHandling((exceptionConfig) ->
                                        exceptionConfig.authenticationEntryPoint(customAuthenticationEntryPoint));

        // CSRF 보안을 활성
        http.authorizeHttpRequests(authz -> authz
                // 로그인 없이 접근 가능한 엔드포인트
                .requestMatchers(HttpMethod.GET, "/boards/**", "/categories/**", "/discounts/**", "/products/**", "/reviews/**").permitAll()
                .requestMatchers(HttpMethod.POST, "/members/signup", "/members/login").permitAll()

                // user 권한만 접근 가능한 엔드포인트
                .requestMatchers(HttpMethod.POST, "/boards/**", "/cartItems/**", "/delivery", "/discounts/{id}", "/orders").hasAnyAuthority("USER", "ADMIN")
                .requestMatchers(HttpMethod.PUT, "/boards/**", "/cartItems/**", "/delivery").hasAnyAuthority("USER", "ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/boards/**", "/cartItems/**", "/delivery", "/discounts/users/**", "/orders/**").hasAnyAuthority("USER", "ADMIN")
                .requestMatchers(HttpMethod.GET, "/carts", "/members/info", "/orders/**").hasAnyAuthority("USER", "ADMIN")
                .requestMatchers(HttpMethod.POST, "/members/refreshToken").hasAnyAuthority("USER", "ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/members/logout", "/members/signout").hasAnyAuthority("USER", "ADMIN")

                // admin 권한만 접근 가능한 나머지 엔드포인트
                // 잠시 review 권한 부여 전에 다 허용
                //.anyRequest().hasAuthority("ADMIN")
                .anyRequest().permitAll()
        );
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}