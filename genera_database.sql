CREATE DATABASE asktoreply;

USE asktoreply;

CREATE TABLE utenti (
    id VARCHAR(256),
    email VARCHAR(320) NOT NULL,
    password_hash VARCHAR(64) NOT NULL,
    nuova_password VARCHAR(64),
    username VARCHAR(30) NOT NULL,
    nome VARCHAR(50) NOT NULL,
    cognome VARCHAR(50) NOT NULL,
    ruolo_id INTEGER,
    PRIMARY KEY (id),
    FOREIGN KEY (ruolo_id) REFERENCES ruoli(id)
 );

 CREATE TABLE ruoli (
     id INTEGER,
     nome VARCHAR(20) NOT NULL,
    PRIMARY KEY (id)
 );

 CREATE TABLE partecipanti (
     id_utente VARCHAR(256),
     punteggio INTEGER DEFAULT 0,
     numero_segnalazioni INTEGER DEFAULT 0,
     PRIMARY KEY(id_utente),
     FOREIGN KEY(id_utente) REFERENCES utenti(id)
 );
