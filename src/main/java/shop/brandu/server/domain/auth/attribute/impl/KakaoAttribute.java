package shop.brandu.server.domain.auth.attribute.impl;

import org.springframework.util.StringUtils;
import shop.brandu.server.domain.auth.attribute.OAuth2Attribute;

import java.util.Map;

/**
 * Kakao Oauth2 인증 정보 클래스 <br/>
 *
 * @author : sunsuking
 * @fileName : KakaoAttribute
 * @since : 4/16/24
 */
public class KakaoAttribute extends OAuth2Attribute {
    private Map<String, Object> properties;

    public KakaoAttribute(Map<String, Object> attributes) {
        super(attributes);
        this.properties = (Map<String, Object>) attributes.get("properties");
    }

    @Override
    public String getId() {
        return super.getAttributes().get("id").toString();
    }

    @Override
    public String getName() {
        return properties.get("nickname").toString();
    }

    @Override
    public String getEmail() {
        String email = (String) properties.get("email");
        if (!StringUtils.hasText(email)) {
            return this.getName() + "@kakao.com";
        }
        return email;
    }

    @Override
    public String getImageUrl() {
        return super.getAttributes().get("profile_image").toString();
    }
}
