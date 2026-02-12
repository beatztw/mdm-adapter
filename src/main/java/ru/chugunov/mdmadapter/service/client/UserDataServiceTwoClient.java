package ru.chugunov.mdmadapter.service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import ru.chugunov.mdmadapter.dto.requests.UpdateUserDataServiceTwoRequest;
import ru.chugunov.mdmadapter.dto.responses.ServiceResponse;

import java.util.concurrent.CompletableFuture;

@FeignClient(url = "${mdm.service.user-data-two.url}", name = "user-data-service-two-client")
public interface UserDataServiceTwoClient {

    @PostMapping(value = "/user-data-service-two/user/update/phone")
    CompletableFuture<ServiceResponse> updatePhone(UpdateUserDataServiceTwoRequest request);
}
