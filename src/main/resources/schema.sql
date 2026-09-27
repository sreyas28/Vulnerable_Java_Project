CREATE TABLE users (
    id INT PRIMARY KEY,
    username VARCHAR(64) NOT NULL,
    password VARCHAR(128) NOT NULL,
    role VARCHAR(32) NOT NULL,
    email VARCHAR(128)
);

CREATE TABLE accounts (
    id INT PRIMARY KEY,
    owner VARCHAR(64) NOT NULL,
    balance DECIMAL(12, 2) NOT NULL
);

INSERT INTO users (id, username, password, role, email) VALUES
    (1, 'alice', 'alice-pass', 'user', 'alice@example.test'),
    (2, 'bob', 'bob-secret', 'user', 'bob@example.test'),
    (3, 'admin', 'admin-change-me', 'admin', 'admin@example.test');

INSERT INTO accounts (id, owner, balance) VALUES
    (100, 'alice', 500.00),
    (101, 'bob', 250.50),
    (102, 'admin', 10000.00);
