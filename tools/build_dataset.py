#!/usr/bin/env python3
"""Build quiz CSVs (bands / languages / songs) from public Genius lyrics datasets.

Sources (download them first, see README):
  * English: Dr3dre/Genius-song-lyrics-cleaned  (parquet files, has `views`)
  * Russian: sevenreasons/genius-lyrics-russian (genius-ru.json, no `views`)

Popularity:
  * English - by Genius page views.
  * Russian - the dataset has no views, so an artist's number of songs in the
    dataset is used as a proxy (artists with < MIN_RU_SONGS songs are dropped).

Usage:
  pip install duckdb
  python tools/build_dataset.py --en data/en --ru data/genius-ru.json --out data/out --limit 200000
"""
import argparse
import os

import duckdb

MIN_RU_SONGS = 5
MAX_PER_ARTIST = 40
MIN_LINES, MIN_CHARS, MAX_CHARS = 8, 300, 8000

CYR = "[А-Яа-яЁё]"

# Lyrics cleaning: drop all "[Chorus]" / "[Artist]" annotations, the trailing "123Embed" artifact,
# zero-width characters and runs of blank lines.
CLEAN = r"""
trim(regexp_replace(
  regexp_replace(
    regexp_replace(
      regexp_replace(
        regexp_replace(replace(replace({col}, chr(8203), ''), chr(160), ' '),
                       '\[[^\]\n]*\]', '', 'g'),
        '\d*Embed\s*$', ''),
      '\r', ''),
    '[ \t]*\n[ \t]*', chr(10), 'g'),
  '\n{{3,}}', chr(10)||chr(10), 'g'), chr(10)||' ')
"""


def clean(col):
    return CLEAN.format(col=col)


# "Аквариум (Aquarium)" -> "Аквариум": drop a Latin transliteration after a Cyrillic name
def strip_translit(col):
    return (f"trim(regexp_replace(replace(replace({col}, chr(8203), ''), chr(160), ' '), "
            f"'^(.*{CYR}.*?)\\s*\\([^А-Яа-яЁё()]*\\)$', '\\1'))")


BAD_TITLE = "(?i)(translation|перевод|traducci|tradu[cç]|übersetzung|traduction|tracklist|discography)"


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("--en", required=True, help="directory with Dr3dre parquet files")
    ap.add_argument("--ru", required=True, help="genius-ru.json")
    ap.add_argument("--out", required=True)
    ap.add_argument("--limit", type=int, default=200000, help="songs per language")
    ap.add_argument("--sample", type=int, default=0, help="read only N Russian rows (testing)")
    a = ap.parse_args()
    os.makedirs(a.out, exist_ok=True)
    lim = f"LIMIT {a.sample}" if a.sample else ""

    c = duckdb.connect()
    c.execute("PRAGMA threads=4")
    c.execute("SET memory_limit='5GB'")
    c.execute(f"SET temp_directory='{a.out}/.duckdb_tmp'")
    c.execute("SET preserve_insertion_order=false")

    # ---------------------------------------------------------------- Russian
    c.execute(f"""
      CREATE TABLE ru_raw AS
      SELECT {strip_translit('artist')} AS artist, {strip_translit('title')} AS title,
             {clean('lyrics')} AS lyrics, NULL::INTEGER AS year, 0::BIGINT AS views
      FROM read_json('{a.ru}', maximum_object_size=50000000)
      WHERE artist NOT ILIKE 'Genius%' AND coalesce(tag,'') NOT IN ('Non-Music','misc','Misc')
        AND lyrics IS NOT NULL
        -- mostly Cyrillic (the dataset also contains English-language songs by Russian artists)
        AND length(regexp_replace(lyrics, '[^А-Яа-яЁё]', '', 'g'))
            > 0.6 * length(regexp_replace(lyrics, '[^A-Za-zА-Яа-яЁё]', '', 'g'))
      {lim}
    """)
    c.execute(f"""
      CREATE TABLE ru_ok AS
      SELECT artist, title, lyrics, year, views,
             count(*) OVER (PARTITION BY artist) AS artist_songs
      FROM (SELECT DISTINCT ON (lower(artist), lower(title)) *
            FROM ru_raw
            WHERE NOT regexp_matches(title, '{BAD_TITLE}') AND length(artist) > 0
              AND length(lyrics) BETWEEN {MIN_CHARS} AND {MAX_CHARS}
              AND len(string_split(lyrics, chr(10))) >= {MIN_LINES})
    """)
    c.execute(f"""
      CREATE TABLE ru AS
      SELECT *, 'Русский язык' AS lang FROM (
        SELECT *, row_number() OVER (PARTITION BY artist ORDER BY length(lyrics) DESC) AS rn
        FROM ru_ok WHERE artist_songs >= {MIN_RU_SONGS})
      WHERE rn <= {MAX_PER_ARTIST}
      ORDER BY artist_songs DESC, artist, rn LIMIT {a.limit}
    """)

    # ---------------------------------------------------------------- English
    # Pass 1 picks songs using only the small columns; pass 2 reads the (large) lyrics
    # column just for the picked ids. Oversample a little: some texts are dropped after cleaning.
    parquet = os.path.join(a.en, "*.parquet")
    c.execute(f"""
      CREATE TABLE en_sel AS
      SELECT id, artist, title, year, views FROM (
        SELECT *, row_number() OVER (PARTITION BY artist ORDER BY views DESC) AS rn
        FROM (SELECT DISTINCT ON (lower(artist), lower(title)) id, artist, title, year, views
              FROM (SELECT id, artist, title, year, views FROM read_parquet('{parquet}')
                    WHERE language = 'en' AND language_cld3 = 'en' AND language_ft = 'en'
                      AND tag <> 'misc' AND views IS NOT NULL
                      AND artist NOT ILIKE 'Genius%' AND NOT regexp_matches(title, '{BAD_TITLE}')
                      AND char_len BETWEEN {MIN_CHARS} AND {MAX_CHARS})
              ORDER BY lower(artist), lower(title), views DESC))
      WHERE rn <= {MAX_PER_ARTIST}
      ORDER BY views DESC LIMIT {int(a.limit * 1.3)}
    """)
    c.execute(f"""
      CREATE TABLE en AS
      SELECT * FROM (
        SELECT sel.artist, sel.title, {clean('p.lyrics')} AS lyrics, sel.year, sel.views, 'English' AS lang
        FROM en_sel sel JOIN read_parquet('{parquet}') p ON p.id = sel.id)
      WHERE length(lyrics) BETWEEN {MIN_CHARS} AND {MAX_CHARS}
        AND len(string_split(lyrics, chr(10))) >= {MIN_LINES}
      ORDER BY views DESC LIMIT {a.limit}
    """)

    # ------------------------------------------------------------------ output
    c.execute("""
      CREATE TABLE all_songs AS
      SELECT lang, artist, title, lyrics, year, views FROM ru
      UNION ALL SELECT lang, artist, title, lyrics, year, views FROM en
    """)
    c.execute("CREATE TABLE bands AS SELECT row_number() OVER (ORDER BY lang, artist) AS id, lang, artist "
              "FROM (SELECT DISTINCT lang, artist FROM all_songs)")
    c.execute("COPY (SELECT id, artist AS name FROM bands ORDER BY id) "
              f"TO '{a.out}/bands.csv' (HEADER, FORMAT csv)")
    c.execute("COPY (SELECT id AS band_id, lang AS languages FROM bands ORDER BY id) "
              f"TO '{a.out}/language_texts.csv' (HEADER, FORMAT csv)")
    c.execute(f"""
      COPY (SELECT row_number() OVER (ORDER BY b.id, s.views DESC, s.title) AS song_id,
                   b.id AS band_id, s.title AS name_song, s.lyrics AS text_song,
                   CASE WHEN s.year BETWEEN 1500 AND 2100 THEN make_date(s.year, 1, 1) END AS release_date,
                   s.lang AS language
            FROM all_songs s JOIN bands b ON b.lang = s.lang AND b.artist = s.artist
            ORDER BY song_id)
      TO '{a.out}/songs.csv' (HEADER, FORMAT csv)
    """)

    for lang, n, artists in c.execute(
            "SELECT lang, count(*), count(DISTINCT artist) FROM all_songs GROUP BY lang").fetchall():
        print(f"{lang}: {n} songs, {artists} artists")


if __name__ == "__main__":
    main()
