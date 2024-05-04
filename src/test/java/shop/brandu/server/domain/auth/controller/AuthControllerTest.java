package shop.brandu.server.domain.auth.controller;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import shop.brandu.server.common.BaseControllerTest;
import shop.brandu.server.core.exception.ErrorCode;
import shop.brandu.server.domain.auth.dto.AuthData;
import shop.brandu.server.domain.user.entity.User;
import shop.brandu.server.domain.user.repository.UserRepository;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.restdocs.restassured.RestAssuredRestDocumentation.document;

class AuthControllerTest extends BaseControllerTest {
    private static final String BASE_PATH = "/api/v1/auth";
    private User user;
    private AuthData.SignIn baseSignIn;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    public void init() {
        AuthData.SignUp signUp = AuthData.SignUp.of("test@gmail.com", "test", "test@gmail.com", passwordEncoder.encode("Test1234!@"));
        baseSignIn = new AuthData.SignIn();
        baseSignIn.setUsername("test@gmail.com");
        baseSignIn.setPassword("Test1234!@");
        user = User.createLocalUser(signUp);
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("로그인_정상_테스트")
    public void 로그인_정상_테스트() throws Exception {
        // * GIVEN
        String path = BASE_PATH + "/sign-in";
        user.confirmEmail();
        userRepository.save(user);

        AuthData.SignIn signIn = new AuthData.SignIn();
        signIn.setUsername(baseSignIn.getUsername());
        signIn.setPassword(baseSignIn.getPassword());

        RequestSpecification request = RestAssured.given(this.spec).body(signIn);

        // * WHEN
        Response response = request.filter(document("auth/sign-in",
                requestFields(
                        fieldWithPath("username").description("로그인 아이디"),
                        fieldWithPath("password").description("비밀번호")
                ),
                baseResponseFields(
                        fieldWithPath("data.accessToken").description("인증 토큰"),
                        fieldWithPath("data.refreshToken").description("인증 재발급 토큰")
                )
        )).when().post(path);

        // * THEN
        response.then()
                .statusCode(OK_CODE)
                .body("code", equalTo(OK_CODE))
                .body("message", equalTo("success"))
                .body("data.accessToken", notNullValue())
                .body("data.refreshToken", notNullValue())
                .body("isSuccess", equalTo(true));
    }

    @Test
    @DisplayName("로그인_입력필드_실패_테스트")
    public void 로그인_입력필드_실패_테스트() throws Exception {
        // * GIVEN
        String path = BASE_PATH + "/sign-in";
        user.confirmEmail();
        userRepository.save(user);

        AuthData.SignIn signIn = new AuthData.SignIn();
        signIn.setUsername("");
        signIn.setPassword("");

        RequestSpecification request = RestAssured.given(this.spec).body(signIn);

        // * WHEN
        Response response = request.filter(document("auth/sign-in",
                requestFields(
                        fieldWithPath("username").description("로그인 아이디"),
                        fieldWithPath("password").description("비밀번호")
                ),
                errorResponseFields()
        )).when().post(path);

        // * THEN
        response.then()
                .statusCode(BAD_REQUEST_CODE)
                .body("code", equalTo(ErrorCode.INVALID_INPUT_VALUE.getCode()))
                .body("message", equalTo("올바르지 않은 입력 값 입니다. 다시 한번 확인해주세요."))
                .body("isSuccess", equalTo(false));
    }

    @Test
    @DisplayName("로그인_이메일_미인증_테스트")
    public void 로그인_이메일_미인증_테스트() throws Exception {
        // * GIVEN
        String path = BASE_PATH + "/sign-in";
        userRepository.save(user);
        AuthData.SignIn signIn = new AuthData.SignIn();

        signIn.setUsername(baseSignIn.getUsername());
        signIn.setPassword(baseSignIn.getPassword());
        RequestSpecification request = RestAssured.given(this.spec).body(signIn);

        // * WHEN
        Response response = request.filter(document("auth/sign-in",
                requestFields(
                        fieldWithPath("username").description("로그인 아이디"),
                        fieldWithPath("password").description("비밀번호")
                ),
                errorResponseFields()
        )).when().post(path);

        // * THEN
        response.then()
                .statusCode(FORBIDDEN_CODE)
                .body("code", equalTo(ErrorCode.USER_EMAIL_NOT_VERIFIED.getCode()))
                .body("message", equalTo("이메일 인증이 필요합니다."))
                .body("isSuccess", equalTo(false));
    }

    @Test
    @DisplayName("로그인_존재하지_않은_아이디_테스트")
    public void 로그인_존재하지_않은_아이디_테스트() throws Exception {
        // * GIVEN
        String path = BASE_PATH + "/sign-in";
        AuthData.SignIn signIn = new AuthData.SignIn();
        signIn.setUsername(baseSignIn.getUsername());
        signIn.setPassword(baseSignIn.getPassword());

        RequestSpecification request = RestAssured.given(this.spec).body(signIn);

        // * WHEN
        Response response = request.filter(document("auth/sign-in",
                requestFields(
                        fieldWithPath("username").description("로그인 아이디"),
                        fieldWithPath("password").description("비밀번호")
                ),
                errorResponseFields()
        )).when().post(path);

        // THEN
        response.then()
                .statusCode(BAD_REQUEST_CODE)
                .body("code", equalTo(ErrorCode.USER_NOT_MATCH.getCode()))
                .body("message", equalTo("사용자 정보가 일치하지 않습니다."))
                .body("isSuccess", equalTo(false));
    }

    @Test
    @DisplayName("회원가입_정상_테스트")
    public void 회원가입_정상_테스트() throws Exception {
        // * GIVEN
        String path = BASE_PATH + "/sign-up";
        AuthData.SignUp signUp = AuthData.SignUp.of(baseSignIn.getUsername(), user.getNickname(), user.getEmail(), baseSignIn.getPassword());

        RequestSpecification request = RestAssured.given(this.spec).body(signUp);

        // * WHEN
        Response response = request.filter(document("auth/sign-up",
                requestFields(
                        fieldWithPath("username").description("아이디"),
                        fieldWithPath("nickname").description("닉네임"),
                        fieldWithPath("email").description("이메일"),
                        fieldWithPath("password").description("비밀번호")
                ),
                emptyResponseFields()
        )).when().post(path);

        // * THEN
        response.then()
                .statusCode(CREATED_CODE)
                .body("code", equalTo(OK_CODE))
                .body("message", equalTo("success"))
                .body("isSuccess", equalTo(true));
    }

    @Test
    @DisplayName("회원가입_입력필드_실패_테스트")
    public void 회원가입_입력필드_실패_테스트() throws Exception {
        // * GIVEN
        String path = BASE_PATH + "/sign-up";
        AuthData.SignUp signUp = AuthData.SignUp.of("", "", "", "");

        RequestSpecification request = RestAssured.given(this.spec).body(signUp);

        // * WHEN
        Response response = request.filter(document("auth/sign-up",
                requestFields(
                        fieldWithPath("username").description("아이디"),
                        fieldWithPath("nickname").description("닉네임"),
                        fieldWithPath("email").description("이메일"),
                        fieldWithPath("password").description("비밀번호")
                ),
                errorResponseFields()
        )).when().post(path);

        // * THEN
        response.then()
                .statusCode(BAD_REQUEST_CODE)
                .body("code", equalTo(ErrorCode.INVALID_INPUT_VALUE.getCode()))
                .body("message", equalTo("올바르지 않은 입력 값 입니다. 다시 한번 확인해주세요."))
                .body("isSuccess", equalTo(false));
    }

    @Test
    @DisplayName("회원가입_이메일_중복_테스트")
    public void 회원가입_이메일_중복_테스트() throws Exception {
        // * GIVEN
        String path = BASE_PATH + "/sign-up";
        userRepository.save(user);
        AuthData.SignUp signUp = AuthData.SignUp.of(baseSignIn.getUsername(), user.getNickname(), user.getEmail(), baseSignIn.getPassword());

        RequestSpecification request = RestAssured.given(this.spec).body(signUp);

        // * WHEN
        Response response = request.filter(document("auth/sign-up",
                requestFields(
                        fieldWithPath("username").description("아이디"),
                        fieldWithPath("nickname").description("닉네임"),
                        fieldWithPath("email").description("이메일"),
                        fieldWithPath("password").description("비밀번호")
                ),
                errorResponseFields()
        )).when().post(path);

        // * THEN
        response.then()
                .statusCode(BAD_REQUEST_CODE)
                .body("code", equalTo(ErrorCode.USER_ALREADY_EXISTS.getCode()))
                .body("message", equalTo("이미 존재하는 사용자입니다."))
                .body("isSuccess", equalTo(false));
    }

    @Test
    @DisplayName("회원가입_비밀번호_형식_테스트")
    public void 회원가입_비밀번호_형식_테스트() throws Exception {
        // * GIVEN
        String path = BASE_PATH + "/sign-up";
        AuthData.SignUp signUp = AuthData.SignUp.of(baseSignIn.getUsername(), user.getNickname(), user.getEmail(), "test");

        RequestSpecification request = RestAssured.given(this.spec).body(signUp);

        // * WHEN
        Response response = request.filter(document("auth/sign-up",
                requestFields(
                        fieldWithPath("username").description("아이디"),
                        fieldWithPath("nickname").description("닉네임"),
                        fieldWithPath("email").description("이메일"),
                        fieldWithPath("password").description("비밀번호")
                ),
                errorResponseFields()
        )).when().post(path);

        // * THEN
        response.then()
                .statusCode(BAD_REQUEST_CODE)
                .body("code", equalTo(ErrorCode.INVALID_INPUT_VALUE.getCode()))
                .body("message", equalTo("올바르지 않은 입력 값 입니다. 다시 한번 확인해주세요."))
                .body("isSuccess", equalTo(false));
    }

    @Test
    @DisplayName("로그아웃_정상_테스트")
    @WithMockUser(username = "test@gmail.com")
    public void 로그아웃_정상_테스트() throws Exception {
        // GIVEN
        String path = BASE_PATH + "/sign-out";
        user.confirmEmail();
        userRepository.save(user);

        RequestSpecification request = RestAssured.given(this.spec)
                .header("Authorization", "Bearer accessTokenForTest")
                .cookie("refreshToken", "refreshTokenForTest");

        // WHEN
        Response response = request.filter(document("auth/sign-out")).when().delete(path);

        // THEN
        response.then()
                .statusCode(OK_CODE)
                .body("code", equalTo(OK_CODE))
                .body("message", equalTo("success"))
                .body("isSuccess", equalTo(true));
    }

    @Test
    @DisplayName("로그아웃_리프레시_토큰_없음_테스트")
    @WithMockUser(username = "test@gmail.com")
    public void 로그아웃_리프레시_토큰_없음_테스트() throws Exception {
        // GIVEN
        String path = BASE_PATH + "/sign-out";
        user.confirmEmail();
        userRepository.save(user);

        RequestSpecification request = RestAssured.given(this.spec)
                .header("Authorization", "Bearer accessTokenForTest")
                .cookie("none", "none");


        // WHEN
        Response response = request.filter(document("auth/sign-out")).when().delete(path);


        // THEN
        response.then()
                .statusCode(UNAUTHORIZED_CODE)
                .body("code", equalTo(ErrorCode.INVALID_INPUT_VALUE.getCode()))
                .body("message", equalTo("올바르지 않은 입력 값 입니다. 다시 한번 확인해주세요."))
                .body("isSuccess", equalTo(false));
    }

}