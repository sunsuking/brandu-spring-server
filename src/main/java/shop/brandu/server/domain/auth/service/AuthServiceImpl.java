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
import shop.brandu.server.domain.auth.dto.AuthData.*;
import shop.brandu.server.domain.auth.entity.TokenValidate;
import shop.brandu.server.domain.user.entity.User;
import shop.brandu.server.domain.user.repository.UserRepository;

import java.time.Duration;
import java.util.Objects;

import static shop.brandu.server.domain.auth.dto.AuthData.*;

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

    /**
     * 로컬 사용자 로그인
     *
     * @param signIn {@link SignIn}
     * @return {@link JwtToken}
     */
    @Transactional(readOnly = true)
    @Override
    public JwtToken signIn(SignIn signIn) {
        // * 사용자 아이디 기반으로 사용자 정보 조회
        User user = userRepository.findByUsername(signIn.getUsername()).orElseThrow(
                () -> new BranduException(ErrorCode.USER_NOT_MATCH)
        );

        // * 비밀번호 검사 로직 수행
        if (!passwordEncoder.matches(signIn.getPassword(), user.getPassword())) {
            throw new BranduException(ErrorCode.USER_NOT_MATCH);
        }

        // * 사용자 상태 검사 - 계정 상태 확인
        if (user.isLocked()) {
            throw new BranduException(ErrorCode.USER_LOCKED);
        }

        // * 사용자 상태 검사 - 이메일 인증 여부 확인
        if (!user.isEmailVerified()) {
            String key = redisTemplate.opsForValue().get(CacheKey.emailConfirmCodeKey(user.getEmail()));
            if (key != null) throw new BranduException(ErrorCode.USER_EMAIL_NOT_VERIFIED);
            throw new BranduException(ErrorCode.USER_EMAIL_NOT_VERIFIED, "이메일 인증 유효시간이 지났습니다. 다시 인증해주세요.");
        }

        return jwtTokenService.generateTokenByLocal(user);
    }

    /**
     * 신규 회원가입
     *
     * @param signUp {@link SignUp}
     */
    @Override
    public void signUp(SignUp signUp) {
        if (existsByUsername(signUp.getUsername())) {
            throw new BranduException(ErrorCode.USER_ALREADY_EXISTS);
        }

        // 비밀번호 암호화 추가
        signUp.setPassword(passwordEncoder.encode(signUp.getPassword()));

        // 이메일 인증 코드 생성
        String code = generateCode();
        try {
            emailService.sendSignUpEmail(signUp.getEmail(), signUp.getNickname(), code);
            String key = CacheKey.emailConfirmCodeKey(signUp.getEmail());
            if (Boolean.TRUE.equals(redisTemplate.hasKey(key))) throw new BranduException(ErrorCode.EMAIL_SEND_FAILED);
            redisTemplate.opsForValue().set(key, code, Duration.ofMinutes(5));
        } catch (Exception e) {
            throw new BranduException(ErrorCode.EMAIL_SEND_FAILED);
        }
        userRepository.save(User.createLocalUser(signUp));
    }

    /**
     * 로그아웃
     *
     * @param user         {@link User}
     * @param refreshToken {@link JwtToken}
     */
    @Transactional(readOnly = true)
    @Override
    public void signOut(User user, String refreshToken) {
        TokenValidate tokenValidate = TokenValidate.of(user.getUsername(), refreshToken);
        String key = CacheKey.authenticationKey(user.getUsername());
        redisTemplate.opsForHash().putAll(key, tokenValidate.toMap());
        redisTemplate.expire(key, Duration.ofMillis(authProperties.getRefreshTokenExpiry()));
    }

    /**
     * 이메일 인증 코드 확인
     *
     * @param type  인증 타입
     * @param email 이메일
     * @param code  인증 코드
     * @return 성공 여부
     */
    @Override
    public boolean confirm(Confirm confirm) {
        String key;
        switch (confirm.getType()) {
            case "sign-up" -> key = CacheKey.emailConfirmCodeKey(confirm.getEmail());
            case "find-password" -> key = CacheKey.findPasswordCodeKey(confirm.getEmail());
            default -> throw new BranduException(ErrorCode.INVALID_INPUT_VALUE);
        }

        if (confirm.getCode().equals(redisTemplate.opsForValue().get(key))) {
            userRepository.findByEmail(confirm.getEmail()).ifPresent(User::confirmEmail);
            redisTemplate.delete(key);
            return true;
        }
        return false;
    }

    /**
     * 비밀번호 찾기
     *
     * @param email 이메일
     */
    @Override
    public void findPassword(String email) {
        String code = generateCode();
        try {
            String key = CacheKey.findPasswordCodeKey(email);
            if (Boolean.TRUE.equals(redisTemplate.hasKey(key))) throw new BranduException(ErrorCode.EMAIL_SEND_FAILED);
            redisTemplate.opsForValue().set(key, code, Duration.ofMinutes(5));
            emailService.sendFindPasswordEmail(email, code);
        } catch (Exception e) {
            throw new BranduException(ErrorCode.EMAIL_SEND_FAILED);
        }
    }

    @Override
    public void resendEmail(String email, String type) {
        userRepository.findByEmail(email).ifPresent(user -> {
            String code = generateCode();
            try {
                switch (type) {
                    case "sign-up" -> {
                        String key = CacheKey.emailConfirmCodeKey(email);
                        if (Boolean.TRUE.equals(redisTemplate.hasKey(key)))
                            throw new BranduException(ErrorCode.EMAIL_SEND_FAILED);
                        emailService.sendSignUpEmail(email, "", code);
                        redisTemplate.opsForValue().set(key, code, Duration.ofMinutes(5));
                    }
                    case "find-password" -> {
                        String key = CacheKey.findPasswordCodeKey(email);
                        if (Boolean.TRUE.equals(redisTemplate.hasKey(key)))
                            throw new BranduException(ErrorCode.EMAIL_SEND_FAILED);
                        redisTemplate.opsForValue().set(key, code, Duration.ofMinutes(5));
                        emailService.sendFindPasswordEmail(email, code);
                    }
                    default -> throw new BranduException(ErrorCode.INVALID_INPUT_VALUE);
                }
            } catch (Exception e) {
                throw new BranduException(ErrorCode.EMAIL_SEND_FAILED);
            }
        });
    }

    /**
     * 토큰 재발급
     *
     * @param refreshToken 재발급을 위한 리프레시 토큰
     * @return newJwtToken {@link JwtToken}
     */
    @Override
    public JwtToken refresh(String refreshToken) {
        // * 토큰 유효성 검사
        try {
            String username = jwtTokenService.getUsername(refreshToken);
            if (username == null) throw new BranduException(ErrorCode.INVALID_TOKEN);


            String key = CacheKey.authenticationKey(username);
            Object redisRefreshToken = redisTemplate.opsForHash().get(key, "refreshToken");
            if (Objects.equals(redisRefreshToken, refreshToken)) {
                throw new BranduException(ErrorCode.USER_ALREADY_SIGN_OUT);
            }

            return jwtTokenService.generateTokenByRefreshToken(refreshToken);
        } catch (Exception e) {
            throw new BranduException(ErrorCode.INVALID_TOKEN, e.getMessage());
        }
    }

    private boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    private String generateCode() {
        return String.valueOf((int) (Math.random() * 900000) + 100000);
    }
}
