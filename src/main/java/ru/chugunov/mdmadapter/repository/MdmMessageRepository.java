package ru.chugunov.mdmadapter.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.chugunov.mdmadapter.model.MdmMessage;

import java.util.UUID;

public interface MdmMessageRepository extends JpaRepository<MdmMessage, UUID> {

    boolean existsByExternalId(UUID externalId);

}
