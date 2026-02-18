package ru.chugunov.mdmadapter.service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import ru.chugunov.mdmadapter.dto.requests.UpdateUserDataServiceOneRequest;
import ru.chugunov.mdmadapter.dto.responses.UserDataServiceOneResponse;

@FeignClient(url = "${mdm.service.user-data-one.url}", name = "user-data-service-one-client")
public interface UserDataServiceOneClient {

    @PostMapping(value = "/user-data-service-one/update-phone")
    UserDataServiceOneResponse updatePhone(UpdateUserDataServiceOneRequest request);

}
