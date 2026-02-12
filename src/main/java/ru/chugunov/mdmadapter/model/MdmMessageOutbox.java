package ru.chugunov.mdmadapter.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.Objects;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class MdmMessageOutbox extends AuditableEntity {

    /**
     * Уникальный числовой идентификатор записи
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    /**
     * Уникальный идентификатор события из таблицы {@link MdmMessage}
     */
    private UUID mdmMessageId;
    /**
     * Статус доставки сообщения во внешний сервис
     */
    @Enumerated(EnumType.STRING)
    private MdmMessageOutboxStatus status;
    /**
     * Направление, куда должно быть доставлено сообщение
     */
    @Enumerated(EnumType.STRING)
    private MdmMessageOutboxTarget target;
    /**
     * Ответ внешнего сервиса после обработки сообщения
     * Должен быть представлен в виде JSON с двумя полями:
     * - "response" - строка, содержащая ответ от внешнего сервиса в сериализованном виде
     * - "errors" - список строк, ошибки, возникшие в процессе отправки запроса
     *              и обработки ответа от внешнего сервиса
     */
    @JdbcTypeCode(SqlTypes.JSON)
    private String responseData;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MdmMessageOutbox that = (MdmMessageOutbox) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
