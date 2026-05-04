-- ============================================================
-- Eventra — V1__initial_schema.sql
-- Schema inițială completă: 24 tabele
-- Versiune: 1.0 | Data: Mai 2026
-- ============================================================

-- ── EXTENSII ────────────────────────────────────────────────
CREATE EXTENSION IF NOT EXISTS "pgcrypto";  -- pentru gen_random_uuid()

-- ── ENUM TYPES ──────────────────────────────────────────────
CREATE TYPE user_role AS ENUM (
    'CLIENT',
    'VENDOR',
    'ADMIN'
);

CREATE TYPE user_status AS ENUM (
    'PENDING_VERIFICATION',
    'ACTIVE',
    'SUSPENDED',
    'SCHEDULED_DELETION'
);

CREATE TYPE vendor_status AS ENUM (
    'PENDING_REVIEW',
    'VERIFIED',
    'SUSPENDED'
);

CREATE TYPE subscription_plan AS ENUM (
    'FREE',
    'STANDARD',
    'PREMIUM'
);

CREATE TYPE rsvp_status AS ENUM (
    'PENDING',
    'CONFIRMED',
    'DECLINED',
    'MAYBE'
);

CREATE TYPE budget_category AS ENUM (
    'LOCATIE',
    'CATERING',
    'MUZICA',
    'FOTO_VIDEO',
    'DECOR_FLORI',
    'TRANSPORT',
    'INVITATII',
    'BEAUTY',
    'DIVERTISMENT',
    'ALTELE'
);

CREATE TYPE payment_status AS ENUM (
    'PLANNED',
    'ADVANCE_PAID',
    'FULLY_PAID'
);

CREATE TYPE review_status AS ENUM (
    'PENDING',
    'APPROVED',
    'REJECTED'
);

CREATE TYPE contact_request_status AS ENUM (
    'NEW',
    'READ',
    'REPLIED'
);

CREATE TYPE audit_action AS ENUM (
    'USER_DELETED',
    'USER_SUSPENDED',
    'EMAIL_CHANGED',
    'PASSWORD_RESET',
    'VENDOR_VERIFIED',
    'VENDOR_SUSPENDED',
    'REVIEW_APPROVED',
    'REVIEW_REJECTED',
    'SUBSCRIPTION_CHANGED',
    'ADMIN_LOGIN'
);

-- ============================================================
-- TABEL 1: users
-- Toți utilizatorii platformei: clienți, furnizori, admini
-- ============================================================
CREATE TABLE users (
    id              UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    email           VARCHAR(255) NOT NULL UNIQUE,
    password_hash   VARCHAR(255) NOT NULL,
    full_name       VARCHAR(255) NOT NULL,
    role            user_role   NOT NULL DEFAULT 'CLIENT',
    status          user_status NOT NULL DEFAULT 'PENDING_VERIFICATION',
    email_verified  BOOLEAN     NOT NULL DEFAULT FALSE,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted_at      TIMESTAMPTZ NULL
);

CREATE INDEX idx_users_email    ON users(email);
CREATE INDEX idx_users_status   ON users(status);
CREATE INDEX idx_users_role     ON users(role);
CREATE INDEX idx_users_deleted  ON users(deleted_at) WHERE deleted_at IS NULL;

-- ============================================================
-- TABEL 2: email_verification_tokens
-- Tokeni pentru verificarea adresei de email la înregistrare
-- ============================================================
CREATE TABLE email_verification_tokens (
    id          UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id     UUID        NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    token_hash  VARCHAR(255) NOT NULL UNIQUE,
    expires_at  TIMESTAMPTZ NOT NULL,
    used_at     TIMESTAMPTZ NULL,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_evtoken_user ON email_verification_tokens(user_id);
CREATE INDEX idx_evtoken_hash ON email_verification_tokens(token_hash);

-- ============================================================
-- TABEL 3: refresh_tokens
-- JWT Refresh Tokens stocate hashed în DB
-- ============================================================
CREATE TABLE refresh_tokens (
    id          UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id     UUID        NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    token_hash  VARCHAR(255) NOT NULL UNIQUE,
    expires_at  TIMESTAMPTZ NOT NULL,
    revoked_at  TIMESTAMPTZ NULL,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_rtoken_user ON refresh_tokens(user_id);
CREATE INDEX idx_rtoken_hash ON refresh_tokens(token_hash);

-- ============================================================
-- TABEL 4: vendor_categories
-- Categoriile de furnizori: FOTO, MUZ, LOC, DEC etc.
-- ============================================================
CREATE TABLE vendor_categories (
    id          UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    code        VARCHAR(50) NOT NULL UNIQUE,
    name        VARCHAR(100) NOT NULL,
    description TEXT        NULL,
    sort_order  INT         NOT NULL DEFAULT 0,
    is_active   BOOLEAN     NOT NULL DEFAULT TRUE
);

-- ============================================================
-- TABEL 5: event_types
-- Tipurile de evenimente suportate: NUNTA, BOTEZ, CORPORATE etc.
-- ============================================================
CREATE TABLE event_types (
    id          UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    code        VARCHAR(50) NOT NULL UNIQUE,
    name        VARCHAR(100) NOT NULL,
    icon        VARCHAR(50) NULL,
    sort_order  INT         NOT NULL DEFAULT 0,
    is_active   BOOLEAN     NOT NULL DEFAULT TRUE
);

-- ============================================================
-- TABEL 6: counties
-- Județele din România (42 + București)
-- ============================================================
CREATE TABLE counties (
    id      UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    code    VARCHAR(10) NOT NULL UNIQUE,
    name    VARCHAR(100) NOT NULL
);

-- ============================================================
-- TABEL 7: vendor_profiles
-- Profilurile furnizorilor de servicii
-- ============================================================
CREATE TABLE vendor_profiles (
    id                   UUID              PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id              UUID              NOT NULL UNIQUE REFERENCES users(id) ON DELETE CASCADE,
    business_name        VARCHAR(255)      NOT NULL,
    description          TEXT              NULL,
    phone                VARCHAR(50)       NULL,
    website_url          VARCHAR(500)      NULL,
    instagram_url        VARCHAR(500)      NULL,
    facebook_url         VARCHAR(500)      NULL,
    price_from           NUMERIC(10, 2)    NULL,
    price_to             NUMERIC(10, 2)    NULL,
    price_currency       VARCHAR(3)        NOT NULL DEFAULT 'RON',
    status               vendor_status     NOT NULL DEFAULT 'PENDING_REVIEW',
    subscription_plan    subscription_plan NOT NULL DEFAULT 'FREE',
    subscription_ends_at TIMESTAMPTZ       NULL,
    average_rating       NUMERIC(3, 2)     NULL,
    review_count         INT               NOT NULL DEFAULT 0,
    view_count           INT               NOT NULL DEFAULT 0,
    contact_count        INT               NOT NULL DEFAULT 0,
    is_featured          BOOLEAN           NOT NULL DEFAULT FALSE,
    featured_until       TIMESTAMPTZ       NULL,
    created_at           TIMESTAMPTZ       NOT NULL DEFAULT NOW(),
    updated_at           TIMESTAMPTZ       NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_vendor_status   ON vendor_profiles(status);
CREATE INDEX idx_vendor_plan     ON vendor_profiles(subscription_plan);
CREATE INDEX idx_vendor_rating   ON vendor_profiles(average_rating DESC NULLS LAST);
CREATE INDEX idx_vendor_featured ON vendor_profiles(is_featured, featured_until);
CREATE INDEX idx_vendor_user     ON vendor_profiles(user_id);

-- ============================================================
-- TABEL 8: vendor_photos
-- Fotografiile din profilul furnizorului (stocate în Cloudinary)
-- ============================================================
CREATE TABLE vendor_photos (
    id              UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    vendor_id       UUID        NOT NULL REFERENCES vendor_profiles(id) ON DELETE CASCADE,
    cloudinary_id   VARCHAR(255) NOT NULL,
    url             VARCHAR(1000) NOT NULL,
    thumbnail_url   VARCHAR(1000) NULL,
    is_cover        BOOLEAN     NOT NULL DEFAULT FALSE,
    sort_order      INT         NOT NULL DEFAULT 0,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_vphoto_vendor ON vendor_photos(vendor_id);

-- ============================================================
-- TABEL 9: vendor_videos
-- Video-urile de prezentare ale furnizorului (stocate în Cloudinary)
-- Separat de photos: tipuri diferite de media, limite diferite per plan
-- ============================================================
CREATE TABLE vendor_videos (
    id              UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    vendor_id       UUID        NOT NULL REFERENCES vendor_profiles(id) ON DELETE CASCADE,
    cloudinary_id   VARCHAR(255) NOT NULL,
    url             VARCHAR(1000) NOT NULL,
    thumbnail_url   VARCHAR(1000) NULL,
    duration_sec    INT         NULL,
    sort_order      INT         NOT NULL DEFAULT 0,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_vvideo_vendor ON vendor_videos(vendor_id);

-- ============================================================
-- TABEL 10: vendor_category_assignments
-- M2M: un furnizor poate fi în maximum 3 categorii
-- ============================================================
CREATE TABLE vendor_category_assignments (
    vendor_id   UUID    NOT NULL REFERENCES vendor_profiles(id) ON DELETE CASCADE,
    category_id UUID    NOT NULL REFERENCES vendor_categories(id) ON DELETE CASCADE,
    is_primary  BOOLEAN NOT NULL DEFAULT FALSE,
    PRIMARY KEY (vendor_id, category_id)
);

-- ============================================================
-- TABEL 11: vendor_county_coverage
-- M2M: județele în care activează furnizorul
-- ============================================================
CREATE TABLE vendor_county_coverage (
    vendor_id   UUID NOT NULL REFERENCES vendor_profiles(id) ON DELETE CASCADE,
    county_id   UUID NOT NULL REFERENCES counties(id) ON DELETE CASCADE,
    PRIMARY KEY (vendor_id, county_id)
);

CREATE INDEX idx_vcounty_county ON vendor_county_coverage(county_id);

-- ============================================================
-- TABEL 12: vendor_event_type_coverage
-- M2M: tipurile de evenimente pentru care furnizorul e disponibil
-- ============================================================
CREATE TABLE vendor_event_type_coverage (
    vendor_id       UUID NOT NULL REFERENCES vendor_profiles(id) ON DELETE CASCADE,
    event_type_id   UUID NOT NULL REFERENCES event_types(id) ON DELETE CASCADE,
    PRIMARY KEY (vendor_id, event_type_id)
);

-- ============================================================
-- TABEL 13: vendor_availability
-- Calendarul de disponibilitate al furnizorului
-- Furnizorul marchează datele OCUPATE; restul = liber
-- ============================================================
CREATE TABLE vendor_availability (
    id          UUID    PRIMARY KEY DEFAULT gen_random_uuid(),
    vendor_id   UUID    NOT NULL REFERENCES vendor_profiles(id) ON DELETE CASCADE,
    date        DATE    NOT NULL,
    is_blocked  BOOLEAN NOT NULL DEFAULT TRUE,
    note        VARCHAR(255) NULL,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE (vendor_id, date)
);

CREATE INDEX idx_vavail_vendor_date ON vendor_availability(vendor_id, date);

-- ============================================================
-- TABEL 14: vendor_contact_requests
-- Cererile de contact trimise furnizorilor de către clienți
-- ============================================================
CREATE TABLE vendor_contact_requests (
    id              UUID                   PRIMARY KEY DEFAULT gen_random_uuid(),
    vendor_id       UUID                   NOT NULL REFERENCES vendor_profiles(id) ON DELETE CASCADE,
    client_id       UUID                   NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    event_type_id   UUID                   NULL REFERENCES event_types(id) ON DELETE SET NULL,
    event_date      DATE                   NULL,
    guest_count     INT                    NULL,
    message         TEXT                   NOT NULL,
    status          contact_request_status NOT NULL DEFAULT 'NEW',
    created_at      TIMESTAMPTZ            NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_vcontact_vendor ON vendor_contact_requests(vendor_id, status);
CREATE INDEX idx_vcontact_client ON vendor_contact_requests(client_id);

-- ============================================================
-- TABEL 15: vendor_subscription_history
-- Istoricul complet al schimbărilor de plan ale furnizorilor
-- Necesar pentru billing, audit și suport
-- ============================================================
CREATE TABLE vendor_subscription_history (
    id          UUID              PRIMARY KEY DEFAULT gen_random_uuid(),
    vendor_id   UUID              NOT NULL REFERENCES vendor_profiles(id) ON DELETE CASCADE,
    from_plan   subscription_plan NOT NULL,
    to_plan     subscription_plan NOT NULL,
    changed_at  TIMESTAMPTZ       NOT NULL DEFAULT NOW(),
    changed_by  UUID              NULL REFERENCES users(id) ON DELETE SET NULL,
    reason      VARCHAR(500)      NULL
);

CREATE INDEX idx_vsub_history_vendor ON vendor_subscription_history(vendor_id);

-- ============================================================
-- TABEL 16: events
-- Evenimentele create de clienți pentru planificare
-- ============================================================
CREATE TABLE events (
    id              UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    client_id       UUID        NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    event_type_id   UUID        NOT NULL REFERENCES event_types(id),
    name            VARCHAR(255) NOT NULL,
    event_date      DATE        NULL,
    location        VARCHAR(500) NULL,
    guest_count     INT         NULL,
    total_budget    NUMERIC(12, 2) NULL,
    notes           TEXT        NULL,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted_at      TIMESTAMPTZ NULL
);

CREATE INDEX idx_events_client  ON events(client_id);
CREATE INDEX idx_events_date    ON events(event_date);
CREATE INDEX idx_events_type    ON events(event_type_id);
CREATE INDEX idx_events_active  ON events(client_id) WHERE deleted_at IS NULL;

-- ============================================================
-- TABEL 17: guests
-- Invitații per eveniment cu status RSVP
-- ============================================================
CREATE TABLE guests (
    id          UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    event_id    UUID        NOT NULL REFERENCES events(id) ON DELETE CASCADE,
    full_name   VARCHAR(255) NOT NULL,
    phone       VARCHAR(50) NULL,
    email       VARCHAR(255) NULL,
    rsvp_status rsvp_status NOT NULL DEFAULT 'PENDING',
    notes       VARCHAR(500) NULL,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_guests_event ON guests(event_id);
CREATE INDEX idx_guests_rsvp  ON guests(event_id, rsvp_status);

-- ============================================================
-- TABEL 18: budget_items
-- Itemele de buget per eveniment
-- ============================================================
CREATE TABLE budget_items (
    id              UUID            PRIMARY KEY DEFAULT gen_random_uuid(),
    event_id        UUID            NOT NULL REFERENCES events(id) ON DELETE CASCADE,
    name            VARCHAR(255)    NOT NULL,
    category        budget_category NOT NULL DEFAULT 'ALTELE',
    total_amount    NUMERIC(12, 2)  NOT NULL,
    paid_amount     NUMERIC(12, 2)  NOT NULL DEFAULT 0,
    payment_status  payment_status  NOT NULL DEFAULT 'PLANNED',
    vendor_name     VARCHAR(255)    NULL,
    notes           VARCHAR(1000)   NULL,
    created_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_budget_event    ON budget_items(event_id);
CREATE INDEX idx_budget_category ON budget_items(event_id, category);

-- ============================================================
-- TABEL 19: seating_tables
-- Mesele din planul de așezare al evenimentului
-- ============================================================
CREATE TABLE seating_tables (
    id          UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    event_id    UUID        NOT NULL REFERENCES events(id) ON DELETE CASCADE,
    name        VARCHAR(100) NOT NULL,
    capacity    INT         NOT NULL,
    sort_order  INT         NOT NULL DEFAULT 0,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_seating_tables_event ON seating_tables(event_id);

-- ============================================================
-- TABEL 20: seating_assignments
-- M2M: un invitat = o singură masă (UNIQUE pe guest_id)
-- ============================================================
CREATE TABLE seating_assignments (
    id          UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    table_id    UUID        NOT NULL REFERENCES seating_tables(id) ON DELETE CASCADE,
    guest_id    UUID        NOT NULL UNIQUE REFERENCES guests(id) ON DELETE CASCADE,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_seating_asgn_table ON seating_assignments(table_id);

-- ============================================================
-- TABEL 21: checklist_templates
-- Template-uri pre-populate per tip de eveniment
-- La creare eveniment, se copiază itemele relevante în checklist_items
-- ============================================================
CREATE TABLE checklist_templates (
    id              UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    event_type_id   UUID        NOT NULL REFERENCES event_types(id) ON DELETE CASCADE,
    title           VARCHAR(500) NOT NULL,
    days_before     INT         NULL,
    sort_order      INT         NOT NULL DEFAULT 0,
    is_active       BOOLEAN     NOT NULL DEFAULT TRUE
);

CREATE INDEX idx_checklist_tmpl_type ON checklist_templates(event_type_id);

-- ============================================================
-- TABEL 22: checklist_items
-- Sarcinile din checklist-ul unui eveniment specific
-- ============================================================
CREATE TABLE checklist_items (
    id              UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    event_id        UUID        NOT NULL REFERENCES events(id) ON DELETE CASCADE,
    template_id     UUID        NULL REFERENCES checklist_templates(id) ON DELETE SET NULL,
    title           VARCHAR(500) NOT NULL,
    is_completed    BOOLEAN     NOT NULL DEFAULT FALSE,
    due_date        DATE        NULL,
    sort_order      INT         NOT NULL DEFAULT 0,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_checklist_event ON checklist_items(event_id);

-- ============================================================
-- TABEL 23: reviews
-- Recenziile clienților despre furnizori
-- Un client poate scrie o singură recenzie per furnizor
-- ============================================================
CREATE TABLE reviews (
    id          UUID          PRIMARY KEY DEFAULT gen_random_uuid(),
    vendor_id   UUID          NOT NULL REFERENCES vendor_profiles(id) ON DELETE CASCADE,
    client_id   UUID          NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    event_id    UUID          NULL REFERENCES events(id) ON DELETE SET NULL,
    rating      SMALLINT      NOT NULL CHECK (rating BETWEEN 1 AND 5),
    comment     TEXT          NULL,
    status      review_status NOT NULL DEFAULT 'PENDING',
    reviewed_at TIMESTAMPTZ   NULL,
    created_at  TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
    UNIQUE (vendor_id, client_id)
);

CREATE INDEX idx_reviews_vendor ON reviews(vendor_id, status);
CREATE INDEX idx_reviews_client ON reviews(client_id);

-- ============================================================
-- TABEL 24: audit_log
-- Log operațiuni sensibile — obligatoriu GDPR
-- ============================================================
CREATE TABLE audit_log (
    id              UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    action          audit_action NOT NULL,
    performed_by    UUID         NULL REFERENCES users(id) ON DELETE SET NULL,
    target_user_id  UUID         NULL REFERENCES users(id) ON DELETE SET NULL,
    details         JSONB        NULL,
    ip_address      VARCHAR(45)  NULL,
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_audit_action    ON audit_log(action);
CREATE INDEX idx_audit_performer ON audit_log(performed_by);
CREATE INDEX idx_audit_target    ON audit_log(target_user_id);
CREATE INDEX idx_audit_created   ON audit_log(created_at DESC);
