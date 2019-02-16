package light.rpc.util.json;

/**
 * Created by boying on 17/2/12.
 */

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import light.rpc.util.json.deserializers.LocalDateDeserializer;
import light.rpc.util.json.deserializers.LocalDateTimeDeserializer;
import light.rpc.util.json.deserializers.LocalTimeDeserializer;
import light.rpc.util.json.serializers.BigDecimalSerializer;
import light.rpc.util.json.serializers.LocalDateSerializer;
import light.rpc.util.json.serializers.LocalDateTimeSerializer;
import light.rpc.util.json.serializers.LocalTimeSerializer;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class JacksonHelper {
    private static final SimpleModule module = initModule();
    private static final ObjectMapper mapper;
    private static final ObjectMapper prettyMapper;

    public JacksonHelper() {
    }

    private static SimpleModule initModule() {
        return (new SimpleModule()).addSerializer(BigDecimal.class, new BigDecimalSerializer()).addSerializer(LocalTime.class, new LocalTimeSerializer()).addDeserializer(LocalTime.class, new LocalTimeDeserializer()).addSerializer(LocalDate.class, new LocalDateSerializer()).addDeserializer(LocalDate.class, new LocalDateDeserializer()).addSerializer(LocalDateTime.class, new LocalDateTimeSerializer()).addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer());
    }

    public static JavaType genJavaType(Type type) {
        return getMapper().getTypeFactory().constructType(type);
    }

    public static ObjectMapper getMapper() {
        return mapper;
    }

    public static ObjectMapper getPrettyMapper() {
        return prettyMapper;
    }

    static {
        mapper = (new ObjectMapper()).registerModule(module).configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false).configure(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_AS_NULL, true).configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        prettyMapper = mapper.copy().configure(SerializationFeature.INDENT_OUTPUT, true);
    }
}

