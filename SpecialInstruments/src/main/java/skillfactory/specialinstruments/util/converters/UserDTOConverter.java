package skillfactory.specialinstruments.util.converters;

import lombok.experimental.UtilityClass;
import skillfactory.specialinstruments.dao.entity.AccountEntity;
import skillfactory.specialinstruments.dto.accounts.User;

@UtilityClass
public class UserDTOConverter {

    public static User toDTO(AccountEntity accountEntity) {
        return User.builder()
                .login(accountEntity.getLogin())
                .role(accountEntity.getRole())
                .createDateTime(accountEntity.getCreateDateTime())
                .updateDateTime(accountEntity.getUpdateDateTime())
                .build();
    }

}
