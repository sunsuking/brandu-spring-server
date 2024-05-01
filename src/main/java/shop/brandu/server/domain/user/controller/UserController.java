package shop.brandu.server.domain.user.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 유저 컨트롤러 <br/>
 *
 * @author : sunsuking
 * @fileName : UserController
 * @since : 4/17/24
 */
@RequiredArgsConstructor
@RestController
@RequestMapping(value = "/api/v1/users")
public class UserController {
    /**
     * 개발 상태에서 인증을 처리를 확인하기 위한 테스트 API <br/>
     *
     * @return String - pong
     */
    @GetMapping("/ping")
    public String ping() {
        return "{\"message\": \"pong\"}";
    }
}
