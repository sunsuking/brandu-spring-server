package shop.brandu.server.core.advice;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import shop.brandu.server.core.exception.BranduException;
import shop.brandu.server.core.response.ErrorResponse;

@Slf4j
@RestControllerAdvice
public class BranduControllerAdvice {

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ResponseBody
    public ErrorResponse handleException(Exception exception) {
        log.error("Exception: {}", exception.getMessage());
        return ErrorResponse.of(exception);
    }

    @ExceptionHandler(BranduException.class)
    public ResponseEntity<ErrorResponse> handleBranduException(BranduException exception) {
        log.error("BranduException: {}", exception.getMessage());
        return ResponseEntity.status(exception.getErrorCode().getStatus())
                .body(ErrorResponse.of(exception));
    }
}
