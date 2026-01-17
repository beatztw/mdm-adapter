package ru.chugunov.mdmadapter.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.chugunov.mdmadapter.model.enums.MDMMessageOutboxStatus;
import ru.chugunov.mdmadapter.model.enums.MDMMessageOutboxTarget;

import java.util.Objects;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class MDMMessageOutbox extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private UUID mdmMessageId;

    private MDMMessageOutboxStatus status;

    private MDMMessageOutboxTarget target;

    @Column(columnDefinition = "jsonb")
    private String responseData;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MDMMessageOutbox that = (MDMMessageOutbox) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
