CREATE TABLE expenses (
    id uuid PRIMARY KEY,
    trip_id uuid NOT NULL,
    category varchar(64) NOT NULL,
    amount numeric(19,2) NOT NULL,
    currency varchar(3) NOT NULL,
    exchange_rate numeric(19,6) NOT NULL,
    rate_source varchar(16),
    expense_date date NOT NULL,
    description varchar(1000),
    payer_id uuid NOT NULL,
    created_at timestamp without time zone NOT NULL DEFAULT now(),

    CONSTRAINT expenses_trip_fk
        FOREIGN KEY (trip_id) REFERENCES trips(id) ON DELETE CASCADE,

    CONSTRAINT expenses_payer_fk
        FOREIGN KEY (payer_id) REFERENCES users(id),

    CONSTRAINT expenses_amount_positive
        CHECK (amount > 0),

    CONSTRAINT expenses_exchange_rate_positive
        CHECK (exchange_rate > 0)
);

CREATE TABLE expense_splits (
    expense_id uuid NOT NULL,
    user_id uuid NOT NULL,
    owed_amount numeric(19,2) NOT NULL,

    PRIMARY KEY (expense_id, user_id),

    CONSTRAINT expense_splits_expense_fk
        FOREIGN KEY (expense_id) REFERENCES expenses(id) ON DELETE CASCADE,

    CONSTRAINT expense_splits_user_fk
        FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);
