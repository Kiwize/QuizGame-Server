drop table if exists Possess;
drop table if exists Answer;
drop table if exists Question;
drop table if exists Game;
drop table if exists Player;

CREATE TABLE Player(
   idplayer INT AUTO_INCREMENT,
   name VARCHAR(50)  NOT NULL,
   password VARCHAR(255) NOT NULL,
   PRIMARY KEY(idplayer)
);

CREATE TABLE Game(
   idgame INT AUTO_INCREMENT,
   score INT,
   idplayer INT NOT NULL,
   PRIMARY KEY(idgame),
   FOREIGN KEY(idplayer) REFERENCES Player(idplayer)
);

CREATE TABLE Question(
   idquestion INT,
   difficultyLevel INT,
   label VARCHAR(255),
   PRIMARY KEY(idquestion)
);

CREATE TABLE Answer(
   idanswer INT AUTO_INCREMENT,
   label VARCHAR(255) ,
   iscorrect BOOLEAN,
   idquestion INT NOT NULL,
   PRIMARY KEY(idanswer),
   FOREIGN KEY(idquestion) REFERENCES Question(idquestion)
);

CREATE TABLE Possess(
   idgame INT NOT NULL,
   idquestion INT NOT NULL,
   FOREIGN KEY(idgame) REFERENCES Game(idgame),
   FOREIGN KEY(idquestion) REFERENCES Question(idquestion),
   PRIMARY KEY(idgame, idquestion)
);

-- Insérez quelques joueurs
INSERT INTO Player (name, password) VALUES
   ('Joueur 1', '$2a$10$o2UumzT7fDeIQiFZfv.uJOR7U7jP6uj62PSrLjvjh4tR.b3Th4wIa');

-- Insérez des jeux avec des scores pour les joueurs
INSERT INTO Game (score, idplayer) VALUES
   (100, 1);

-- Insertion de données dans la table "Question"
INSERT INTO Question (idquestion, difficultyLevel, label) VALUES
   (1, 1, 'Quelle est l''acronyme de SSL ?'),
   (2, 1, 'Quelle est la première étape de la méthodologie de test d''intrusion ?'),
   (3, 2, 'Quelle est la différence entre un pare-feu stateful et un pare-feu stateless ?'),
   (4, 2, 'Quelle est la vulnérabilité la plus courante dans les applications web ?'),
   (5, 3, 'Qu''est-ce que l''authentification à deux facteurs (2FA) ?'),
   (6, 3, 'Qu''est-ce qu''une attaque DDoS ?'),
   (7, 1, 'Quel est le protocole de chiffrement utilisé dans HTTPS ?'),
   (8, 2, 'Qu''est-ce que l''ingénierie sociale ?'),
   (9, 3, 'Qu''est-ce qu''une clé publique en cryptographie ?'),
   (10, 2, 'Quelle est la principale différence entre un virus et un ver informatique ?'),
   (11, 1, 'Qu''est-ce qu''un logiciel antivirus ?'),
   (12, 3, 'Qu''est-ce que le chiffrement de bout en bout ?');

-- Insertion de données dans la table "Answer" (exemples de réponses)
-- Assurez-vous d'ajuster les réponses correctes en conséquence.
INSERT INTO Answer (label, iscorrect, idquestion) VALUES
   ('Secure Sockets Layer', TRUE, 1),
   ('Security Socket Layer', FALSE, 1),
   ('Simplified Security Layer', FALSE, 1),
   ('Reconnaissance', TRUE, 2),
   ('Planification', FALSE, 2),
   ('Exploitation', FALSE, 2),
   ('Le pare-feu stateful conserve l''état des connexions, tandis que le pare-feu stateless ne le fait pas.', TRUE, 3),
   ('Le pare-feu stateful ne filtre que le trafic entrant, tandis que le pare-feu stateless filtre le trafic sortant.', FALSE, 3),
   ('Le pare-feu stateful est plus vulnérable aux attaques DDoS que le pare-feu stateless.', FALSE, 3),
   ('Injection SQL', TRUE, 4),
   ('Cross-Site Scripting (XSS)', TRUE, 4),
   ('Cross-Site Request Forgery (CSRF)', FALSE, 4),
   ('Une méthode d''authentification qui utilise deux facteurs différents pour vérifier l''identité de l''utilisateur.', TRUE, 5),
   ('Un processus de vérification d''identité basé sur la reconnaissance faciale uniquement.', FALSE, 5),
   ('Une méthode de chiffrement à deux couches pour sécuriser les données sensibles.', FALSE, 5),
   ('Attaque par déni de service distribué', TRUE, 6),
   ('Attaque par déni de service simple', FALSE, 6),
   ('Attaque par déni de service par injection', FALSE, 6),
   ('Transport Layer Security (TLS)', TRUE, 7),
   ('Secure Data Encryption (SDE)', FALSE, 7),
   ('High-Speed Security Protocol (HSSP)', FALSE, 7),
   ('Une technique visant à tromper les individus pour obtenir des informations confidentielles.', TRUE, 8),
   ('Une méthode de vérification de l''authenticité d''un e-mail.', TRUE, 8),
   ('Un système de détection d''intrusion avancé.', FALSE, 8),
   ('Une clé utilisée pour le chiffrement des données et qui peut être partagée publiquement.', TRUE, 9),
   ('Une clé utilisée uniquement pour le chiffrement asymétrique.', FALSE, 9),
   ('Une clé secrète partagée uniquement entre deux personnes.', FALSE, 9),
   ('Les vers se propagent indépendamment, tandis que les virus ont besoin d''un hôte.', TRUE, 10),
   ('Les virus se propagent indépendamment, tandis que les vers nécessitent un hôte.', FALSE, 10),
   ('Les vers et les virus sont des termes interchangeables pour désigner la même menace.', FALSE, 10),
   ('Un logiciel conçu pour détecter et éliminer les logiciels malveillants.', TRUE, 11),
   ('Un logiciel de chiffrement de fichiers sensibles.', FALSE, 11),
   ('Un logiciel de surveillance de l''activité réseau.', FALSE, 11),
   ('Chiffrement où seuls l''expéditeur et le destinataire peuvent déchiffrer les données.', TRUE, 12),
   ('Chiffrement qui utilise une clé publique pour le chiffrement et une clé privée pour le déchiffrement.', TRUE, 12),
   ('Chiffrement symétrique avec une seule clé partagée publiquement.', FALSE, 12);
