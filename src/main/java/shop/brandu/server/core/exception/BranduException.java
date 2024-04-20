package shop.brandu.server.core.exception;

import lombok.Getter;
import org.springframework.validation.Errors;

/**
 * Brandu 서버에서 발생하는 런타임 에러 <br/>
 *
 * @author : sunsuking
 * @fileName : BaseException
 * @since : 4/16/24
 */
@Getter
public class BranduException extends RuntimeException {
    private final ErrorCode errorCode;
    private Errors errors;

    public BranduException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    public BranduException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public BranduException(ErrorCode errorCode, Errors errors) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
        this.errors = errors;
    }

    public BranduException(ErrorCode errorCode, String message, Errors errors) {
        super(message);
        this.errorCode = errorCode;
        this.errors = errors;
    }

    public boolean hasErrors() {
        return errors != null && errors.hasErrors();
    }
}
