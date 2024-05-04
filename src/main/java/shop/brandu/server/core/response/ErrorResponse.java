package shop.brandu.server.core.response;

import net.minidev.json.annotate.JsonIgnore;
import org.springframework.validation.Errors;
import org.springframework.validation.SimpleErrors;
import shop.brandu.server.core.exception.BranduException;


public class ErrorResponse extends BaseResponse<Void> {
    @JsonIgnore
    protected transient Void data;

    public ErrorResponse(boolean isSuccess, int code, String message, Errors errors) {
        super(isSuccess, code, message);
        super.errors = errors;
    }

    public ErrorResponse(BranduException exception) {
        this(false, exception.getErrorCode().getCode(), exception.getMessage(), new SimpleErrors("error", exception.getMessage()));
    }

    public ErrorResponse(BranduException exception, String message) {
        this(false, exception.getErrorCode().getCode(), message, null);
    }

    public static ErrorResponse of(BranduException exception) {
        return new ErrorResponse(exception);
    }

    public static ErrorResponse of(BranduException exception, String message) {
        return new ErrorResponse(exception, message);
    }

    public static ErrorResponse of(Exception exception) {
        return new ErrorResponse(false, 500, exception.getMessage(), null);
    }
}
