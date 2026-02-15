package ru.chugunov.mdmadapter.dto.responses;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDataServiceTwoResponseBody implements Serializable {

    private String id;
    private ServiceResponseStatus status;
    private String errorMessage;

}
