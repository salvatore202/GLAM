-- Crea il database
CREATE DATABASE DatabaseGLAM10;
USE DatabaseGLAM10;

-- Creazione della tabella Maestro
CREATE TABLE Maestro (
    idMaestro INT AUTO_INCREMENT PRIMARY KEY,
    nome VARCHAR(50) NOT NULL,
    cognome VARCHAR(50) NOT NULL,
    numeroditelefono VARCHAR(20),
    email VARCHAR(100) NOT NULL,
    password VARCHAR(100) NOT NULL
);

-- Creazione della tabella Strumento
CREATE TABLE Strumento (
    idStrumento INT AUTO_INCREMENT PRIMARY KEY,
    nome VARCHAR(50) NOT NULL,
    idMaestro INT,
    FOREIGN KEY (idMaestro) REFERENCES Maestro(idMaestro)
);

-- Creazione della tabella StudenteRegistrato
CREATE TABLE StudenteRegistrato (
    idStudente INT AUTO_INCREMENT PRIMARY KEY,
    nome VARCHAR(50) NOT NULL,
    cognome VARCHAR(50) NOT NULL,
    datadinascita DATE,
    numeroditelefono VARCHAR(20),
    username VARCHAR(50) NOT NULL,
    email VARCHAR(100) NOT NULL,
    password VARCHAR(100) NOT NULL
);

-- Creazione della tabella GiornoDisponibilita
CREATE TABLE GiornoDisponibilita (
    idData INT AUTO_INCREMENT PRIMARY KEY,
    data DATE NOT NULL,
    idMaestro INT NOT NULL,
    FOREIGN KEY (idMaestro) REFERENCES Maestro(idMaestro)
);

-- Creazione della tabella Lezione
CREATE TABLE Lezione (
    idLezione INT AUTO_INCREMENT PRIMARY KEY,
    ora TIME NOT NULL,
    livello VARCHAR(50) NOT NULL,
    idMaestro INT NOT NULL,
    idData INT NOT NULL,
    Disponibile VARCHAR(10),
    FOREIGN KEY (idMaestro) REFERENCES Maestro(idMaestro),
    FOREIGN KEY (idData) REFERENCES GiornoDisponibilita(idData)
);

-- Creazione della tabella Prenotazione
CREATE TABLE Prenotazione (
    idPrenotazione INT AUTO_INCREMENT PRIMARY KEY,
    costo DECIMAL(10, 2) NOT NULL,
    idLezione INT NOT NULL,
    idStudente INT,
    FOREIGN KEY (idLezione) REFERENCES Lezione(idLezione),
    FOREIGN KEY (idStudente) REFERENCES StudenteRegistrato(idStudente)
);

-- Inserimento dei Maestri
INSERT INTO Maestro (nome, cognome, numeroditelefono, email, password) VALUES
('Paolo', 'Ventresca', '1234567890', 'paolo.ventresca@example.com', 'password123'),
('Angelo', 'Trapanese', '0987654321', 'angelo.trapanese@example.com', 'password456'),
('Loredana', 'Circuito', '3334445550', 'loredana.circuito@example.com', 'password222');

-- Inserimento degli Strumenti
INSERT INTO Strumento (nome, idMaestro) VALUES
('VIOLINO', 3), -- Loredana
('CHITARRA', 2), -- Angelo
('PIANOFORTE', 1), -- Paolo
('BATTERIA', 3), -- Loredana
('SASSOFONO', 2); -- Angelo

-- Inserimento degli Studenti Registrati
INSERT INTO StudenteRegistrato (nome, cognome, datadinascita, numeroditelefono, username, email, password) VALUES
('Ludovico', 'Einaudi', '1955-12-23', '3390078564', 'vleinaudi', 'ludovico.einaudi@example.com', 'password333'),
('Alex', 'delPiero', '1974-11-09', '3393378565', 'adelpiero', 'alex.delpiero@example.com', 'password444');

-- Assegnazione dei privilegi all'utente root
GRANT ALL PRIVILEGES ON DatabaseGLAM10.* TO 'root'@'localhost';
FLUSH PRIVILEGES;

SELECT * FROM giornodisponibilita;
SELECT * FROM Maestro;
SELECT * FROM strumento;
SELECT * FROM prenotazione;
SELECT * FROM StudenteRegistrato;
SELECT * FROM lezione;