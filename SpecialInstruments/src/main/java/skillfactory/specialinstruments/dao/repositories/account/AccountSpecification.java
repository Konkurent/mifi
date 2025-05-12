package skillfactory.specialinstruments.dao.repositories.account;

import lombok.experimental.UtilityClass;
import org.springframework.data.jpa.domain.Specification;
import skillfactory.specialinstruments.constants.Role;
import skillfactory.specialinstruments.dao.entity.AccountEntity;

@UtilityClass
public class AccountSpecification {

    public static Specification<AccountEntity> loginLike(String login) {
        return (root, query, criteriaBuilder) -> {
            if (login == null) return criteriaBuilder.conjunction();
            if (login.trim().isBlank()) return criteriaBuilder.conjunction();
            return criteriaBuilder.lower(root.get("login")).in("%" + login.toLowerCase() + "%");
        };
    }

    public static Specification<AccountEntity> roleEqual(Role role) {
        return (root, query, criteriaBuilder) -> {
            if (role == null) return criteriaBuilder.conjunction();
            return criteriaBuilder.equal(root.get("role"), role);
        };
    }

}
