package shop.brandu.server.core.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.jackson.JsonComponent;
import org.springframework.validation.Errors;

import java.io.IOException;

@JsonComponent
@Slf4j
public class ErrorsSerializer extends JsonSerializer<Errors> {
    @Override
    public void serialize(Errors errors, JsonGenerator generator, SerializerProvider serializers) throws IOException {
        generator.writeStartArray();
        errors.getFieldErrors().forEach(error -> {
            try {
                generator.writeStartObject();
                generator.writeStringField("field", error.getField());
                generator.writeStringField("objectName", error.getObjectName());
                generator.writeStringField("code", error.getCode());
                generator.writeStringField("defaultMessage", error.getDefaultMessage());
                Object rejectedValue = error.getRejectedValue();
                if (rejectedValue != null) {
                    generator.writeStringField("rejectedValue", rejectedValue.toString());
                }
                generator.writeEndObject();
            } catch (IOException e) {
                log.error("Error while serializing errors", e);
            }
        });

        errors.getGlobalErrors().forEach(error -> {
            try {
                generator.writeStartObject();
                generator.writeStringField("objectName", error.getObjectName());
                generator.writeStringField("code", error.getCode());
                generator.writeStringField("defaultMessage", error.getDefaultMessage());
                generator.writeEndObject();
            } catch (IOException e) {
                log.error("Error while serializing errors", e);
            }
        });
        generator.writeEndArray();
    }
}
