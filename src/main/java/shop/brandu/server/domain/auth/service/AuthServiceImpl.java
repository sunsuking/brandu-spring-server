package shop.brandu.server.domain.auth.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import shop.brandu.server.core.cache.CacheKey;
import shop.brandu.server.core.exception.BranduException;
import shop.brandu.server.core.exception.ErrorCode;
import shop.brandu.server.core.properties.AuthProperties;
import shop.brandu.server.domain.auth.dto.AuthData;
import shop.brandu.server.domain.auth.dto.AuthData.JwtToken;
import shop.brandu.server.domain.auth.entity.TokenValidate;
import shop.brandu.server.domain.user.entity.User;
import shop.brandu.server.domain.user.repository.UserRepository;

import java.time.Duration;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthServiceImpl implements AuthService {
    private final EmailService emailService;
    private final UserRepository userRepository;
    private final JwtTokenService jwtTokenService;
    private final PasswordEncoder passwordEncoder;
    private final AuthProperties authProperties;
    private final RedisTemplate<String, String> redisTemplate;

    @Transactional(readOnly = true)
    @Override
    public JwtToken signIn(AuthData.SignIn signIn) {
        // 사용자 아이디 기반으로 사용자 정보 조회
        User user = userRepository.findByUsername(signIn.getUsername()).orElseThrow(
                () -> new BranduException(ErrorCode.USER_NOT_FOUND)
        );

        // 비밀번호 검사 로직 수행
        if (!passwordEncoder.matches(signIn.getPassword(), user.getPassword())) {
            throw new BranduException(ErrorCode.USER_NOT_FOUND);
        }

        return jwtTokenService.generateTokenByLocal(user);
    }

    @Override
    public void signUp(AuthData.SignUp signUp) {
        if (existsByUsername(signUp.getUsername())) {
            throw new BranduException(ErrorCode.USER_ALREADY_EXISTS);
        }

        // 비밀번호 암호화 추가
        signUp.setPassword(passwordEncoder.encode(signUp.getPassword()));

        // 이메일 인증 코드 생성
        String code = generateCode();
        try {
            emailService.sendSignUpEmail(signUp.getEmail(), signUp.getNickname(), code);
            String key = CacheKey.emailCodeKey(signUp.getEmail());
            if (Boolean.TRUE.equals(redisTemplate.hasKey(key))) throw new BranduException(ErrorCode.EMAIL_SEND_FAILED);
            redisTemplate.opsForValue().set(key, code, Duration.ofMinutes(5));
        } catch (Exception e) {
            throw new BranduException(ErrorCode.EMAIL_SEND_FAILED);
        }
        userRepository.save(User.createLocalUser(signUp));
    }

    @Transactional(readOnly = true)
    @Override
    public void signOut(User user, JwtToken token) {
        TokenValidate tokenValidate = TokenValidate.of(user.getUsername(), token.getAccessToken(), token.getRefreshToken());
        String key = CacheKey.authenticationKey(user.getUsername());
        redisTemplate.opsForHash().putAll(key, tokenValidate.toMap());
        redisTemplate.expire(key, Duration.ofMillis(authProperties.getRefreshTokenExpiry()));
    }

    @Override
    public boolean confirm(String email, String code) {
        String key = CacheKey.emailCodeKey(email);
        if (code.equals(redisTemplate.opsForValue().get(key))) {
            userRepository.findByEmail(email).ifPresent(User::confirmEmail);
            redisTemplate.delete(key);
            return true;
        }
        return false;
    }


    private boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    private String generateCode() {
        return String.valueOf((int) (Math.random() * 900000) + 100000);
    }
}
