# 🎧 Music Quiz — угадай исполнителя по тексту песни

Telegram-бот [@MQIZ_BOT](https://t.me/MQIZ_BOT): присылает четыре строки из песни, а игрок выбирает исполнителя из четырёх вариантов. В базе около **200 000 английских** и **111 000 русских** песен популярных исполнителей.

## Как это выглядит

```
🇬🇧 Угадай исполнителя   🔥 3

┃ первая строка отрывка
┃ вторая строка отрывка
┃ третья строка отрывка
┃ четвёртая строка отрывка

[ A · Исполнитель 1 ]
[ B · Исполнитель 2 ]
[ C · Исполнитель 3 ]
[ D · Исполнитель 4 ]
```

После ответа то же сообщение превращается в разбор:

```
✅ Исполнитель 1
❌ Исполнитель 2
▫️ Исполнитель 3
▫️ Исполнитель 4

🎉 Верно!  🔥 Серия: 4
🎵 «Название песни»

[ ▶️ Дальше ] [ 📊 Статистика ]
```

## Возможности

- Вопросы на русском 🇷🇺 или английском 🇬🇧, язык выбирается отдельно для каждого чата.
- Четыре **разных** исполнителя в вариантах, правильный ответ всегда среди них.
- Серия правильных ответов 🔥, лучшая серия 🏆, процент точности с прогресс-баром.
- После ответа видно все варианты с отметками и название песни.
- Работает в личных сообщениях и в группах (`/game@MQIZ_BOT`).
- Новый вопрос генерируется за 20–30 мс даже на сотнях тысяч песен.

### Команды

| Команда | Действие |
|---|---|
| `/start` | приветствие и кнопки «Играть» / «Язык» |
| `/game` | новый вопрос |
| `/lang` | выбор языка песен |
| `/stats` | статистика: точность, текущая и лучшая серия |
| `/help` | правила |

> Язык и статистика хранятся в памяти и сбрасываются при перезапуске приложения.

## Стек

Java 17 · Spring Boot 3.1 · Spring Data JPA · PostgreSQL 15 · [TelegramBots](https://github.com/rubenlagus/TelegramBots) 6.5 (long polling) · Docker Compose · Python + DuckDB (подготовка данных).

## Структура проекта

```
src/main/java/com/example/musicquix/
├── bot/
│   ├── TelegramBot.java      # обработка команд и кнопок, состояние чатов
│   ├── Messages.java         # HTML-шаблоны сообщений
│   ├── BotInitializer.java   # регистрация бота и меню команд при старте
│   ├── BotConfig.java        # имя и токен из окружения
│   └── Language.java         # RUSSIAN / ENGLISH
├── service/MusicService.java # сборка вопроса: отрывок + 4 варианта
├── repository/               # нативные SQL-запросы случайной песни и исполнителей
├── model/                    # JPA-сущности Band, Song
└── dto/QuizQuestion.java
src/main/resources/
├── application.properties    # всё настраивается переменными окружения
└── schema.sql                # схема БД и индексы (применяется при старте)
tools/
├── build_dataset.py          # датасеты Hugging Face → CSV
└── load_dataset.sh           # CSV → PostgreSQL
```

## Быстрый старт (Docker)

1. Получите токен у [@BotFather](https://t.me/BotFather).
2. Создайте `.env`:
   ```bash
   cp .env.example .env
   ```
   и заполните `BOT_TOKEN` и `POSTGRES_PASSWORD`.
3. Запустите:
   ```bash
   docker compose up -d --build
   ```
   При первом старте приложение само создаст таблицы.
4. Загрузите песни (см. [Наполнение базы](#наполнение-базы)).
5. Проверьте логи — должна быть строка `Telegram bot @... started`:
   ```bash
   docker compose logs app
   ```

Остановить, сохранив данные: `docker compose stop`. Удалить контейнеры (данные в томе `pgdata` останутся): `docker compose down`.

> ⚠️ Один и тот же бот нельзя запускать в двух местах одновременно: long polling будет конфликтовать.

## Наполнение базы

Тексты берутся из открытых датасетов Hugging Face:

| Язык | Источник | Как отбираются популярные |
|---|---|---|
| English | [Dr3dre/Genius-song-lyrics-cleaned](https://huggingface.co/datasets/Dr3dre/Genius-song-lyrics-cleaned) (~9,4 ГБ, parquet) | по числу просмотров на Genius |
| Русский | [sevenreasons/genius-lyrics-russian](https://huggingface.co/datasets/sevenreasons/genius-lyrics-russian) (~0,5 ГБ, json) | просмотров нет, поэтому по числу песен исполнителя (≥ 5) |

Скрипт оставляет не больше 40 песен на исполнителя. Он убирает служебные пометки (`[Chorus]`, `[Куплет 1]`), переводы, дубликаты и слишком короткие или длинные тексты, а у русских имён — латинскую транслитерацию в скобках.

```bash
# 1. Скачать данные (~10 ГБ на диске)
mkdir -p data/en
for i in $(seq -w 0 31); do
  curl -L -C - -o data/en/train-$i.parquet \
    https://huggingface.co/datasets/Dr3dre/Genius-song-lyrics-cleaned/resolve/main/data/train-000$i-of-00032.parquet
done
curl -L -o data/genius-ru.json \
  https://huggingface.co/datasets/sevenreasons/genius-lyrics-russian/resolve/main/genius-ru.json

# 2. Собрать CSV (~7 минут, ~5 ГБ RAM)
pip install duckdb
python tools/build_dataset.py --en data/en --ru data/genius-ru.json --out data/out --limit 200000

# 3. Загрузить в PostgreSQL из docker compose (заменяет все песни в базе!)
set -a; source .env; set +a
tools/load_dataset.sh data/out
```

Скрипт загрузки по умолчанию работает через `docker compose exec db psql`. Для другой базы задайте свою команду в `PSQL`, например `PSQL="psql -h localhost -U user -d musicquiz"`.

Папка `data/` в git не попадает.

## Запуск без Docker

Нужны Java 17 и PostgreSQL.

```bash
export BOT_TOKEN=... \
       SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/musicquiz \
       POSTGRES_USER=... POSTGRES_PASSWORD=...
./mvnw spring-boot:run
```

## Переменные окружения

| Переменная | По умолчанию | Описание |
|---|---|---|
| `BOT_TOKEN` | — | токен бота; без него приложение стартует, но бот не запускается |
| `BOT_NAME` | `MQIZ_BOT` | username бота |
| `SPRING_DATASOURCE_URL` | `jdbc:postgresql://localhost:5432/musicquiz` | адрес БД |
| `POSTGRES_USER` / `POSTGRES_PASSWORD` | `postgres` / — | доступ к БД |
| `POSTGRES_DB` | `musicquiz` | имя БД (только для docker compose) |
| `PORT` | `3000` | HTTP-порт приложения |

Секреты хранятся только в `.env`, а `.env` добавлен в `.gitignore`.

## Тесты

```bash
./mvnw test
```

- **Юнит-тесты:** проверяют сборку вопроса и форматирование сообщений, база для них не нужна.
- **Интеграционный тест `MusicServiceDbTest`:** генерирует по 300 вопросов на каждый язык на реальной заполненной базе. Запускается, только если задана `SPRING_DATASOURCE_URL`.

## Известные ограничения

- В английском датасете у исполнителей потеряны символы вне ASCII («Beyonc», «Sigur Rs»). Это дефект источника.
- Русский датасет в основном состоит из рэпа и поп-музыки с Genius, а «популярность» в нём оценивается приблизительно.
- Права на тексты песен принадлежат правообладателям. Учитывайте это при публичном использовании бота.
