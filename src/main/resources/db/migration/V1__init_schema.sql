-- =====================================================================
-- DIGITAL HEROES — V1 INITIAL SCHEMA (Flyway migration)
-- PostgreSQL / Supabase
--
-- Reviewed for JPA/Hibernate compatibility vs. the Phase 2 draft:
--   * Native CREATE TYPE ... AS ENUM replaced with VARCHAR + CHECK
--     constraints, so entities can use plain @Enumerated(EnumType.STRING)
--     without custom Hibernate UserType wiring. This is a persistence
--     mapping decision only — it does not change any business rule
--     documented in ASSUMPTIONS.md.
--   * All monetary columns use NUMERIC — mapped to java.math.BigDecimal.
--   * FK ordering verified: every REFERENCES target is created earlier
--     in this file.
-- =====================================================================

-- ---------------------------------------------------------------------
-- USERS
-- ---------------------------------------------------------------------
CREATE TABLE users (
    id                  BIGSERIAL PRIMARY KEY,
    email               VARCHAR(255) NOT NULL UNIQUE,
    password_hash       VARCHAR(255) NOT NULL,
    full_name           VARCHAR(255) NOT NULL,
    role                VARCHAR(20) NOT NULL DEFAULT 'ROLE_USER'
                            CHECK (role IN ('ROLE_USER', 'ROLE_ADMIN')),
    is_active           BOOLEAN NOT NULL DEFAULT TRUE,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- ---------------------------------------------------------------------
-- PLATFORM SETTINGS (configurable business rules — ASSUMPTIONS.md A2/A3)
-- ---------------------------------------------------------------------
CREATE TABLE platform_settings (
    key                 VARCHAR(100) PRIMARY KEY,
    value               VARCHAR(255) NOT NULL,
    description         TEXT,
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT now()
);

INSERT INTO platform_settings (key, value, description) VALUES
  ('prize_pool_allocation_pct', '20.00', 'Percent of each subscription payment allocated to the prize pool (A2)'),
  ('charity_min_contribution_pct', '10.00', 'Minimum charity contribution percent of subscription fee'),
  ('charity_max_contribution_pct', '100.00', 'Maximum charity contribution percent of subscription fee (A3)');

-- ---------------------------------------------------------------------
-- SUBSCRIPTION PLANS & SUBSCRIPTIONS
-- ---------------------------------------------------------------------
CREATE TABLE subscription_plans (
    id                  BIGSERIAL PRIMARY KEY,
    name                VARCHAR(100) NOT NULL,
    plan_interval       VARCHAR(10) NOT NULL CHECK (plan_interval IN ('MONTHLY', 'YEARLY')),
    price               NUMERIC(10,2) NOT NULL CHECK (price >= 0),
    currency            VARCHAR(3) NOT NULL DEFAULT 'INR',
    is_active           BOOLEAN NOT NULL DEFAULT TRUE,
    stripe_price_id     VARCHAR(255),
    created_at          TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE subscriptions (
    id                          BIGSERIAL PRIMARY KEY,
    user_id                     BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    plan_id                     BIGINT NOT NULL REFERENCES subscription_plans(id),
    status                      VARCHAR(20) NOT NULL DEFAULT 'ACTIVE'
                                    CHECK (status IN ('ACTIVE', 'CANCELLED', 'LAPSED', 'PAST_DUE')),
    start_date                  TIMESTAMPTZ NOT NULL,
    renewal_date                TIMESTAMPTZ,
    cancellation_date           TIMESTAMPTZ,
    stripe_subscription_id      VARCHAR(255) UNIQUE,
    stripe_customer_id          VARCHAR(255),
    created_at                  TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at                  TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- Only one ACTIVE subscription per user at a time (partial unique index)
CREATE UNIQUE INDEX uq_one_active_subscription_per_user
    ON subscriptions (user_id)
    WHERE status = 'ACTIVE';

CREATE INDEX idx_subscriptions_user ON subscriptions(user_id);

-- ---------------------------------------------------------------------
-- PAYMENTS (Stripe transaction records — traceable)
-- ---------------------------------------------------------------------
CREATE TABLE payments (
    id                      BIGSERIAL PRIMARY KEY,
    subscription_id         BIGINT REFERENCES subscriptions(id),
    user_id                 BIGINT NOT NULL REFERENCES users(id),
    amount                  NUMERIC(10,2) NOT NULL CHECK (amount >= 0),
    currency                VARCHAR(3) NOT NULL DEFAULT 'INR',
    status                  VARCHAR(20) NOT NULL
                                CHECK (status IN ('SUCCEEDED', 'FAILED', 'PENDING', 'REFUNDED')),
    provider                VARCHAR(50) NOT NULL DEFAULT 'STRIPE',
    provider_transaction_id VARCHAR(255) UNIQUE,
    created_at              TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_payments_user ON payments(user_id);

-- ---------------------------------------------------------------------
-- SCORES (rolling latest-5 logic enforced in service layer + DB constraint)
-- ---------------------------------------------------------------------
CREATE TABLE scores (
    id                  BIGSERIAL PRIMARY KEY,
    user_id             BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    score_value         SMALLINT NOT NULL CHECK (score_value BETWEEN 1 AND 45),
    score_date          DATE NOT NULL,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uq_user_score_date UNIQUE (user_id, score_date)
);

CREATE INDEX idx_scores_user_date ON scores(user_id, score_date DESC);

-- ---------------------------------------------------------------------
-- CHARITIES
-- ---------------------------------------------------------------------
CREATE TABLE charities (
    id                  BIGSERIAL PRIMARY KEY,
    name                VARCHAR(255) NOT NULL,
    slug                VARCHAR(255) NOT NULL UNIQUE,
    description         TEXT,
    logo_url            VARCHAR(500),
    is_featured         BOOLEAN NOT NULL DEFAULT FALSE,
    is_archived         BOOLEAN NOT NULL DEFAULT FALSE,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE charity_events (
    id                  BIGSERIAL PRIMARY KEY,
    charity_id          BIGINT NOT NULL REFERENCES charities(id) ON DELETE CASCADE,
    title               VARCHAR(255) NOT NULL,
    description         TEXT,
    event_date          DATE,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- Historical, effective-dated charity selection (ASSUMPTIONS.md A7)
CREATE TABLE charity_selections (
    id                  BIGSERIAL PRIMARY KEY,
    user_id             BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    charity_id          BIGINT NOT NULL REFERENCES charities(id),
    contribution_pct    NUMERIC(5,2) NOT NULL CHECK (contribution_pct BETWEEN 10 AND 100),
    effective_from      TIMESTAMPTZ NOT NULL DEFAULT now(),
    effective_to        TIMESTAMPTZ,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- Only one currently-effective selection per user
CREATE UNIQUE INDEX uq_one_current_charity_selection
    ON charity_selections(user_id)
    WHERE effective_to IS NULL;

-- ---------------------------------------------------------------------
-- DONATIONS (independent of gameplay)
-- ---------------------------------------------------------------------
CREATE TABLE donations (
    id                      BIGSERIAL PRIMARY KEY,
    user_id                 BIGINT NOT NULL REFERENCES users(id),
    charity_id              BIGINT NOT NULL REFERENCES charities(id),
    amount                  NUMERIC(10,2) NOT NULL CHECK (amount > 0),
    currency                VARCHAR(3) NOT NULL DEFAULT 'INR',
    status                  VARCHAR(20) NOT NULL DEFAULT 'PENDING'
                                CHECK (status IN ('PENDING', 'SUCCEEDED', 'FAILED')),
    provider_transaction_id VARCHAR(255) UNIQUE,
    created_at              TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_donations_user ON donations(user_id);
CREATE INDEX idx_donations_charity ON donations(charity_id);

-- ---------------------------------------------------------------------
-- DRAWS
-- ---------------------------------------------------------------------
CREATE TABLE draws (
    id                  BIGSERIAL PRIMARY KEY,
    draw_period         DATE NOT NULL,
    mode                VARCHAR(20) NOT NULL CHECK (mode IN ('RANDOM', 'ALGORITHMIC')),
    status              VARCHAR(20) NOT NULL DEFAULT 'DRAFT'
                            CHECK (status IN ('DRAFT', 'SIMULATED', 'PUBLISHED', 'COMPLETED', 'CANCELLED')),
    drawn_numbers       SMALLINT[],
    simulated_at        TIMESTAMPTZ,
    published_at        TIMESTAMPTZ,
    completed_at        TIMESTAMPTZ,
    cancelled_at        TIMESTAMPTZ,
    created_by          BIGINT NOT NULL REFERENCES users(id),
    created_at          TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uq_draw_period UNIQUE (draw_period)
);

-- ---------------------------------------------------------------------
-- DRAW PARTICIPANTS (immutable snapshot — ASSUMPTIONS.md A11, A12)
-- ---------------------------------------------------------------------
CREATE TABLE draw_participants (
    id                  BIGSERIAL PRIMARY KEY,
    draw_id             BIGINT NOT NULL REFERENCES draws(id) ON DELETE CASCADE,
    user_id             BIGINT NOT NULL REFERENCES users(id),
    snapshot_numbers    SMALLINT[] NOT NULL,
    match_count         SMALLINT,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uq_draw_user UNIQUE (draw_id, user_id)
);

CREATE INDEX idx_draw_participants_draw ON draw_participants(draw_id);

-- ---------------------------------------------------------------------
-- PRIZE POOLS (per draw, per tier)
-- ---------------------------------------------------------------------
CREATE TABLE prize_pools (
    id                  BIGSERIAL PRIMARY KEY,
    draw_id             BIGINT NOT NULL REFERENCES draws(id) ON DELETE CASCADE,
    tier                VARCHAR(10) NOT NULL CHECK (tier IN ('THREE', 'FOUR', 'FIVE')),
    allocated_amount    NUMERIC(12,2) NOT NULL CHECK (allocated_amount >= 0),
    rollover_in         NUMERIC(12,2) NOT NULL DEFAULT 0,
    total_pool          NUMERIC(12,2) NOT NULL,
    winner_count        INT NOT NULL DEFAULT 0,
    amount_per_winner   NUMERIC(12,2),
    rounding_residual   NUMERIC(12,2) NOT NULL DEFAULT 0,
    rollover_out        NUMERIC(12,2) NOT NULL DEFAULT 0,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uq_draw_tier UNIQUE (draw_id, tier)
);

-- ---------------------------------------------------------------------
-- JACKPOT LEDGER (persistent rollover accumulator — ASSUMPTIONS.md A16)
-- ---------------------------------------------------------------------
CREATE TABLE jackpot_ledger (
    id                  BIGSERIAL PRIMARY KEY,
    balance             NUMERIC(12,2) NOT NULL DEFAULT 0 CHECK (balance >= 0),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT now(),
    last_draw_id        BIGINT REFERENCES draws(id)
);

INSERT INTO jackpot_ledger (balance) VALUES (0);

-- ---------------------------------------------------------------------
-- WINNERS
-- ---------------------------------------------------------------------
CREATE TABLE winners (
    id                          BIGSERIAL PRIMARY KEY,
    draw_id                     BIGINT NOT NULL REFERENCES draws(id),
    user_id                     BIGINT NOT NULL REFERENCES users(id),
    tier                        VARCHAR(10) NOT NULL CHECK (tier IN ('THREE', 'FOUR', 'FIVE')),
    amount                      NUMERIC(12,2) NOT NULL CHECK (amount >= 0),
    verification_status         VARCHAR(25) NOT NULL DEFAULT 'PENDING_VERIFICATION'
                                    CHECK (verification_status IN ('PENDING_VERIFICATION', 'APPROVED', 'REJECTED')),
    verification_notes          TEXT,
    verified_by                 BIGINT REFERENCES users(id),
    verified_at                 TIMESTAMPTZ,
    created_at                  TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uq_draw_user_tier UNIQUE (draw_id, user_id, tier)
);

CREATE INDEX idx_winners_user ON winners(user_id);
CREATE INDEX idx_winners_draw ON winners(draw_id);

-- ---------------------------------------------------------------------
-- WINNER PROOFS (Supabase Storage references — never public)
-- ---------------------------------------------------------------------
CREATE TABLE winner_proofs (
    id                  BIGSERIAL PRIMARY KEY,
    winner_id           BIGINT NOT NULL REFERENCES winners(id) ON DELETE CASCADE,
    storage_path        VARCHAR(500) NOT NULL,
    file_type           VARCHAR(50) NOT NULL,
    file_size_bytes     BIGINT NOT NULL,
    uploaded_at         TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- ---------------------------------------------------------------------
-- PAYOUTS
-- ---------------------------------------------------------------------
CREATE TABLE payouts (
    id                  BIGSERIAL PRIMARY KEY,
    winner_id           BIGINT NOT NULL UNIQUE REFERENCES winners(id) ON DELETE CASCADE,
    status              VARCHAR(15) NOT NULL DEFAULT 'PENDING'
                            CHECK (status IN ('PENDING', 'PROCESSING', 'PAID', 'FAILED')),
    amount              NUMERIC(12,2) NOT NULL CHECK (amount >= 0),
    paid_at             TIMESTAMPTZ,
    marked_by           BIGINT REFERENCES users(id),
    created_at          TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- ---------------------------------------------------------------------
-- AUDIT LOGS
-- ---------------------------------------------------------------------
CREATE TABLE audit_logs (
    id                  BIGSERIAL PRIMARY KEY,
    admin_id            BIGINT NOT NULL REFERENCES users(id),
    action              VARCHAR(100) NOT NULL,
    entity_type         VARCHAR(100) NOT NULL,
    entity_id           BIGINT NOT NULL,
    metadata            JSONB,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_audit_logs_entity ON audit_logs(entity_type, entity_id);
CREATE INDEX idx_audit_logs_admin ON audit_logs(admin_id);
