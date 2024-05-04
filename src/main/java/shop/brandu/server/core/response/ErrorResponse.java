package shop.brandu.server.core.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.springframework.validation.Errors;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.validation.SimpleErrors;
import shop.brandu.server.core.exception.BranduException;

import java.util.ArrayList;
import java.util.List;


public class ErrorResponse extends BaseResponse<Void> {

    public ErrorResponse(boolean isSuccess, int code, String message, Errors errors) {
        super(isSuccess, code, message);
        super.errors = parseErrors(errors);
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

    private List<CustomError> parseErrors(Errors errors) {
        if (errors == null) return new ArrayList<>();

        List<CustomError> customErrors = new ArrayList<>();
        for (FieldError error : errors.getFieldErrors()) {
            customErrors.add(new CustomError(error.getField(), error.getCode(), error.getDefaultMessage(), error.getObjectName()));
        }
        for (ObjectError error : errors.getGlobalErrors()) {
            customErrors.add(new CustomError(null, error.getCode(), error.getDefaultMessage(), error.getObjectName()));
        }
        return customErrors;
    }

    @JsonIgnore
    @Override
    public Void getData() {
        return super.getData();
    }
}
