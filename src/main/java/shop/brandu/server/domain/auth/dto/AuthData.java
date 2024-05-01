package shop.brandu.server.domain.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;

/**
 * 인증 관련 DTO 클래스 관리용 이너 클래스 <br/>
 *
 * @author : sunsuking
 * @fileName : AuthData
 * @since : 4/16/24
 */
public class AuthData {
    @AllArgsConstructor
    @Getter
    public static class JwtToken {
        private String accessToken;
        private String refreshToken;
    }

    @Data
    public static class SignIn {
        @NotBlank(message = "이메일을 입력해주세요.")
        private String username;

        @NotBlank(message = "비밀번호를 입력해주세요.")
        private String password;
    }

    @Data
    public static class SignUp {
        @Email(message = "이메일 형식이 아닙니다.")
        @NotBlank(message = "이메일을 입력해주세요.")
        private String username;

        private String nickname;

        private String email;

        @Pattern(regexp = "(?=.*[0-9])(?=.*[a-zA-Z])(?=.*\\W)(?=\\S+$).{8,16}", message = "비밀번호는 8~16자 영문 대 소문자, 숫자, 특수문자를 사용하세요.")
        @NotBlank(message = "비밀번호를 입력해주세요.")
        private String password;
    }
}
