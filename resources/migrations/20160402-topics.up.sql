CREATE TABLE topics(id SERIAL PRIMARY KEY,
                    tweet_id integer REFERENCES tweets (id),
                    created_at timestamp,
                    topic varchar(50));

CREATE INDEX topics_created ON topics (created_at DESC, topic);
