package mifi.booking.entites;

import jakarta.persistence.*;
import lombok.*;
import mifi.booking.constant.BookingStatus;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "bookings")
public class BookingEntity {

    @Id
    @SequenceGenerator(name = "booking_id_seq")
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    private Long roomId;

    private LocalDate startDate;

    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    private BookingStatus status;

    @OneToOne(fetch = FetchType.LAZY)
    private UserEntity user;

    private Long rqId;

    @Version
    private Integer version;

    @CreationTimestamp
    private LocalDateTime createDate;

    @UpdateTimestamp
    private LocalDateTime updateDate;

}
