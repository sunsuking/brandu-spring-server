package shop.brandu.server.domain.auth.service;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import shop.brandu.server.core.properties.AuthProperties;
import shop.brandu.server.domain.auth.dto.AuthData.JwtToken;
import shop.brandu.server.domain.auth.entity.UserPrincipal;

import java.security.Key;
import java.util.Date;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
public class JwtTokenService {
    private final AuthProperties authProperties;
    private final UserPrincipalService userPrincipalService;
    private Key key;

    @PostConstruct
    public void init() {
        byte[] keyBytes = Decoders.BASE64.decode(authProperties.getSecretKey());
        this.key = Keys.hmacShaKeyFor(keyBytes);
    }

    private Map<String, Object> createHeader() {
        return Map.of("typ", "JWT", "alg", "HS256");
    }

    /**
     * OAuth2 인증을 통해 토큰 생성
     *
     * @param principal principal
     * @return JWT 토큰
     */
    public JwtToken generateTokenByOAuth2(UserPrincipal principal) {
        String authorities = principal.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));

        long now = System.currentTimeMillis();

        Date accessTokenExpire = new Date(now);
        Date refreshTokenExpire = new Date(now + authProperties.getRefreshTokenExpiry());

        String accessToken = Jwts.builder()
                .setHeader(createHeader())
                .claim("authorities", authorities)
                .setSubject(principal.getUsername())
                .setIssuedAt(new Date(now))
                .setExpiration(accessTokenExpire)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();

        String refreshToken = Jwts.builder()
                .setSubject(principal.getUsername())
                .setIssuedAt(new Date(now))
                .setExpiration(refreshTokenExpire)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();

        return new JwtToken(accessToken, refreshToken);
    }


    /**
     * accessToken을 파싱하여 Authentication 객체를 반환
     *
     * @param accessToken JWT 토큰
     * @return 인증된 Authentication 객체
     */
    public Authentication parseAuthentication(String accessToken) throws Exception {
        Claims claims = parseClaims(accessToken);

        assert claims != null;
        UserDetails principal = userPrincipalService.loadUserByUsername(claims.getSubject());

        return new UsernamePasswordAuthenticationToken(principal, "", principal.getAuthorities());
    }

    /**
     * 토큰 유효성 검사
     *
     * @param accessToken JWT 토큰
     * @return 유효한 토큰이면 true, 그렇지 않으면 false
     */
    public boolean validateToken(String accessToken) throws Exception {
        return parseClaims(accessToken) != null;
    }

    /**
     * accessToken 파싱
     *
     * @param accessToken JWT 토큰
     * @return 토큰의 클레임
     */
    private Claims parseClaims(String accessToken) throws Exception {
        String message;
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(accessToken)
                    .getBody();
        } catch (ExpiredJwtException e) {
            message = "유효기간이 만료된 토큰입니다.";
        } catch (MalformedJwtException e) {
            message = "잘못된 형식의 토큰입니다.";
        } catch (IllegalArgumentException e) {
            message = "잘못된 인자입니다.";
        } catch (Exception e) {
            message = "토큰 파싱 중 에러가 발생했습니다.";
        }
        throw new Exception(message);
    }
}
