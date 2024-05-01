package shop.brandu.server.core.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import shop.brandu.server.core.filter.JwtAuthenticationFilter;
import shop.brandu.server.domain.auth.handler.BranduAuthenticationDeniedHandler;
import shop.brandu.server.domain.auth.handler.BranduAuthenticationEntryPoint;
import shop.brandu.server.domain.auth.handler.OAuth2SuccessHandler;
import shop.brandu.server.domain.auth.service.BranduOAuth2UserService;

/**
 * Spring Security 설정 클래스 <br/>
 *
 * @author : sunsuking
 * @version : 1.0
 * @fileName : SecurityConfig
 * @since : 4/16/24
 */
@RequiredArgsConstructor
@EnableWebSecurity
@Configuration
public class SecurityConfig {
    private final BranduOAuth2UserService oAuth2UserService;
    private final OAuth2SuccessHandler successHandler;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final BranduAuthenticationDeniedHandler branduAuthenticationDeniedHandler;
    private final BranduAuthenticationEntryPoint branduAuthenticationEntryPoint;
    

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity security) throws Exception {
        return security
                .httpBasic(AbstractHttpConfigurer::disable)
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(AbstractHttpConfigurer::disable)
                .exceptionHandling(configurer -> configurer
                        .authenticationEntryPoint(branduAuthenticationEntryPoint)
                        .accessDeniedHandler(branduAuthenticationDeniedHandler)
                )
                .authorizeHttpRequests(
                        registry -> registry.requestMatchers("/api/v1/auth/sign-in", "/api/v1/auth/sign-up", "/actuator/**", "/h2-console/**").permitAll()
                                .anyRequest().authenticated()
                )
                .oauth2Login(configurer -> configurer
                        .authorizationEndpoint(authorization -> authorization.baseUri("/oauth2/authorization"))
                        .redirectionEndpoint(redirection -> redirection.baseUri("/*/oauth2/code/*"))
                        .userInfoEndpoint(endPoint -> endPoint.userService(oAuth2UserService))
                        .successHandler(successHandler)
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
