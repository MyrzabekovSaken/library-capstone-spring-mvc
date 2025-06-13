INSERT INTO roles (name) VALUES ('READER');
INSERT INTO roles (name) VALUES ('LIBRARIAN');
INSERT INTO roles (name) VALUES ('ADMIN');

INSERT INTO users (username, email, password, status, role_id)
VALUES (
    'admin',
    'admin@mail.com',
    'admin',
    'ACTIVE',
    1
);

INSERT INTO users (username, email, password, status, role_id)
VALUES (
    'librarian',
    'librarian@mail.com',
    'librarian',
    'ACTIVE',
    2
);
