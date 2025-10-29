package mifi.hotel.repository;

import mifi.hotel.entities.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RoomRepository extends JpaRepository<Room, Long> {

    List<Room> findByAvailableTrue();

    List<Room> findByHotelId(Long hotelId);

    @Query("SELECT r FROM Room r WHERE r.available = true ORDER BY r.timesBooked ASC")
    List<Room> findAvailableRoomsOrderedByTimesBooked();

    @Query("SELECT r FROM Room r WHERE r.available = true")
    List<Room> findAllAvailableRooms();
}

