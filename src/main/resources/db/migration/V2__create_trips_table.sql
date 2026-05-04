--  tabela wycieczek
--  atrybuty: kraj, czas start/koniec, waluta bazowa i alternatywna, budżet w bazowej
CREATE TABLE trips (
    id uuid NOT NULL,
    country varchar(127) NOT NULL,          --można by zamiast tego dać kod kraju i słownik kod->nazwa
    start_date date NOT NULL,
    end_date date NOT NULL,
    budget decimal(19, 2) NOT NULL,         --w walucie bazowej, potem przeliczanie przy liczeniu wydatków
    currency_primary varchar(3) NOT NULL,    --np. PLN, musi być
    currency_secondary varchar(3) NULL,      --np. EUR/USD, nie musi być
    created_at timestamp DEFAULT now() NOT NULL,
    CONSTRAINT trips_pk PRIMARY KEY (id)
);

--  tabela incydencji user -> trip_participants <- trip
--  atrybuty związku user < - > trip:
    --  rola
    --  czas dołączenia do wycieczki
CREATE TABLE trip_participants (
    trip_id uuid NOT NULL,
    user_id uuid NOT NULL,
    role varchar(32) NOT NULL, --OWNER/PARTICIPANT itp.
    joined_at timestamp DEFAULT now() NOT NULL,
    CONSTRAINT trip_participants_pk PRIMARY KEY (trip_id, user_id),
    CONSTRAINT trip_participants_trips_fk FOREIGN KEY (trip_id) REFERENCES trips(id) ON DELETE CASCADE,
    CONSTRAINT trip_participants_users_fk FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);