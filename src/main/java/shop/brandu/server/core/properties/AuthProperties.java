package shop.brandu.server.core.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 인증 관련 설정 정보 <br/>
 *
 * @author : sunsuking
 * @fileName : AuthProperties
 * @since : 4/16/24
 */
@Getter @Setter
@ConfigurationProperties(prefix = "auth")
@Component
public class AuthProperties {
    private String redirectUrl;
    private String secretKey;
    private long tokenExpiry;
    private long refreshTokenExpiry;
}
