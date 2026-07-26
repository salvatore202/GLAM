-- test_fixtures.sql
--
-- NOT part of the original project. This file is additive and does not modify
-- databaseglam10.sql in any way.
--
-- Why this exists:
-- src/Test/BoundayMaestroTest.java and src/Test/BoundaryStudenteRegistratoTest.java
-- assert against a registered student "vdeluca" / "password111" and a
-- GiornoDisponibilita for Maestro 1 (Ventresca) on 19/05/2025 and 20/05/2025,
-- with the 17:00 slot on each day already booked. None of that data exists in
-- databaseglam10.sql, so on a clean database 10 of the 13 existing tests fail
-- before ever reaching the logic they intend to check. This script reconstructs
-- the missing rows so the ORIGINAL, unmodified test suite can run headlessly
-- (e.g. in CI) exactly as it was written.
--
-- Run this AFTER databaseglam10.sql, against the same database.
-- The student's name/phone/email are placeholders (only the username and
-- password matter to the tests) -- rename freely.

USE DatabaseGLAM10;

INSERT INTO StudenteRegistrato (nome, cognome, datadinascita, numeroditelefono, username, email, password) VALUES
('Veronica', 'Deluca', '2000-04-10', '3391112223', 'vdeluca', 'veronica.deluca@example.com', 'password111');
SET @idStudenteFixture = LAST_INSERT_ID();

-- Day 1: 19/05/2025 for Maestro 1 (Paolo Ventresca)
INSERT INTO GiornoDisponibilita (data, idMaestro) VALUES ('2025-05-19', 1);
SET @idGiorno1 = LAST_INSERT_ID();

INSERT INTO Lezione (ora, livello, idMaestro, idData, Disponibile) VALUES
('17:00:00', 'BASE', 1, @idGiorno1, 'si'),
('18:00:00', 'BASE', 1, @idGiorno1, 'si'),
('19:00:00', 'BASE', 1, @idGiorno1, 'si'),
('20:00:00', 'BASE', 1, @idGiorno1, 'si');

-- Day 2: 20/05/2025 for Maestro 1 (Paolo Ventresca)
INSERT INTO GiornoDisponibilita (data, idMaestro) VALUES ('2025-05-20', 1);
SET @idGiorno2 = LAST_INSERT_ID();

INSERT INTO Lezione (ora, livello, idMaestro, idData, Disponibile) VALUES
('17:00:00', 'BASE', 1, @idGiorno2, 'si'),
('18:00:00', 'BASE', 1, @idGiorno2, 'si'),
('19:00:00', 'BASE', 1, @idGiorno2, 'si'),
('20:00:00', 'BASE', 1, @idGiorno2, 'si');

-- vdeluca books the 17:00 PIANOFORTE lesson on both days (matches what
-- prenotaLezione_lezioneNonDisponibileTest / disdiciLezione_inputValidiTest expect)
SET @idLezione1_17 = (SELECT idLezione FROM Lezione WHERE idData = @idGiorno1 AND ora = '17:00:00');
SET @idLezione2_17 = (SELECT idLezione FROM Lezione WHERE idData = @idGiorno2 AND ora = '17:00:00');

UPDATE Lezione SET Disponibile = 'no' WHERE idLezione IN (@idLezione1_17, @idLezione2_17);

INSERT INTO Prenotazione (costo, idLezione, idStudente) VALUES
(15.00, @idLezione1_17, @idStudenteFixture),
(15.00, @idLezione2_17, @idStudenteFixture);
