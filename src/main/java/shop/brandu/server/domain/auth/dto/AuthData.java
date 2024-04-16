package shop.brandu.server.domain.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 인증 관련 DTO 클래스 관리용 이너 클래스 <br/>
 *
 * @author : sunsuking
 * @fileName : AuthData
 * @since : 4/16/24
 */
public class AuthData {
    @AllArgsConstructor
    @Getter
    public static class JwtToken {
        private String accessToken;
        private String refreshToken;
    }
}
