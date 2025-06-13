CREATE TABLE roles (
    id SERIAL PRIMARY KEY,
    name VARCHAR(20) NOT NULL UNIQUE
);

CREATE TABLE users (
    id SERIAL PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    role_id INTEGER NOT NULL REFERENCES roles(id)
);

CREATE TABLE books (
    id SERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    author_first_name VARCHAR(100),
    author_last_name VARCHAR(100),
    genre VARCHAR(100),
    description TEXT,
    cover_url VARCHAR(500)
);

CREATE TABLE book_copies (
    id SERIAL PRIMARY KEY,
    inventory_number VARCHAR(50) UNIQUE NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'AVAILABLE',
    book_id INTEGER NOT NULL REFERENCES books(id)
);

CREATE TABLE orders (
    id SERIAL PRIMARY KEY,
    user_id INTEGER NOT NULL REFERENCES users(id),
    copy_id INTEGER NOT NULL REFERENCES book_copies(id),
    order_type VARCHAR(20) NOT NULL,
    order_status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    issue_date DATE,
    due_date DATE,
    return_date DATE
);
