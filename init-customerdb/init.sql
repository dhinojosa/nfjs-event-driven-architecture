-- init-db/init.sql

CREATE TABLE customers
(
    id SERIAL PRIMARY KEY, -- Auto-incrementing integer ID
    firstname  VARCHAR(255),
    lastname   VARCHAR(255),
    points     INT,
    updatedTimestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
);

INSERT INTO customers (firstname, lastname, points)
VALUES ('John', 'Doe', 150),
       ('Jane', 'Smith', 120),
       ('Alice', 'Johnson', 90),
       ('Chris', 'Williams', 180),
       ('Kate', 'Brown', 60),
       ('Luke', 'Jones', 200),
       ('Anna', 'Davis', 110),
       ('Mike', 'Wilson', 75),
       ('Emma', 'Garcia', 130),
       ('Ryan', 'Martinez', 140),
       ('Mia', 'Hernandez', 55),
       ('James', 'Lopez', 190),
       ('Sophie', 'Gonzalez', 35),
       ('Liam', 'Clark', 80),
       ('Olivia', 'Lee', 200),
       ('Noah', 'Walker', 65),
       ('Ava', 'Hall', 145),
       ('Ethan', 'Allen', 50),
       ('Isabella', 'Scott', 170),
       ('Mason', 'Young', 95);