package shop.brandu.server.domain.auth.controller;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import shop.brandu.server.core.annotation.CurrentUser;
import shop.brandu.server.core.properties.AuthProperties;
import shop.brandu.server.core.response.SuccessResponse;
import shop.brandu.server.domain.auth.dto.AuthData.JwtToken;
import shop.brandu.server.domain.auth.dto.AuthData.SignIn;
import shop.brandu.server.domain.auth.dto.AuthData.SignUp;
import shop.brandu.server.domain.auth.service.AuthService;
import shop.brandu.server.domain.user.entity.User;

@RestController
@Slf4j
@RequestMapping(value = "/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;
    private final AuthProperties authProperties;

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
     */
    @DeleteMapping("/sign-out")
    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    public SuccessResponse<Void> signOut(
            @CurrentUser User user,
            @RequestBody JwtToken token
    ) {
        authService.signOut(user, token);
        return SuccessResponse.empty();
    }

    @GetMapping("/confirm")
    @ResponseStatus(value = HttpStatus.OK)
    public SuccessResponse<Void> confirm(
            @RequestParam("email") String email,
            @RequestParam("code") String code
    ) {
        if (!authService.confirm(email, code)) {
            throw new IllegalArgumentException("인증 코드가 일치하지 않습니다.");
        }
        return SuccessResponse.empty();
    }

    private Cookie createCookie(JwtToken token) {
        Cookie refreshTokenCookie = new Cookie("refreshToken", token.getRefreshToken());
        refreshTokenCookie.setHttpOnly(true);
        refreshTokenCookie.setMaxAge((int) authProperties.getRefreshTokenExpiry() / 1000);
        refreshTokenCookie.setPath("/");
        return refreshTokenCookie;
    }
}
