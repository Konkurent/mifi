package skillfactory.specialinstruments.dao.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import skillfactory.specialinstruments.dao.entity.OTPConfigurationEntity;

@Repository
public interface OTPConfigurationRepository extends JpaRepository<OTPConfigurationEntity, Long> {

}
