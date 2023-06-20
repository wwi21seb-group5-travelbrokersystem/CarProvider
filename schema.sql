CREATE TABLE cars
(
    car_id       uuid NOT NULL,
    model        varchar(50),
    manufacturer varchar(50),
    capacity     integer,
    price_per_day  numeric(10, 2)
);

ALTER TABLE cars
    ADD CONSTRAINT car_id
        PRIMARY KEY (car_id);

CREATE TABLE rentals
(
    rental_id  uuid NOT NULL,
    car_id     uuid NOT NULL,
    start_date date,
    end_date   date,
    total_price numeric(10, 2)
);

ALTER TABLE rentals
    ADD CONSTRAINT rental_id
        PRIMARY KEY (rental_id);

ALTER TABLE rentals
    ADD CONSTRAINT fk_rentals_cars
        FOREIGN KEY (car_id) REFERENCES cars (car_id);
