package mifi.hotel.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "processed_requests")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProcessedRequest {

    @Id
    @Column(name = "request_id")
    private Long requestId;

    @Column(name = "room_id")
    private Long roomId;

    @Column(name = "processed_at", nullable = false)
    private LocalDateTime processedAt;

    @Column(name = "operation_type", nullable = false)
    private String operationType;
}

