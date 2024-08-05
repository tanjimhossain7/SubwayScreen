-- Create the database
CREATE DATABASE CityHallAds;

-- Use the database
USE CityHallAds;

-- Create the Advertisements table
CREATE TABLE Advertisements (
    id INT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    media_id INT,
    display_order INT,
    FOREIGN KEY (media_id) REFERENCES MediaFiles(id)
);

-- Create the MediaFiles table
CREATE TABLE MediaFiles (
    id INT AUTO_INCREMENT PRIMARY KEY,
    file_name VARCHAR(255) NOT NULL,
    file_type ENUM('PDF', 'MPG', 'JPEG', 'BMP') NOT NULL,
    file_path VARCHAR(255) NOT NULL
);