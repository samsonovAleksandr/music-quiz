
DROP TABLE IF EXISTS music;
CREATE TABLE IF NOT EXISTS band (
    id BIGINT GENERATED BY DEFAULT AS IDENTITY,
    name TEXT NOT NULL,
    countrys TEXT,
    genre TEXT,
    CONSTRAINT pk_band PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS language_texts
(
    band_id BIGINT NOT NULL,
    languages TEXT NOT NULL
);

