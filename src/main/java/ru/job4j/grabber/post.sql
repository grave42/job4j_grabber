CREATE TABLE post (
              id SERIAL PRIMARY KEY,
              name VARCHAR(255) NOT NULL,
              text TEXT,
              link VARCHAR(255) UNIQUE NOT NULL,
              created TIMESTAMP
);