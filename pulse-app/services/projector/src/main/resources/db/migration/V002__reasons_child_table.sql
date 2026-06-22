CREATE TABLE transaction_reason (
    projection_id BIGINT NOT NULL REFERENCES transaction_risk_projection(id) ON DELETE CASCADE,
    reason        TEXT   NOT NULL,
    position      INT    NOT NULL
);

CREATE INDEX idx_transaction_reason_projection_id ON transaction_reason(projection_id);

-- Migrate existing pipe-delimited data
INSERT INTO transaction_reason (projection_id, reason, position)
SELECT trp.id, t.reason, (t.pos - 1)::INT
FROM transaction_risk_projection trp,
     LATERAL unnest(string_to_array(trp.reasons, '|')) WITH ORDINALITY AS t(reason, pos)
WHERE trp.reasons <> '';

ALTER TABLE transaction_risk_projection DROP COLUMN reasons;
