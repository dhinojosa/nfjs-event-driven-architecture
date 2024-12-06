-- init-db/init.sql


CREATE TABLE customers
(
    customerid VARCHAR(255),
    firstname  VARCHAR(255),
    lastname   VARCHAR(255),
    points     INT
);

INSERT INTO customers (customerid, firstname, lastname, points)
VALUES ('id-1', 'John', 'Doe', 150),
       ('id-2', 'Jane', 'Smith', 120),
       ('id-3', 'Alice', 'Johnson', 90),
       ('id-4', 'Chris', 'Williams', 180),
       ('id-5', 'Kate', 'Brown', 60),
       ('id-6', 'Luke', 'Jones', 200),
       ('id-7', 'Anna', 'Davis', 110),
       ('id-8', 'Mike', 'Wilson', 75),
       ('id-9', 'Emma', 'Garcia', 130),
       ('id-10', 'Ryan', 'Martinez', 140),
       ('id-11', 'Mia', 'Hernandez', 55),
       ('id-12', 'James', 'Lopez', 190),
       ('id-13', 'Sophie', 'Gonzalez', 35),
       ('id-14', 'Liam', 'Clark', 80),
       ('id-15', 'Olivia', 'Lee', 200),
       ('id-16', 'Noah', 'Walker', 65),
       ('id-17', 'Ava', 'Hall', 145),
       ('id-18', 'Ethan', 'Allen', 50),
       ('id-19', 'Isabella', 'Scott', 170),
       ('id-20', 'Mason', 'Young', 95);
