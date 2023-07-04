package com.yolt.creditscoring.controller.admin.users;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.node.TextNode;
import org.springframework.boot.jackson.JsonComponent;

import java.io.IOException;

@JsonComponent
public class Based64CombinedSerializer {

    private Based64CombinedSerializer() {
    }

    public static class Based64JsonSerializer extends JsonSerializer<Based64> {

        @Override
        public void serialize(Based64 based64, JsonGenerator jsonGenerator,
                              SerializerProvider serializerProvider) throws IOException {

            jsonGenerator.writeString(based64.toString());
        }

    }

    public static class Based64JsonDeserializer extends JsonDeserializer<Based64> {

        @Override
        public Based64 deserialize(JsonParser jsonParser,
                                   DeserializationContext deserializationContext) throws IOException {

            TextNode treeNode = jsonParser.getCodec().readTree(jsonParser);
            return Based64.fromEncoded(treeNode.asText());
        }
    }

}
