package skillfactory.specialinstruments.dao.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import skillfactory.specialinstruments.constants.Role;

@Getter
@Setter
@Entity
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "accounts")
public class AccountEntity extends GeneralEntity {

    private String login;

    private String password;

    private String email;

    private String phone;

    private String chatId;

    @Enumerated(EnumType.STRING)
    private Role role;

    @PrePersist
    public void formatPhone() {
        if (phone != null && !phone.isEmpty()) {
            this.phone = formatPhone(this.phone);
        }
    }

    public void setPhone(String phone) {
        this.phone = formatPhone(phone);
    }

    public static String formatPhone(String phone) {
        String digits = phone.replaceAll("[^0-9]", "");
        if (digits.matches("^[78]\\d{10}$")) {
            return digits.replaceFirst(
                    "(\\d)(\\d{3})(\\d{3})(\\d{2})(\\d{2})",
                    "+$1($2)$3-$4-$5"
            );
        } else if (digits.matches("^\\d{10}$")) {
            return digits.replaceFirst(
                    "(\\d{3})(\\d{3})(\\d{2})(\\d{2})",
                    "+7($1)$2-$3-$4"
            );
        } else {
            throw new IllegalArgumentException("Invalid phone number");
        }
    }

}
