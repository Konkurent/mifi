CREATE SEQUENCE otp_id_seq;

CREATE TABLE accounts
(
    id                       BIGSERIAL PRIMARY KEY,
    login                    VARCHAR(255) NOT NULL,
    password                 VARCHAR(255) NOT NULL,
    role                     VARCHAR(255) NOT NULL,
    phone                    VARCHAR(255),
    email                    VARCHAR(255),
    chat_id                  VARCHAR(255),
    active                   BOOLEAN DEFAULT true,
    deleted                  BOOLEAN DEFAULT false,
    create_date_time         TIMESTAMP WITHOUT TIME ZONE,
    update_date_time         TIMESTAMP WITHOUT TIME ZONE
);


