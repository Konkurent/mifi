package mifi.booking.repository;

import org.springframework.data.jpa.repository.Query;

@org.springframework.stereotype.Repository
public interface BookingSequence {

    @Query(value = "SELECT nextval('booking_rq_id')", nativeQuery = true)
    Long next();

}
