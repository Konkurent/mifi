package mifi.booking.entites;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "users")
public class UserEntity {

    @Id
    @SequenceGenerator(name = "booking_id_seq")
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @NotBlank
    private String lastName;

    @NotBlank
    private String firstName;

    private String middleName;

    private String login;

    @Email
    private String email;

    @CreationTimestamp
    private LocalDateTime creatDate;

    @UpdateTimestamp
    private LocalDateTime updateDate;

    @PrePersist
    public void prePersist() {
        lastName = lastName.trim();
        firstName = firstName.trim();
        middleName = middleName != null && !middleName.isBlank()? middleName.trim() : null;
        login = login.trim();
        email = email.trim();
    }

    @PreUpdate
    private  void preUpdate() {
        lastName = lastName.trim();
        firstName = firstName.trim();
        middleName = middleName != null && !middleName.isBlank()? middleName.trim() : null;
        login = login.trim();
        email = email.trim();
    }

}
