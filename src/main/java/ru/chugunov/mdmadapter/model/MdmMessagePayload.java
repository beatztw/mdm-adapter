package ru.chugunov.mdmadapter.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MdmMessagePayload {

    /**
     * Телефон для передачи в payload mdm сообщения
     */
    private String phone;
}
