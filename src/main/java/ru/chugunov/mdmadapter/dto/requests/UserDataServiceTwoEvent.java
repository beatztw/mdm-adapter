package ru.chugunov.mdmadapter.dto.requests;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDataServiceTwoEvent {

    private String eventType;
    private String guid;
    private String phone;
}
