#!/usr/bin/env bash
# Loads the CSVs produced by tools/build_dataset.py into PostgreSQL.
# WARNING: replaces all existing bands/songs.
#
#   tools/load_dataset.sh data/out
#
# The psql command can be overridden, e.g. for docker compose (default):
#   PSQL="docker compose exec -T db psql -U musicquiz -d musicquiz -v ON_ERROR_STOP=1"
set -euo pipefail

DIR="${1:?usage: $0 <dir with bands.csv, language_texts.csv, songs.csv>}"
PSQL="${PSQL:-docker compose exec -T db psql -U ${POSTGRES_USER:-musicquiz} -d ${POSTGRES_DB:-musicquiz} -v ON_ERROR_STOP=1}"

for f in bands.csv language_texts.csv songs.csv; do
  [ -f "$DIR/$f" ] || { echo "missing $DIR/$f" >&2; exit 1; }
done

# The app creates the schema on first start (schema.sql); make sure it exists.
$PSQL -c "TRUNCATE band, language_texts, genres, countrys, song RESTART IDENTITY"
$PSQL -c "COPY band (id, name) FROM STDIN WITH (FORMAT csv, HEADER)" < "$DIR/bands.csv"
$PSQL -c "COPY language_texts (band_id, languages) FROM STDIN WITH (FORMAT csv, HEADER)" < "$DIR/language_texts.csv"
$PSQL -c "COPY song (song_id, band_id, name_song, text_song, release_date, language) FROM STDIN WITH (FORMAT csv, HEADER)" < "$DIR/songs.csv"
$PSQL -c "SELECT setval(pg_get_serial_sequence('band','id'), (SELECT max(id) FROM band))"
$PSQL -c "SELECT setval(pg_get_serial_sequence('song','song_id'), (SELECT max(song_id) FROM song))"
$PSQL -c "ANALYZE"
$PSQL -At -c "SELECT l.languages, count(DISTINCT b.id) AS bands, count(s.song_id) AS songs FROM band b JOIN language_texts l ON l.band_id=b.id JOIN song s ON s.band_id=b.id GROUP BY 1"
