CREATE TABLE otp_configuration
(
    id                       BIGINT PRIMARY KEY,
    duration                 BIGINT,
    length                   BIGINT
);

INSERT INTO otp_configuration (id, duration, length) VALUES (nextval('otp_id_seq'), 0, 0);

CREATE TABLE otps
(
    id                       BIGSERIAL PRIMARY KEY,
    account_id               BIGINT REFERENCES accounts(id),
    code                     BIGINT,
    status                   VARCHAR(255),
    expiration_date_time     TIMESTAMP WITHOUT TIME ZONE,
    operation                VARCHAR(255),
    active                   BOOLEAN DEFAULT true,
    deleted                  BOOLEAN DEFAULT false,
    create_date_time         TIMESTAMP WITHOUT TIME ZONE,
    update_date_time         TIMESTAMP WITHOUT TIME ZONE
);
