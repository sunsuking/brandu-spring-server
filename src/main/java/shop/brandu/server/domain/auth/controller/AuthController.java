package shop.brandu.server.domain.auth.controller;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import shop.brandu.server.core.annotation.CurrentUser;
import shop.brandu.server.core.exception.BranduException;
import shop.brandu.server.core.exception.ErrorCode;
import shop.brandu.server.core.properties.AuthProperties;
import shop.brandu.server.core.response.SuccessResponse;
import shop.brandu.server.domain.auth.dto.AuthData.*;
import shop.brandu.server.domain.auth.service.AuthService;
import shop.brandu.server.domain.user.entity.User;

import java.util.Arrays;

@RestController
@Slf4j
@RequestMapping(value = "/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;
    private final AuthProperties authProperties;

    /**
     * 로그인
     *
     * @param signIn   {@link SignIn}
     * @param response {@link HttpServletResponse}
     * @return {@link JwtToken}
     */
    @PostMapping("/sign-in")
    @ResponseStatus(value = HttpStatus.OK)
    public SuccessResponse<JwtToken> signIn(
            @RequestBody @Validated SignIn signIn,
            HttpServletResponse response
    ) {
        JwtToken token = authService.signIn(signIn);
        response.addCookie(createCookie(token));
        return SuccessResponse.of(token);
    }

    /**
     * 신규 회원가입
     *
     * @param signUp   {@link SignUp}
     * @param response {@link HttpServletResponse}
     * @return 성공 응답
     */
    @PostMapping("/sign-up")
    @ResponseStatus(value = HttpStatus.CREATED)
    public SuccessResponse<Void> signUp(
            @RequestBody @Validated SignUp signUp,
            HttpServletResponse response
    ) {
        authService.signUp(signUp);
        return SuccessResponse.empty();
    }


    /**
     * 로그아웃
     *
     * @param user     {@link User}
     * @param request  {@link HttpServletRequest}
     * @param response {@link HttpServletResponse}
     */
    @DeleteMapping("/sign-out")
    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    public SuccessResponse<Void> signOut(
            @CurrentUser User user,
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        Cookie cookie = parseRefreshCookie(request);
        authService.signOut(user, cookie.getValue());
        removeCookie(response, "refreshToken");
        return SuccessResponse.empty();
    }

    /**
     * 비밀번호 찾기
     *
     * @return 성공 응답
     */
    @PostMapping("/find-password")
    @ResponseStatus(value = HttpStatus.OK)
    public SuccessResponse<Void> findPassword(
            @RequestBody @Validated FindPassword findPassword
    ) {
        authService.findPassword(findPassword.getEmail());
        return SuccessResponse.empty();
    }

    /**
     * 이메일 인증 코드 확인
     * TODO: POST 방식으로 변경 후 프론트와 연동 해야함.
     *
     * @param email 이메일
     * @param code  인증 코드
     * @return 성공 응답
     */
    @GetMapping("/confirm")
    @ResponseStatus(value = HttpStatus.OK)
    public SuccessResponse<Void> confirm(
            @RequestParam("type") String type,
            @RequestParam("email") String email,
            @RequestParam("code") String code
    ) {
        if (!authService.confirm(type, email, code)) {
            throw new IllegalArgumentException("인증 코드가 일치하지 않습니다.");
        }
        return SuccessResponse.empty();
    }

    @PostMapping("/resend-email")
    @ResponseStatus(value = HttpStatus.OK)
    public SuccessResponse<Void> resendEmail(
            @RequestBody @Validated ResendEmail resendEmail
    ) {
        authService.resendEmail(resendEmail.getEmail(), resendEmail.getType());
        return SuccessResponse.empty();
    }

    /**
     * 토큰 재발급
     *
     * @param request  {@link HttpServletRequest}
     * @param response {@link HttpServletResponse}
     * @return 새로운 토큰
     */
    @PostMapping("/refresh")
    @ResponseStatus(value = HttpStatus.OK)
    public SuccessResponse<JwtToken> refresh(
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        Cookie cookie = parseRefreshCookie(request);
        JwtToken newToken = authService.refresh(cookie.getValue());
        response.addCookie(createCookie(newToken));
        return SuccessResponse.of(newToken);
    }


    private Cookie parseRefreshCookie(HttpServletRequest request) {
        return Arrays.stream(request.getCookies())
                .filter(cookie -> cookie.getName().equals("refreshToken"))
                .findFirst()
                .orElseThrow(() -> new BranduException(ErrorCode.INVALID_TOKEN, "Refresh Token이 존재하지 않습니다."));
    }

    private Cookie createCookie(JwtToken token) {
        Cookie refreshTokenCookie = new Cookie("refreshToken", token.getRefreshToken());
        refreshTokenCookie.setHttpOnly(true);
        refreshTokenCookie.setMaxAge((int) authProperties.getRefreshTokenExpiry() / 1000);
        refreshTokenCookie.setPath("/");
        return refreshTokenCookie;
    }

    private void removeCookie(HttpServletResponse response, String key) {
        Cookie cookie = new Cookie(key, null);
        cookie.setHttpOnly(true);
        cookie.setMaxAge(0);
        cookie.setPath("/");
        response.addCookie(cookie);
    }
}
