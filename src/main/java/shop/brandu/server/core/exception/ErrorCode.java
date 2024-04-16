package shop.brandu.server.core.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

/**
 * Brandu 서버에서 발생하는 예외 코드 <br/>
 *
 * @author : sunsuking
 * @fileName : ErrorCode
 * @since : 4/16/24
 */
@RequiredArgsConstructor
@Getter
public enum ErrorCode {
    // Common (1000번대 에러 발생)
    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, 1000, "올바르지 않은 입력 값 입니다. 다시 한번 확인해주세요."),

    // Auth & User (2000번대 에러 발생)
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, 2000, "올바르지 않은 인증 토큰입니다. 다시 한번 확인해주세요."),
    ACCESS_DENIED(HttpStatus.FORBIDDEN, 2001, "접근 권한이 없습니다."),
    NOT_SUPPORTED_PROVIDER(HttpStatus.BAD_REQUEST, 2002, "지원하지 않는 소셜 로그인 제공자입니다."),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, 2003, "사용자를 찾을 수 없습니다."),

    ;
    private final HttpStatus status;
    private final int code;
    private final String message;
}
