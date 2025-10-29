package mifi.hotel.repository;

import mifi.hotel.entities.ProcessedRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProcessedRequestRepository extends JpaRepository<ProcessedRequest, Long> {
    Optional<ProcessedRequest> findByRequestIdAndRoomId(Long requestId, Long roomId);
    boolean existsByRequestIdAndOperationType(Long requestId, String operationType);
}

