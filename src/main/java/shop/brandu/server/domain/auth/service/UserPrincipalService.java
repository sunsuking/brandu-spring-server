package shop.brandu.server.domain.auth.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import shop.brandu.server.core.exception.BranduException;
import shop.brandu.server.core.exception.ErrorCode;
import shop.brandu.server.domain.auth.entity.UserPrincipal;
import shop.brandu.server.domain.user.entity.User;
import shop.brandu.server.domain.user.repository.UserRepository;

@RequiredArgsConstructor
@Service
public class UserPrincipalService implements UserDetailsService {
    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username).orElseThrow(
                () -> new BranduException(ErrorCode.USER_NOT_FOUND)
        );
        return new UserPrincipal(user);
    }
}
