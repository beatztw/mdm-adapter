package ru.chugunov.mdmadapter.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.chugunov.mdmadapter.validation.ValidEventType;
import ru.chugunov.mdmadapter.validation.ValidGuid;
import ru.chugunov.mdmadapter.validation.ValidPhone;
import ru.chugunov.mdmadapter.validation.ValidUUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdatePhoneMdmEvent {

    @NotBlank(message = "Поле id не может быть пустым")
    @ValidUUID
    private String id;

    @NotBlank(message = "Поле 'type' не может быть пустым")
    @ValidEventType
    private String type;

    @NotBlank(message = "Поле 'guid' не может быть пустым")
    @Size(min = 32, max = 32, message = "Поле guid должно быть длиной ровно 32 символа")
    @ValidGuid
    private String guid;

    @NotBlank(message = "Поле 'phone' не может быть пустым")
    @ValidPhone
    private String phone;
}
