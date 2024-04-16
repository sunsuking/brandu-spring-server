package shop.brandu.server.domain.auth.attribute.impl;

import org.springframework.util.StringUtils;
import shop.brandu.server.domain.auth.attribute.OAuth2Attribute;

import java.util.Map;

/**
 * Google Oauth2 인증 정보 클래스 <br/>
 *
 * @author : sunsuking
 * @fileName : GoogleAttribute
 * @since : 4/16/24
 */
public class GoogleAttribute extends OAuth2Attribute {
    public GoogleAttribute(Map<String, Object> attributes) {
        super(attributes);
    }

    @Override
    public String getId() {
        return super.getAttributes().get("sub").toString();
    }

    @Override
    public String getName() {
        return super.getAttributes().get("name").toString();
    }

    @Override
    public String getEmail() {
        String email = super.getAttributes().get("email").toString();
        if (!StringUtils.hasText(email)) {
            return this.getName() + "@gmail.com";
        }
        return email;
    }

    @Override
    public String getImageUrl() {
        return super.getAttributes().get("picture").toString();
    }
}
