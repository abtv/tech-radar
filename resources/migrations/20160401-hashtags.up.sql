CREATE TABLE hashtags(id SERIAL PRIMARY KEY,
                      created_at timestamp,
                      topic varchar(50),
                      hashtag varchar(50));

CREATE INDEX hastags_created_topic ON hashtags (created_at DESC, topic);
