package skillfactory.specialinstruments.dao.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import skillfactory.specialinstruments.constants.OTPStatus;
import skillfactory.specialinstruments.dao.entity.AccountEntity;
import skillfactory.specialinstruments.dao.entity.OTPEntity;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OTPRepository extends JpaRepository<OTPEntity, Long> {

    List<OTPEntity> findAllByAccount(AccountEntity account);

    Optional<OTPEntity> findByAccountIdAndCode(Long accountId, Integer code);

    List<OTPEntity> findAllByStatusAndExpirationDateTimeBefore(OTPStatus status, LocalDateTime expirationDateTime);

}
