package ru.chugunov.mdmadapter.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.chugunov.mdmadapter.model.MdmMessageOutbox;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface MdmMessageOutboxRepository extends JpaRepository<MdmMessageOutbox, UUID> {

    @Query(value = """
        FROM MdmMessageOutbox msg
        WHERE msg.lastUpdateTime < :updateTimeTo
            AND msg.lastUpdateTime > :updateTimeFrom
            AND msg.status IN (:statuses)
            AND msg.id > :lastId
    """)
    List<MdmMessageOutbox> findMdmMessageForRetry(LocalDateTime updateTimeTo,
                                                  LocalDateTime updateTimeFrom,
                                                  List<String> statuses,
                                                  Long lastId,
                                                  Pageable pageable);

}
