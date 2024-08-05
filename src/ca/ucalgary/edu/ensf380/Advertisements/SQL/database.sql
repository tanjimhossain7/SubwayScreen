-- Create the Advertisements table
CREATE TABLE IF NOT EXISTS Advertisements (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    title TEXT NOT NULL,
    description TEXT,
    media_id INTEGER,
    display_order INTEGER,
    FOREIGN KEY (media_id) REFERENCES MediaFiles(id)
);

-- Create the MediaFiles table
CREATE TABLE IF NOT EXISTS MediaFiles (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    file_name TEXT NOT NULL,
    file_type TEXT NOT NULL,
    file_path TEXT NOT NULL
);
