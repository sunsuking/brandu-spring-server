package shop.brandu.server.core.response;

import io.netty.channel.unix.Errors;
import net.minidev.json.annotate.JsonIgnore;

public class SuccessResponse<T> extends BaseResponse<T> {
    @JsonIgnore
    protected Errors errors;

    private final static SuccessResponse<Void> EMPTY = new SuccessResponse<>();

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
}
