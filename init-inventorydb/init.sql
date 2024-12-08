-- init-db/init.sql

CREATE TABLE inventory
(
    id SERIAL PRIMARY KEY, -- Auto-incrementing integer ID
    name VARCHAR(255) NOT NULL,
    quantity INTEGER NOT NULL,
    updatedTimestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
);

INSERT INTO inventory (name, quantity)
VALUES ('rocking chair', 12),
       ('yo-yo', 25),
       ('race car', 8),
       ('bicycle', 15),
       ('cowboy boots', 30),
       ('hard hat', 22),
       ('skateboard', 9),
       ('disco ball', 16),
       ('kite', 11),
       ('unicorn', 14);