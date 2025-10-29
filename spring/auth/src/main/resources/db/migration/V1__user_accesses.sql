CREATE TABLE user_access(
    email                       VARCHAR(255) PRIMARY KEY,
    login                       VARCHAR(255) UNIQUE ,
    password                    VARCHAR(255) NOT NULL,
    user_id                     BIGINT NOT NULL,
    role                        VARCHAR(255) NOT NULL DEFAULT 'USER',
    create_date                 TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_date                 TIMESTAMP WITH TIME ZONE
)