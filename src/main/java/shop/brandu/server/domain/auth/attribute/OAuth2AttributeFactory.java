package shop.brandu.server.domain.auth.attribute;

import shop.brandu.server.core.exception.BranduException;
import shop.brandu.server.core.exception.ErrorCode;
import shop.brandu.server.domain.auth.attribute.impl.GithubAttribute;
import shop.brandu.server.domain.auth.attribute.impl.GoogleAttribute;
import shop.brandu.server.domain.auth.attribute.impl.KakaoAttribute;
import shop.brandu.server.domain.auth.attribute.impl.NaverAttribute;
import shop.brandu.server.domain.auth.entity.ProviderType;

import java.util.Map;

/**
 * Oauth2Attribute Factory 클래스 <br/>
 *
 * <p>
 *     인증 제공자별로 Oauth2Attribute를 생성하는 Factory 클래스
 * </p>
 *
 * @author : sunsuking
 * @fileName : OAuth2AttributeFactory
 * @since : 4/16/24
 */
public class OAuth2AttributeFactory {
    public static OAuth2Attribute parseAttribute(ProviderType provider, Map<String, Object> attributes) {
        return switch (provider) {
            case GOOGLE -> new GoogleAttribute(attributes);
            case GITHUB -> new GithubAttribute(attributes);
            case KAKAO -> new KakaoAttribute(attributes);
            case NAVER -> new NaverAttribute(attributes);
            default -> throw new BranduException(ErrorCode.NOT_SUPPORTED_PROVIDER);
        };
    }
}
