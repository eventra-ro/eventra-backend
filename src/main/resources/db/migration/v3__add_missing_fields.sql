-- V3__add_missing_fields.sql

-- 1. slug pe vendor_profiles
-- URL-uri SEO-friendly: /furnizori/foto/ion-popescu-photography-b
ALTER TABLE vendor_profiles
    ADD COLUMN slug VARCHAR(255) UNIQUE;

CREATE INDEX idx_vendor_profiles_slug ON vendor_profiles(slug);


-- 2. budget_currency pe events
-- Moneda bugetului evenimentului (RON/EUR/USD)
ALTER TABLE events
    ADD COLUMN budget_currency VARCHAR(3) NOT NULL DEFAULT 'RON';


-- 3. plus_one pe guests
-- Invitatul vine cu partener (+1 persoana extra)
ALTER TABLE guests
    ADD COLUMN plus_one BOOLEAN NOT NULL DEFAULT FALSE;