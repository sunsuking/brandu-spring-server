package shop.brandu.server.domain.auth.controller;

import com.nimbusds.jose.shaded.gson.Gson;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import shop.brandu.server.core.exception.ErrorCode;
import shop.brandu.server.core.response.ErrorResponse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@PropertySource("classpath:application-test.properties")
class AuthControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("인증을_하지않고_로그인_테스트")
    public void 인증을_하지않고_로그인_테스트() throws Exception {
        // GIVEN
        String path = "/api/v1/users/ping";

        // WHEN
        ResultActions actions = mockMvc.perform(get("/api/v1/users/test").contentType(MediaType.APPLICATION_JSON));

        // THEN
        actions.andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("인증을_포함한_로그인_테스트")
    @WithMockUser(username = "test", roles = "USER")
    public void 인증을_포함한_로그인_테스트() throws Exception {
        // GIVEN
        String path = "/api/v1/users/ping";

        // WHEN
        ResultActions actions = mockMvc.perform(get("/api/v1/users/test").contentType(MediaType.APPLICATION_JSON));

        // THEN
        actions.andExpect(status().isOk());
    }

    @Test
    @DisplayName("미인증_요청_에러메시지_테스트")
    public void 미인증_요청_에러메시지_테스트() throws Exception {
        // GIVEN
        String path = "/api/v1/users/ping";

        // WHEN
        MockHttpServletResponse response = mockMvc.perform(get(path).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized())
                .andReturn()
                .getResponse();

        ErrorResponse errorResponse = new Gson().fromJson(response.getContentAsString(), ErrorResponse.class);

        // THEN
        assertThat(errorResponse.getCode()).isEqualTo(ErrorCode.INVALID_TOKEN.getCode());
        assertThat(errorResponse.getMessage()).isEqualTo("유효한 JWT 토큰이 없습니다");
        assertThat(errorResponse.isSuccess()).isEqualTo(false);
    }
}