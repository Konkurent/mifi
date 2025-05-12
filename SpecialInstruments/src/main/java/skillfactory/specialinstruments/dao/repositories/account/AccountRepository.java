package skillfactory.specialinstruments.dao.repositories.account;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import skillfactory.specialinstruments.constants.Role;
import skillfactory.specialinstruments.dao.entity.AccountEntity;
import skillfactory.specialinstruments.dto.accounts.AccountFilter;

import java.util.List;
import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<AccountEntity, Long>, JpaSpecificationExecutor<AccountEntity> {

    Optional<AccountEntity> findByLoginIgnoreCase(String login);
    boolean existsByRole(Role role);
    boolean existsByLoginIgnoreCase(String login);

    default Page<AccountEntity> getPageByFilter(AccountFilter filter) {
        PageRequest pageRequest = PageRequest.of(filter.page(), filter.size(), Sort.Direction.DESC, "createDateTime");
        return findAll(
                AccountSpecification.loginLike(filter.login())
                        .and(AccountSpecification.roleEqual(filter.role())),
                pageRequest
        );
    }

    default List<AccountEntity> getAllByFilter(AccountFilter filter) {
        return findAll(AccountSpecification.loginLike(filter.login()).and(AccountSpecification.roleEqual(filter.role())));
    }

}
