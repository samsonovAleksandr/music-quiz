## Музыкальный квиз!
### Угадай исполнителя по отрывку текста песни!

Telegram-бот [@MQIZ_BOT](https://t.me/MQIZ_BOT): показывает 4 строки из песни, нужно выбрать исполнителя из четырёх вариантов. В базе — ~200 тыс. английских и ~111 тыс. русских песен.

### Команды бота
| Команда | Действие |
|---|---|
| `/game` | новый вопрос |
| `/lang` | выбор языка песен (русский / English) |
| `/stats` | статистика правильных ответов |
| `/help` | помощь |

### Запуск (Docker)
```bash
cp .env.example .env   # заполнить BOT_TOKEN и POSTGRES_PASSWORD
docker compose up -d --build
```
Токен бота получают у [@BotFather](https://t.me/BotFather). **Токены и пароли хранятся только в `.env`/переменных окружения.**

### Запуск локально
Нужны Java 17 и PostgreSQL. Схема создаётся автоматически (`schema.sql`).
```bash
export BOT_TOKEN=... SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/musicquiz POSTGRES_USER=... POSTGRES_PASSWORD=...
./mvnw spring-boot:run
```

### Наполнение базы
Тексты берутся из открытых датасетов Hugging Face (скрапинг сайтов не используется):

| Язык | Источник | Отбор |
|---|---|---|
| English | [Dr3dre/Genius-song-lyrics-cleaned](https://huggingface.co/datasets/Dr3dre/Genius-song-lyrics-cleaned) (~9,4 ГБ, parquet) | топ по просмотрам Genius, до 40 песен на исполнителя |
| Русский | [sevenreasons/genius-lyrics-russian](https://huggingface.co/datasets/sevenreasons/genius-lyrics-russian) (~0,5 ГБ, json) | в датасете нет просмотров, поэтому «популярность» = число песен исполнителя (≥ 5), до 40 песен на исполнителя |

В итоге: ~200 000 английских (40 тыс. исполнителей) и ~111 000 русских (6 тыс. исполнителей) песен: в русском источнике всего 192 тыс. записей, а после фильтров остаётся меньше.

Скрипт убирает служебные пометки (`[Chorus]`, `[Куплет 1]`), переводы, дубликаты и слишком короткие/длинные тексты.

```bash
# 1. скачать данные (понадобится ~10 ГБ на диске)
mkdir -p data/en
for i in $(seq -w 0 31); do
  curl -L -C - -o data/en/train-$i.parquet \
    https://huggingface.co/datasets/Dr3dre/Genius-song-lyrics-cleaned/resolve/main/data/train-000$i-of-00032.parquet
done
curl -L -o data/genius-ru.json https://huggingface.co/datasets/sevenreasons/genius-lyrics-russian/resolve/main/genius-ru.json

# 2. собрать CSV (по 200 000 песен на язык)
pip install duckdb
python tools/build_dataset.py --en data/en --ru data/genius-ru.json --out data/out --limit 200000

# 3. загрузить в PostgreSQL из docker compose (стирает существующие песни!)
tools/load_dataset.sh data/out
```
Известные ограничения источников: у англоязычных исполнителей в датасете Dr3dre потеряны символы вне ASCII («Beyonc», «Sigur Rs»); русский датасет в основном рэп/поп с Genius. Права на тексты принадлежат правообладателям, учитывайте это при публичном использовании бота.

### Переменные окружения
`BOT_TOKEN`, `BOT_NAME`, `SPRING_DATASOURCE_URL`, `POSTGRES_USER`, `POSTGRES_PASSWORD`, `PORT`.

### Тесты
```bash
./mvnw test
```
