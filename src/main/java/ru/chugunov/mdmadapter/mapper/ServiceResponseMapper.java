package ru.chugunov.mdmadapter.mapper;

import org.mapstruct.Mapper;
import ru.chugunov.mdmadapter.dto.common.CommonServiceResponseBody;
import ru.chugunov.mdmadapter.dto.responses.UserDataServiceOneResponseBody;
import ru.chugunov.mdmadapter.dto.responses.UserDataServiceTwoResponseBody;

@Mapper(componentModel = "spring")
public interface ServiceResponseMapper {

    CommonServiceResponseBody toCommonResponseBody(UserDataServiceOneResponseBody responseBody);
    CommonServiceResponseBody toCommonResponseBody(UserDataServiceTwoResponseBody responseBody);
}
