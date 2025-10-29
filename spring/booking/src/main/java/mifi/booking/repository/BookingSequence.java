package mifi.booking.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;

@org.springframework.stereotype.Repository
public interface BookingSequence extends Repository<Long, Long> {

    @Query(value = "SELECT nextval('booking_rq_id')", nativeQuery = true)
    Long next();

}
