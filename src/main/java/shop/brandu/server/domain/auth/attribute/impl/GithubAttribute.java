package shop.brandu.server.domain.auth.attribute.impl;

import org.springframework.util.StringUtils;
import shop.brandu.server.domain.auth.attribute.OAuth2Attribute;

import java.util.Map;

/**
 * Github Oauth2 인증 정보 클래스 <br/>
 *
 * @author : sunsuking
 * @fileName : GithubAttribute
 * @since : 4/16/24
 */
public class GithubAttribute extends OAuth2Attribute {
    public GithubAttribute(Map<String, Object> attributes) {
        super(attributes);
    }

    @Override
    public String getId() {
        return super.getAttributes().get("id").toString();
    }

    @Override
    public String getName() {
        return super.getAttributes().get("name").toString();
    }

    @Override
    public String getEmail() {
        String email = super.getAttributes().get("email").toString();
        if (!StringUtils.hasText(email)) {
            return this.getName() + "@gihub.com";
        }
        return email;
    }

    @Override
    public String getImageUrl() {
        return super.getAttributes().get("avatar_url").toString();
    }
}
