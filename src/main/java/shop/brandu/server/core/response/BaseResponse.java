package shop.brandu.server.core.response;

import com.google.gson.Gson;
import lombok.Getter;
import org.springframework.validation.Errors;

@Getter
public abstract class BaseResponse<T> {
    private final boolean isSuccess;
    private final int code;
    private final String message;
    protected T data;
    protected Errors errors;

    public BaseResponse(boolean isSuccess, int code, String message) {
        this.isSuccess = isSuccess;
        this.code = code;
        this.message = message;
        this.data = null;
        this.errors = null;
    }

    public String toJson() {
        return new Gson().toJson(this);
    }
}
