package skillfactory.specialinstruments.dao.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import skillfactory.specialinstruments.constants.OTPStatus;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;

@Getter
@Setter
@Entity
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "otps")
public class OTPEntity extends GeneralEntity {

    @OneToOne
    private AccountEntity account;

    private Integer code;

    private String operation;

    private LocalDateTime expirationDateTime;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    private OTPStatus status = OTPStatus.ACTIVE;

    @PrePersist
    public void trimOperation() {
        this.operation = operation.trim();
    }

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expirationDateTime);
    }
}
