package ru.chugunov.mdmadapter.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.chugunov.mdmadapter.exeption.JsonConversionException;

@Slf4j
@Component
@RequiredArgsConstructor
public class JsonUtils {

    private final ObjectMapper objectMapper;

    public String toJson(Object object) {
        try {
            return objectMapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            log.error("Ошибка преобразования объекта в JSON", e);

            throw new JsonConversionException();
        }
    }

    public <T> T fromJson(String json, Class<T> clazz) {
        try {
            return objectMapper.readValue(json, clazz);
        } catch (JsonProcessingException e) {
            log.error("Ошибка преобразования JSON в объект", e);

            throw new JsonConversionException();
        }
    }
}
