# Placardo
###### Spring Project - доска объявлений
http://localhost:8080/

### Варианты запуска:
**Вариант-А: полностью в Docker**
```shell
# 1. Клонировать репозиторий и зайти в папку
git clone <адрес-репозитория>
cd placardo

# 2. (Необязательно) настроить вход через Google
cp .env.example .env
# откройте .env и впишите GOOGLE_CLIENT_ID и GOOGLE_CLIENT_SECRET

# 3. Собрать и запустить
docker compose up --build
```
**Вариант-Б: из IDE, база в Docker**
```shell
# 1. Поднять базу данных. Контейнер с PostgreSQL слушает localhost:5432
docker compose up postgres

# 2. (Необязательно) настроить вход через Google. В Edit Configurations прописать Environment variables
GOOGLE_CLIENT_ID = ваш Client ID
GOOGLE_CLIENT_SECRET = ваш Client Secret

# 3. Запустить
src/main/java/com/placardo/PlacardoApplication.java
```