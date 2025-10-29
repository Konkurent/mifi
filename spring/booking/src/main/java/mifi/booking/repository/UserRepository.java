package mifi.booking.repository;

import mifi.booking.entites.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long> {

    boolean existsByEmailIgnoreCase(String email);
    boolean existsByLoginIgnoreCase(String login);

    UserEntity getByLoginIgnoreCase(String login);

}
