package shop.brandu.server.common;

import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.filter.log.LogDetail;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.HttpStatus;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.restdocs.payload.FieldDescriptor;
import org.springframework.restdocs.payload.ResponseFieldsSnippet;
import shop.brandu.server.config.RedisTestContainerConfig;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.restassured.RestAssuredRestDocumentation.documentationConfiguration;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import({RedisTestContainerConfig.class})
@PropertySource("classpath:application-test.properties")
@ExtendWith(RestDocumentationExtension.class)
public class BaseControllerTest {
    protected static final String DEFAULT_RESTDOCS_PATH = "{class_name}/{method_name}";
    protected static final int OK_CODE = HttpStatus.OK.value();
    protected static final int CREATED_CODE = HttpStatus.CREATED.value();
    protected static final int BAD_REQUEST_CODE = HttpStatus.BAD_REQUEST.value();
    protected static final int UNAUTHORIZED_CODE = HttpStatus.UNAUTHORIZED.value();
    protected static final int FORBIDDEN_CODE = HttpStatus.FORBIDDEN.value();
    protected static final int NOT_FOUND_CODE = HttpStatus.NOT_FOUND.value();
    protected static final int SERVER_ERROR_CODE = HttpStatus.INTERNAL_SERVER_ERROR.value();

    @LocalServerPort
    public int port;

    protected RequestSpecification spec;

    public static ResponseFieldsSnippet baseResponseFields(FieldDescriptor... descriptors) {
        List<FieldDescriptor> baseResponse = Arrays.asList(
                fieldWithPath("isSuccess").description("성공 여부"),
                fieldWithPath("code").description("응답 코드"),
                fieldWithPath("message").description("응답 메시지")
        );
        List<FieldDescriptor> response = Arrays.asList(descriptors);
        return responseFields(Stream.concat(baseResponse.stream(), response.stream()).toList());
    }

    public static ResponseFieldsSnippet errorResponseFields() {
        return baseResponseFields(fieldWithPath("errors[]").description("에러 목록"));
    }

    public static ResponseFieldsSnippet emptyResponseFields() {
        return baseResponseFields(fieldWithPath("data").description("응답 메시지"));
    }

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
    }

    @BeforeEach
    void setUpRestDocs(RestDocumentationContextProvider restDocumentation) {
        this.spec = new RequestSpecBuilder()
                .setPort(port)
                .setContentType("application/json")
                .addHeader("Accept", "application/json")
                .addFilter(documentationConfiguration(restDocumentation)
                        .operationPreprocessors()
                        .withRequestDefaults(prettyPrint())
                        .withResponseDefaults(prettyPrint()))
                .log(LogDetail.ALL)
                .build();
    }
}
