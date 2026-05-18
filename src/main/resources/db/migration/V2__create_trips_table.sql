--DROP TABLE IF EXISTS trip_participants;
--DROP TABLE IF EXISTS invitations;
--DROP TABLE IF EXISTS trips;

CREATE TABLE trips (
    id UUID PRIMARY KEY,
    parent_trip_id UUID REFERENCES trips(id) ON DELETE CASCADE,
    name VARCHAR(255) NOT NULL,
    country VARCHAR(127) NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    budget DECIMAL(19, 2) NOT NULL,
    base_currency VARCHAR(3) NOT NULL,
    transport_mode VARCHAR(64),
    trip_type VARCHAR(64),
    status VARCHAR(32) DEFAULT 'PLANNED' NOT NULL
);

CREATE TABLE invitations (
    id UUID PRIMARY KEY,
    trip_id UUID NOT NULL REFERENCES trips(id) ON DELETE CASCADE,
    inviter_id UUID NOT NULL REFERENCES users(id),
    invitee_id UUID NOT NULL REFERENCES users(id),
    role VARCHAR(32) DEFAULT 'PARTICIPANT' NOT NULL,
    status VARCHAR(32) DEFAULT 'PENDING' NOT NULL,
    created_at TIMESTAMP DEFAULT now() NOT NULL
);

CREATE TABLE trip_participants (
    trip_id UUID NOT NULL,
    user_id UUID NOT NULL,
    role VARCHAR(32) DEFAULT 'PARTICIPANT' NOT NULL,
    joined_at TIMESTAMP DEFAULT now() NOT NULL,
    CONSTRAINT trip_participants_pk PRIMARY KEY (trip_id, user_id),
    CONSTRAINT trip_participants_trips_fk FOREIGN KEY (trip_id) REFERENCES trips(id) ON DELETE CASCADE,
    CONSTRAINT trip_participants_users_fk FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);