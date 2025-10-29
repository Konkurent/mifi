CREATE SEQUENCE booking_rq_id START WITH 1 INCREMENT BY 10;
CREATE SEQUENCE booking_id_seq START WITH 1 INCREMENT BY 1;

CREATE TABLE users (
    id              BIGINT PRIMARY KEY AUTO_INCREMENT,
    first_name      VARCHAR(255) NOT NULL,
    last_name       VARCHAR(255) NOT NULL,
    middle_name     VARCHAR(255) DEFAULT NULL,
    login           VARCHAR(255) UNIQUE,
    email           VARCHAR(255) NOT NULL UNIQUE,
    password        VARCHAR(255) NOT NULL,
    role            VARCHAR(255) NOT NULL DEFAULT 'USER',
    create_date     TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_date     TIMESTAMP WITHOUT TIME ZONE
);

CREATE TABLE bookings(
    id              BIGINT PRIMARY KEY AUTO_INCREMENT,
    room_Id         BIGINT NOT NULL ,
    start_date      DATE NOT NULL,
    end_Date        DATE NOT NULL,
    status          VARCHAR(255) DEFAULT 'PENDING',
    user_id         BIGINT REFERENCES users(id),
    rq_id           BIGINT,
    create_date     TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_date     TIMESTAMP WITHOUT TIME ZONE
)



