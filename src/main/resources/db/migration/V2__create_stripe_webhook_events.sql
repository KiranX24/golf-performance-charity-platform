CREATE TABLE IF NOT EXISTS stripe_webhook_events (
    id BIGSERIAL PRIMARY KEY,

    stripe_event_id VARCHAR(255) NOT NULL,

    event_type VARCHAR(255) NOT NULL,

    processed_at TIMESTAMPTZ NOT NULL,

    CONSTRAINT uq_stripe_webhook_event_id
        UNIQUE (stripe_event_id)
);