package ru.chugunov.mdmadapter.dto.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.chugunov.mdmadapter.dto.responses.ServiceResponseStatus;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommonServiceResponseBody implements Serializable {

    private String id;
    private ServiceResponseStatus status;
    private String errorMessage;
}
