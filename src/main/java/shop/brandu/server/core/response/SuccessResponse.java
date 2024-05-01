package shop.brandu.server.core.response;

import io.netty.channel.unix.Errors;
import net.minidev.json.annotate.JsonIgnore;

public class SuccessResponse<T> extends BaseResponse<T> {
    private final static SuccessResponse<Void> EMPTY = new SuccessResponse<>();
    @JsonIgnore
    protected Errors errors;

    public SuccessResponse() {
        super(true, 200, "success");
    }

    public SuccessResponse(T data) {
        super(true, 200, "success");
        super.data = data;
    }


    public static SuccessResponse<Void> empty() {
        return EMPTY;
    }

    public static <T> SuccessResponse<T> of(T data) {
        return new SuccessResponse<>(data);
    }
}
