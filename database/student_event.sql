-- ============================================================
-- Student Event-Management System - MySQL Database Schema
-- Database Name: student_event_db
-- ============================================================

CREATE DATABASE IF NOT EXISTS student_event_db;
USE student_event_db;

-- ------------------------------------------------------------
-- Table structure for `student_details`
-- ------------------------------------------------------------
DROP TABLE IF EXISTS event_details;
DROP TABLE IF EXISTS student_details;

CREATE TABLE student_details (
    id INT AUTO_INCREMENT PRIMARY KEY,
    student_name VARCHAR(100) NOT NULL,
    register_number VARCHAR(20) NOT NULL UNIQUE,
    phone_number VARCHAR(10) NOT NULL,
    email VARCHAR(100) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ------------------------------------------------------------
-- Table structure for `event_details`
-- ------------------------------------------------------------
CREATE TABLE event_details (
    id INT AUTO_INCREMENT PRIMARY KEY,
    student_name VARCHAR(100) NOT NULL,
    student_register_number VARCHAR(20) NOT NULL,
    event_name VARCHAR(150) NOT NULL,
    event_date DATE NOT NULL,
    event_place VARCHAR(100) NOT NULL,
    prize_won ENUM('1st Prize', '2nd Prize', '3rd Prize', 'Participation') NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_student_reg FOREIGN KEY (student_register_number) 
        REFERENCES student_details(register_number) 
        ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ------------------------------------------------------------
-- Sample Seed Data for testing
-- ------------------------------------------------------------

INSERT INTO student_details (student_name, register_number, phone_number, email) VALUES
('Alex Morgan', '21CS001', '9876543210', 'alex.morgan@college.edu'),
('Sophia Chen', '21CS002', '9123456789', 'sophia.chen@college.edu'),
('Ethan Hunt', '21EC015', '9988776655', 'ethan.hunt@college.edu'),
('Maya Lin', '22IT042', '9543210987', 'maya.lin@college.edu'),
('David Miller', '22ME009', '9012345678', 'david.miller@college.edu');

INSERT INTO event_details (student_name, student_register_number, event_name, event_date, event_place, prize_won) VALUES
('Alex Morgan', '21CS001', 'National Hackathon 2026', '2026-03-15', 'Auditorium Hall A', '1st Prize'),
('Sophia Chen', '21CS002', 'Web Crafting Symposium', '2026-04-10', 'Tech Park Lab 3', '2nd Prize'),
('Ethan Hunt', '21EC015', 'RoboWars Championship', '2026-05-02', 'Indoor Sports Complex', '1st Prize'),
('Maya Lin', '22IT042', 'CodeSprint 2.0', '2026-05-20', 'Main Computer Lab', '3rd Prize'),
('David Miller', '22ME009', 'CAD Design Challenge', '2026-06-11', 'Mechanical Workshop B', 'Participation'),
('Alex Morgan', '21CS001', 'AI Paper Presentation', '2026-07-04', 'Seminar Hall 2', '2nd Prize');
