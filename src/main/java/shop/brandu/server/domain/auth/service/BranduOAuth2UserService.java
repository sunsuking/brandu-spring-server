package shop.brandu.server.domain.auth.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import shop.brandu.server.domain.auth.attribute.OAuth2Attribute;
import shop.brandu.server.domain.auth.attribute.OAuth2AttributeFactory;
import shop.brandu.server.domain.auth.entity.ProviderType;
import shop.brandu.server.domain.auth.entity.UserPrincipal;
import shop.brandu.server.domain.user.entity.User;
import shop.brandu.server.domain.user.repository.UserRepository;

/**
 * 인증 관련 서비스 클래스 <br/>
 *
 * @author : sunsuking
 * @fileName : OAuth2UserService
 * @since : 4/17/24
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class BranduOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {
    private final UserRepository userRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        DefaultOAuth2UserService userService = new DefaultOAuth2UserService();
        OAuth2User user = userService.loadUser(userRequest);

        ProviderType provider = ProviderType.valueOf(userRequest.getClientRegistration().getRegistrationId().toUpperCase());
        OAuth2Attribute attribute = OAuth2AttributeFactory.parseAttribute(provider, user.getAttributes());
        log.debug("OAUTH2 기반의 로그인 요청 -> provider: {}, user: {}", provider, user);

        // * 해당 요청은 OAuth2 기반의 요청이기에 일치하는 사용자가 없다면 새로운 사용자를 생성한다.
        User findUser = userRepository.findByUsername(user.getAttribute("email")).orElseGet(() -> User.createOAuthUser(attribute, provider));

        // * 기존 인증 방식과 새로운 인증 방식이 다르다면 로그를 남긴다.
        if (findUser.getProviderType() != provider)  {
            log.debug("기존 인증 방식: {}, 새로운 인증 방식: {}", findUser.getProviderType(), provider);
        }

        return new UserPrincipal(findUser, user.getAttributes());
    }
}
