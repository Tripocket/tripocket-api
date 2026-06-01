-- ============================================================
-- EXPENSES
-- ============================================================
CREATE TABLE public.expenses (
    id            UUID           NOT NULL,
    trip_id       UUID           NOT NULL,
    payer_id      UUID           NOT NULL,
    category      VARCHAR(50)    NULL,
    amount        NUMERIC(19, 2) NOT NULL,
    currency      VARCHAR(3)     NOT NULL,
    exchange_rate NUMERIC(15, 6) NULL,
    rate_source   VARCHAR(20)    NULL,
    date          DATE           NULL,
    description   VARCHAR(255)   NULL,
    created_at    TIMESTAMP      NOT NULL DEFAULT now(),
    CONSTRAINT expenses_pk        PRIMARY KEY (id),
    CONSTRAINT expenses_trip_fk   FOREIGN KEY (trip_id)  REFERENCES public.trips(id)  ON DELETE CASCADE,
    CONSTRAINT expenses_payer_fk  FOREIGN KEY (payer_id) REFERENCES public.users(id)
);

-- ============================================================
-- EXPENSE_SPLITS
-- ============================================================
CREATE TABLE public.expense_splits (
    expense_id  UUID           NOT NULL,
    user_id     UUID           NOT NULL,
    owed_amount NUMERIC(19, 2) NOT NULL,
    CONSTRAINT expense_splits_pk         PRIMARY KEY (expense_id, user_id),
    CONSTRAINT expense_splits_expense_fk FOREIGN KEY (expense_id) REFERENCES public.expenses(id) ON DELETE CASCADE,
    CONSTRAINT expense_splits_user_fk    FOREIGN KEY (user_id)    REFERENCES public.users(id)
);

CREATE INDEX idx_expenses_trip_id        ON public.expenses(trip_id);
CREATE INDEX idx_expenses_payer_id       ON public.expenses(payer_id);
CREATE INDEX idx_expense_splits_user_id  ON public.expense_splits(user_id);
