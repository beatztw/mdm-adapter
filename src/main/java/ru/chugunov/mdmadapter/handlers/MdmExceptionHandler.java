package ru.chugunov.mdmadapter.handlers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.chugunov.mdmadapter.dto.common.CommonResponse;
import ru.chugunov.mdmadapter.exeption.BusinessException;

import java.util.UUID;

@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
public class MdmExceptionHandler {

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(Exception.class)
    public CommonResponse<?> handleException(Exception e){
        log.error("Непредвиденное исключение: {}", e.getMessage(), e);

        return CommonResponse.builder()
                .id(UUID.randomUUID())
                .errorMessage("Непредвиденное исключение: " + e.getMessage())
                .build();
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(BusinessException.class)
    public CommonResponse<?> handleOtpException(BusinessException e){
        log.warn("Перехвачена ошибка выполнения бизнес-логики: {}", e.getMessage(), e);

        return CommonResponse.builder()
                .id(UUID.randomUUID())
                .errorMessage("Ошибка выполнения бизнес-логики: " + e.getMessage())
                .build();
    }
}
