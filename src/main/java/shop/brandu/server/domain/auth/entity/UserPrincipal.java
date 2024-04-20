package shop.brandu.server.domain.auth.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;
import shop.brandu.server.domain.user.entity.User;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * 인증 전용 유저 클래스 <br/>
 * 실제 데이터베이스에는 저장되지 않지만, Spring Security 와 Spring Oauth2 에서 인증을 위해 사용함.
 *
 * @author sunsuking
 * @see UserDetails
 * @see OAuth2User
 */
@RequiredArgsConstructor
@AllArgsConstructor
public class UserPrincipal implements UserDetails, OAuth2User {
    @Getter
    private final transient User user;
    private transient Map<String, Object> attributes;

    @Override
    public String getName() {
        return user.getUsername();
    }

    @Override
    public Map<String, Object> getAttributes() {
        return this.attributes;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(() -> user.getRoleType().name());
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        return user.getUsername();
    }

    @Override
    public boolean isAccountNonExpired() {
        return false;
    }

    @Override
    public boolean isAccountNonLocked() {
        return false;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return false;
    }

    @Override
    public boolean isEnabled() {
        return user.isEmailVerified() && !user.isLocked();
    }
}
