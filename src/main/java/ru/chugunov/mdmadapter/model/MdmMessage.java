package ru.chugunov.mdmadapter.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.Objects;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class MdmMessage extends AuditableEntity {

    /**
     * Уникальный идентификатор записи в базе данных
     */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /**
     * Уникальный идентификатор сообщения, полученного из внешней системы
     */
    private UUID externalId;

    /**
     * Уникальный идентификатор клиента связанного с данным сообщением
     */
    private String guid;

    /**
     * Тип mdm события
     * @see MdmType
     */
    @Enumerated(EnumType.STRING)
    private MdmType type;

    /**
     * Содержание сообщения в формате jsonb
     */
    @JdbcTypeCode(SqlTypes.JSON)
    private String payload;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MdmMessage that = (MdmMessage) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
