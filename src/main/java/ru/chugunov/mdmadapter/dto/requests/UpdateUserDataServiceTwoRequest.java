package ru.chugunov.mdmadapter.dto.requests;

import lombok.*;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateUserDataServiceTwoRequest {

    private UUID id;
    private String systemId;
    private List<UserDataServiceTwoEvent> events;

}
