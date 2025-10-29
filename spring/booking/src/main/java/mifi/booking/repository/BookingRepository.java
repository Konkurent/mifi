package mifi.booking.repository;

import mifi.booking.constant.BookingStatus;
import mifi.booking.dto.BookingFilter;
import mifi.booking.entites.BookingEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<BookingEntity, Long>, JpaSpecificationExecutor<BookingEntity> {

    default Page<BookingEntity> getPageByFilter(BookingFilter filter) {
        PageRequest pageRequest = PageRequest.of(filter.getPage(), filter.getSize(), Sort.Direction.DESC, "endDate", "startDate");
        return findAll(
                BookingSpecification.userIdEqual(filter.getUserId())
                        .and(BookingSpecification.roomIdEqual(filter.getRoomId()))
                        .and(BookingSpecification.statusEqual(filter.getStatus()))
                        .and(BookingSpecification.startDateLessThen(filter.getEndDate()))
                        .and(BookingSpecification.endDateGreaterThen(filter.getStartDate())),
                pageRequest
        );
    }

    default List<BookingEntity> getAllByFilter(BookingFilter filter) {
        return findAll(
                BookingSpecification.userIdEqual(filter.getUserId())
                        .and(BookingSpecification.roomIdEqual(filter.getRoomId()))
                        .and(BookingSpecification.statusEqual(filter.getStatus()))
                        .and(BookingSpecification.startDateLessThen(filter.getEndDate()))
                        .and(BookingSpecification.endDateGreaterThen(filter.getStartDate())),
                Sort.by(Sort.Direction.DESC, "endDate", "startDate")
        );
    }

    static class BookingSpecification {

        public static Specification<BookingEntity> userIdEqual(@Nullable Long userId) {
            return (root, query, criteriaBuilder) -> {
                if (userId == null) {
                    return criteriaBuilder.conjunction();
                } else {
                    return criteriaBuilder.equal(root.get("userId"), userId);
                }
            };
        }

        public static Specification<BookingEntity> roomIdEqual(@Nullable Long roomId) {
            return (root, query, criteriaBuilder) -> {
                if (roomId == null) {
                    return criteriaBuilder.conjunction();
                } else {
                    return criteriaBuilder.equal(root.get("roomId"), roomId);
                }
            };
        }

        public static Specification<BookingEntity> statusEqual(@Nullable BookingStatus status) {
            return (root, query, criteriaBuilder) -> {
                if (status == null) {
                    return criteriaBuilder.conjunction();
                } else {
                    return criteriaBuilder.equal(root.get("status"), status);
                }
            };
        }

        public static Specification<BookingEntity> startDateLessThen(@Nullable LocalDate endDate) {
            return (root, query, criteriaBuilder) -> {
                if (endDate == null) {
                    return criteriaBuilder.conjunction();
                } else {
                    return criteriaBuilder.lessThanOrEqualTo(root.get("startDate"), endDate);
                }
            };
        }

        public static Specification<BookingEntity> endDateGreaterThen(@Nullable LocalDate startDate) {
            return (root, query, criteriaBuilder) -> {
                if (startDate == null) {
                    return criteriaBuilder.conjunction();
                } else {
                    return criteriaBuilder.lessThanOrEqualTo(root.get("endDate"), startDate);
                }
            };
        }

    }

}
