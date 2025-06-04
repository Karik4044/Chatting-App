
CREATE DATABASE IF NOT EXISTS messenger_db;
USE messenger_db;

CREATE TABLE IF NOT EXISTS users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(255) NOT NULL UNIQUE,
    passwordHash VARCHAR(255) NOT NULL
    );
drop table users;

-- Messages table (supports user and group messages)
CREATE TABLE IF NOT EXISTS messages (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    senderid BIGINT NOT NULL,
    receiverid BIGINT, -- nullable for group messages
    groupname VARCHAR(255), -- nullable for private messages
    content TEXT NOT NULL,
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (senderid) REFERENCES users(id),
    FOREIGN KEY (receiverid) REFERENCES users(id)
    );
drop table messages;

select * from users;
select * from messages;

truncate table messages;