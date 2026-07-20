--liquibase formatted sql

--changeset placardo:007-seed-root-categories
INSERT INTO categories (name, slug) VALUES
    ('Электроника', 'electronics'),
    ('Недвижимость', 'realty'),
    ('Транспорт', 'transport'),
    ('Работа', 'jobs'),
    ('Услуги', 'services'),
    ('Хобби и отдых', 'hobby'),
    ('Животные', 'pets'),
    ('Для дома', 'home');

--changeset placardo:008-seed-sub-categories
INSERT INTO categories (parent_id, name, slug) VALUES
    ((SELECT id FROM categories WHERE slug = 'electronics'), 'Телефоны', 'phones'),
    ((SELECT id FROM categories WHERE slug = 'electronics'), 'Ноутбуки', 'laptops'),
    ((SELECT id FROM categories WHERE slug = 'electronics'), 'Аудио и видео', 'audio-video'),
    ((SELECT id FROM categories WHERE slug = 'transport'), 'Автомобили', 'cars'),
    ((SELECT id FROM categories WHERE slug = 'transport'), 'Велосипеды', 'bikes'),
    ((SELECT id FROM categories WHERE slug = 'realty'), 'Аренда', 'rent'),
    ((SELECT id FROM categories WHERE slug = 'realty'), 'Продажа', 'sale');
