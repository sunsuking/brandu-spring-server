package shop.brandu.server.domain.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import shop.brandu.server.domain.user.entity.User;

import java.util.Optional;

/**
 * 사용자 레포지토리 인터페이스 <br/>
 *
 * @author : sunsuking
 * @fileName : UserRepository
 * @since : 4/17/24
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
}
