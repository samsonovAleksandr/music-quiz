-- DROP TABLE IF EXISTS authors;
-- DROP TABLE IF EXISTS songs;
-- DROP TABLE IF EXISTS song_lyric_snippets;


-- CREATE TABLE IF NOT EXISTS authors (
--     id BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
--     author_name VARCHAR(256) NOT NULL
-- );
--
-- CREATE TABLE IF NOT EXISTS songs (
--     id BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
--     author_id BIGINT NOT NULL,
--     song_name VARCHAR(256) NOT NULL,
--     CONSTRAINT "FK_author_id" FOREIGN KEY (author_id) REFERENCES authors (id) ON DELETE CASCADE
-- );
--
-- CREATE  TABLE IF NOT EXISTS song_lyric_snippets (
--     id BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
--     song_id BIGINT NOT NULL,
--     snippet_text TEXT,
--     CONSTRAINT "FK_song_id" FOREIGN KEY (song_id) REFERENCES songs (id) ON DELETE CASCADE
-- );

CREATE TABLE IF NOT EXISTS music (
    id BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    title TEXT NOT NULL,
    artist TEXT NOT NULL,
    lyrics TEXT NOT NULL
);


