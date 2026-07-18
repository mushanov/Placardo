--liquibase formatted sql

--changeset placardo:001-users
CREATE TABLE users (
    id            BIGSERIAL PRIMARY KEY,
    email         VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255),
    name          VARCHAR(100) NOT NULL,
    provider      VARCHAR(20)  NOT NULL,
    role          VARCHAR(20)  NOT NULL,
    status        VARCHAR(20)  NOT NULL,
    created_at    TIMESTAMP    NOT NULL DEFAULT now()
);

--changeset placardo:002-categories
CREATE TABLE categories (
    id        BIGSERIAL PRIMARY KEY,
    parent_id BIGINT REFERENCES categories (id),
    name      VARCHAR(100) NOT NULL,
    slug      VARCHAR(100) NOT NULL UNIQUE
);

--changeset placardo:003-ads
CREATE TABLE ads (
    id          BIGSERIAL PRIMARY KEY,
    user_id     BIGINT       NOT NULL REFERENCES users (id),
    category_id BIGINT       NOT NULL REFERENCES categories (id),
    title       VARCHAR(150) NOT NULL,
    description TEXT         NOT NULL,
    price       NUMERIC(12, 2),
    city        VARCHAR(100),
    status      VARCHAR(30)  NOT NULL,
    created_at  TIMESTAMP    NOT NULL DEFAULT now(),
    updated_at  TIMESTAMP,
    expires_at  TIMESTAMP
);
CREATE INDEX idx_ads_status ON ads (status);
CREATE INDEX idx_ads_category ON ads (category_id);
CREATE INDEX idx_ads_created ON ads (created_at DESC);

--changeset placardo:004-ad-images
CREATE TABLE ad_images (
    id         BIGSERIAL PRIMARY KEY,
    ad_id      BIGINT       NOT NULL REFERENCES ads (id) ON DELETE CASCADE,
    file_path  VARCHAR(255) NOT NULL,
    sort_order INT          NOT NULL DEFAULT 0
);
CREATE INDEX idx_ad_images_ad ON ad_images (ad_id);

--changeset placardo:005-favorites
CREATE TABLE favorites (
    user_id    BIGINT    NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    ad_id      BIGINT    NOT NULL REFERENCES ads (id) ON DELETE CASCADE,
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    PRIMARY KEY (user_id, ad_id)
);

--changeset placardo:006-comments
CREATE TABLE comments (
    id         BIGSERIAL PRIMARY KEY,
    ad_id      BIGINT    NOT NULL REFERENCES ads (id) ON DELETE CASCADE,
    user_id    BIGINT    NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    body       TEXT      NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT now()
);
CREATE INDEX idx_comments_ad ON comments (ad_id);
