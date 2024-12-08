-- init-db/init.sql

CREATE TABLE inventory
(
    productId varchar(255) PRIMARY KEY,
    name varchar(255) NOT NULL,
    quantity  integer NOT NULL,
    updatedTimestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
);


INSERT INTO inventory (productId, name, quantity)
VALUES ('c001', 'rocking chair', 12),
       ('t002', 'yo-yo', 25),
       ('t003', 'race car', 8),
       ('m004', 'bicycle', 15),
       ('t005', 'cowboy boots', 30),
       ('m006', 'hard hat', 22),
       ('t007', 'skateboard', 9),
       ('c008', 'disco ball', 16),
       ('c009', 'kite', 11),
       ('m010', 'unicorn', 14);

