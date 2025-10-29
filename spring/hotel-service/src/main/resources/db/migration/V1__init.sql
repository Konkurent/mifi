CREATE TABLE hotels (
    id              BIGINT PRIMARY KEY AUTO_INCREMENT,
    name            VARCHAR(255) NOT NULL,
    address         VARCHAR(255) NOT NULL
);

CREATE TABLE rooms (
    id              BIGINT PRIMARY KEY AUTO_INCREMENT,
    hotel_id        BIGINT NOT NULL,
    number          VARCHAR(255) NOT NULL,
    available       BOOLEAN NOT NULL DEFAULT true,
    times_booked    INTEGER NOT NULL DEFAULT 0,
    version         INTEGER NOT NULL DEFAULT 0,
    FOREIGN KEY (hotel_id) REFERENCES hotels(id)
);

CREATE TABLE processed_requests (
    request_id      BIGINT PRIMARY KEY,
    room_id         BIGINT,
    processed_at    TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    operation_type  VARCHAR(50) NOT NULL
);

CREATE INDEX idx_processed_requests_room ON processed_requests(room_id, request_id);

