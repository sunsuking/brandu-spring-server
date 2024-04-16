package shop.brandu.server.domain.user.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnDefault;
import shop.brandu.server.core.entity.BaseEntity;
import shop.brandu.server.domain.auth.attribute.OAuth2Attribute;
import shop.brandu.server.domain.auth.entity.ProviderType;

/**
 * 사용자 엔티티 클래스 <br/>
 *
 * @author : sunsuking
 * @fileName : User
 * @since : 4/17/24
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
    name = "users",
    uniqueConstraints = {
        @UniqueConstraint(name = "unique_users_username", columnNames = {"username"}),
        @UniqueConstraint(name = "unique_users_email", columnNames = {"email"})
    }
)
@Entity
public class User extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String username;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private ProviderType providerType;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    @ColumnDefault("USER")
    private RoleType roleType = RoleType.USER;

    @Column(nullable = false)
    @ColumnDefault("false")
    private boolean isEmailVerified = false;

    @Column(nullable = false)
    @ColumnDefault("false")
    private boolean isLocked = false;

    public static User createOAuthUser(OAuth2Attribute attribute, ProviderType provider) {
        User user = new User();
        user.username = attribute.getName();
        user.password = "password";
        user.email = attribute.getEmail();
        user.providerType = provider;
        user.roleType = RoleType.USER;
        return user;
    }
}
