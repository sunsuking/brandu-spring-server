package shop.brandu.server.domain.auth.attribute;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Map;

/**
 * Oauth2 인증 정보 추상 클래스 <br/>
 *
 * 구글, 페이스북, 네이버 등의 Oauth2 인증 정보를 추상화한 클래스
 *
 * @author : sunsuking
 * @fileName : OAuth2Attribute
 * @since : 4/16/24
 */
@RequiredArgsConstructor
@Getter
public abstract class OAuth2Attribute {
    private final Map<String, Object> attributes;

    public abstract String getId();
    public abstract String getName();
    public abstract String getEmail();
    public abstract String getImageUrl();
}
