package mifi.auth.dao.repositories;

import brave.internal.Nullable;
import jakarta.validation.constraints.Null;
import mifi.auth.dao.entities.AccessEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AccessRepository extends JpaRepository<AccessEntity, String> {

    @Query("FROM AccessEntity WHERE LOWER(email) = TRIM(LOWER(:userName)) OR LOWER(login) = TRIM(LOWER(:userName))")
    Optional<AccessEntity> findByUserName(String userName);

    @Nullable
    AccessEntity getByUserId(Long userId);


}
