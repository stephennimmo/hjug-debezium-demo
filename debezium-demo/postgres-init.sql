CREATE TABLE legal_entity_type
(
    legal_entity_type_id SERIAL PRIMARY KEY,
    name                 TEXT NOT NULL
);
ALTER SEQUENCE legal_entity_type_legal_entity_type_id_seq RESTART WITH 100;

CREATE TABLE legal_entity
(
    legal_entity_id      SERIAL PRIMARY KEY,
    legal_entity_type_id INT  NOT NULL,
    name                 TEXT NOT NULL,
    FOREIGN KEY (legal_entity_type_id) REFERENCES legal_entity_type (legal_entity_type_id)
);
ALTER SEQUENCE legal_entity_legal_entity_id_seq RESTART WITH 100;

CREATE TABLE uom
(
    uom_id SERIAL PRIMARY KEY,
    abbr   TEXT NOT NULL,
    name   TEXT NOT NULL
);
ALTER SEQUENCE uom_uom_id_seq RESTART WITH 100;

CREATE TABLE currency
(
    currency_id SERIAL PRIMARY KEY,
    abbr        TEXT NOT NULL,
    name        TEXT NOT NULL
);
ALTER SEQUENCE currency_currency_id_seq RESTART WITH 100;

CREATE TABLE trade_type
(
    trade_type_id SERIAL PRIMARY KEY,
    name          TEXT NOT NULL
);
ALTER SEQUENCE trade_type_trade_type_id_seq RESTART WITH 100;

CREATE TABLE commodity
(
    commodity_id SERIAL PRIMARY KEY,
    name         TEXT NOT NULL
);
ALTER SEQUENCE commodity_commodity_id_seq RESTART WITH 100;

CREATE TABLE location
(
    location_id SERIAL PRIMARY KEY,
    name         TEXT NOT NULL
);
ALTER SEQUENCE location_location_id_seq RESTART WITH 100;

CREATE TABLE trade_header
(
    trade_id            SERIAL PRIMARY KEY,
    start_date          DATE      NOT NULL,
    end_date            DATE      NOT NULL,
    execution_timestamp TIMESTAMP NOT NULL,
    trade_type_id       INT       NOT NULL,
    FOREIGN KEY (trade_type_id) REFERENCES trade_type (trade_type_id)
);
ALTER SEQUENCE trade_header_trade_id_seq RESTART WITH 100;
ALTER TABLE trade_header REPLICA IDENTITY FULL;

CREATE TABLE trade_leg
(
    trade_leg_id      SERIAL PRIMARY KEY,
    trade_id          INT NOT NULL,
    payer_id          INT NOT NULL,
    receiver_id       INT NOT NULL,
    commodity_id      INT NOT NULL,
    location_id       INT NOT NULL,
    price             DECIMAL(10, 2),
    price_currency_id INT,
    quantity          DECIMAL(10, 2),
    quantity_uom_id   INT,
    FOREIGN KEY (trade_id) REFERENCES trade_header (trade_id),
    FOREIGN KEY (payer_id) REFERENCES legal_entity (legal_entity_id),
    FOREIGN KEY (receiver_id) REFERENCES legal_entity (legal_entity_id),
    FOREIGN KEY (commodity_id) REFERENCES commodity (commodity_id),
    FOREIGN KEY (location_id) REFERENCES location (location_id),
    FOREIGN KEY (price_currency_id) REFERENCES currency (currency_id),
    FOREIGN KEY (quantity_uom_id) REFERENCES uom (uom_id)
);
ALTER SEQUENCE trade_leg_trade_leg_id_seq RESTART WITH 100;
ALTER TABLE trade_leg REPLICA IDENTITY FULL;

-- Insert Temp Data

insert into currency (currency_id, abbr, name)
values (1, 'USD', 'United States Dollar');
insert into currency (currency_id, abbr, name)
values (2, 'CAD', 'Canadian Dollar');

insert into uom (uom_id, abbr, name)
VALUES (1, 'MMBTU', 'MMBTU');
insert into uom (uom_id, abbr, name)
VALUES (2, 'MWh', 'MWh');
insert into uom (uom_id, abbr, name)
VALUES (3, 'BBL', 'BBL');

insert into trade_type (trade_type_id, name)
VALUES (1, 'Physical');

insert into legal_entity_type (legal_entity_type_id, name)
VALUES (1, 'Counterparty');

insert into legal_entity (legal_entity_id, legal_entity_type_id, name)
VALUES (1, 1, 'Counterparty One');
insert into legal_entity (legal_entity_id, legal_entity_type_id, name)
VALUES (2, 1, 'Counterparty Two');

insert into commodity (commodity_id, name) VALUES (1, 'NatGas');
insert into commodity (commodity_id, name) VALUES (2, 'Power');

insert into location (location_id, name) VALUES (1, 'HenryHub');
insert into location (location_id, name) VALUES (2, 'LZ_HOUSTON');
insert into location (location_id, name) VALUES (3, 'LZ_NORTH');

insert into trade_header (start_date, end_date, execution_timestamp, trade_type_id)
VALUES (CURRENT_DATE - 30, CURRENT_DATE + 5, CURRENT_TIMESTAMP, 1);

BEGIN;
WITH new_trade AS (
    insert into trade_header (start_date, end_date, execution_timestamp, trade_type_id) VALUES (CURRENT_DATE - 30, CURRENT_DATE + 5, CURRENT_TIMESTAMP, 1)
        RETURNING trade_id
)

INSERT INTO trade_leg (trade_id, payer_id, receiver_id, commodity_id, location_id, price, price_currency_id, quantity, quantity_uom_id)
VALUES ((select trade_id from new_trade), 1, 2, 1, 1, 2.84, 1, 10000, 1),
       ((select trade_id from new_trade), 2, 1, 1, 1, 2.84, 1, 10000, 1);
COMMIT;