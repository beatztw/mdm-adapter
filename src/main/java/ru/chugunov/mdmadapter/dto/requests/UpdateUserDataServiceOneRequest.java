package ru.chugunov.mdmadapter.dto.requests;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateUserDataServiceOneRequest {

    private UserDataServiceOneMeta meta;
    private UserDataServiceOneBody body;
}
