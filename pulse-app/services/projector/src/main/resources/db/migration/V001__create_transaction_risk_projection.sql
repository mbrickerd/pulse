CREATE TABLE transaction_risk_projection (
    id              BIGSERIAL       PRIMARY KEY,
    transaction_id  VARCHAR(255)    NOT NULL UNIQUE,
    customer_id     VARCHAR(255)    NOT NULL,
    amount          NUMERIC(19, 2)  NOT NULL,
    currency        VARCHAR(10)     NOT NULL,
    risk_level      VARCHAR(20)     NOT NULL,
    risk_score      INTEGER         NOT NULL,
    review_required BOOLEAN         NOT NULL,
    reasons         TEXT            NOT NULL,
    assessed_at     TIMESTAMPTZ     NOT NULL
);
