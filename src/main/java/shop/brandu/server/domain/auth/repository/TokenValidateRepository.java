package shop.brandu.server.domain.auth.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import shop.brandu.server.domain.auth.entity.TokenValidate;

@Repository
public interface TokenValidateRepository extends CrudRepository<TokenValidate, Long> {
    TokenValidate findByRefreshToken(String refreshToken);
    TokenValidate findByAccessToken(String accessToken);
}
