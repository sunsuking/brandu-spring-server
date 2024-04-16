package shop.brandu.server.domain.auth.handler;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;
import shop.brandu.server.core.properties.AuthProperties;
import shop.brandu.server.domain.auth.attribute.OAuth2Attribute;
import shop.brandu.server.domain.auth.attribute.OAuth2AttributeFactory;
import shop.brandu.server.domain.auth.dto.AuthData;
import shop.brandu.server.domain.auth.dto.AuthData.JwtToken;
import shop.brandu.server.domain.auth.entity.ProviderType;
import shop.brandu.server.domain.auth.entity.UserPrincipal;
import shop.brandu.server.domain.auth.service.JwtTokenService;

import java.io.IOException;

/**
 * OAuth2 인증 성공 핸들러 <br/>
 *
 * @author : sunsuking
 * @fileName : OAuth2SuccessHandler
 * @since : 4/16/24
 */
@Slf4j
@RequiredArgsConstructor
@Component
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {
    private final AuthProperties authProperties;
    private final JwtTokenService jwtTokenService;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        JwtToken token = jwtTokenService.generateTokenByOAuth2(authentication);
        log.debug("신규 토큰 발급 완료: {}", token.getAccessToken());

        String redirectUrl = UriComponentsBuilder.fromUriString(authProperties.getRedirectUrl())
                .queryParam("accessToken", token.getAccessToken())
                .queryParam("refreshToken", token.getRefreshToken())
                .toUriString();

        Cookie cookie = createCookie(token);
        response.addCookie(cookie);

        getRedirectStrategy().sendRedirect(request, response, redirectUrl);

        super.onAuthenticationSuccess(request, response, authentication);
    }

    private Cookie createCookie(JwtToken token) {
        Cookie refreshTokenCookie = new Cookie("refreshToken", token.getRefreshToken());
        refreshTokenCookie.setHttpOnly(true);
        refreshTokenCookie.setMaxAge((int) authProperties.getRefreshTokenExpiry() / 1000);
        refreshTokenCookie.setPath("/");
        return refreshTokenCookie;
    }
}
