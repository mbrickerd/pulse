CREATE INDEX idx_transaction_risk_projection_customer_id
    ON transaction_risk_projection (customer_id);

CREATE INDEX idx_transaction_risk_projection_risk_level
    ON transaction_risk_projection (risk_level);

CREATE INDEX idx_transaction_risk_projection_assessed_at
    ON transaction_risk_projection (assessed_at DESC);
