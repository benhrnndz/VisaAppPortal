-- =====================================================================
-- VisaAppPortal Canonical Database Schema (SQLite 3NF)
-- =====================================================================

PRAGMA foreign_keys = ON;

-- 1. Users Table (Authentication and Authorization)
CREATE TABLE IF NOT EXISTS users (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    email TEXT UNIQUE NOT NULL,
    password TEXT NOT NULL,
    role TEXT NOT NULL CHECK(role IN ('APPLICANT', 'ADMIN'))
);

-- 2. Passports Table (Natural Key: passport_no)
CREATE TABLE IF NOT EXISTS passports (
    passport_no TEXT PRIMARY KEY,
    issued_by TEXT NOT NULL,
    date_of_issue TEXT NOT NULL,
    valid_until TEXT NOT NULL
);

-- 3. Applicants Table (Biographic profile linked to user)
CREATE TABLE IF NOT EXISTS applicants (
    applicant_id INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id INTEGER NOT NULL,
    name TEXT NOT NULL,
    sex TEXT NOT NULL,
    citizenship TEXT NOT NULL,
    date_of_birth TEXT NOT NULL,
    place_of_birth TEXT NOT NULL,
    contact_no TEXT NOT NULL,
    home_address TEXT NOT NULL,
    civil_status TEXT NOT NULL,
    spouse_name TEXT,
    occupation TEXT,
    employer_office_and_address TEXT,
    father_name TEXT,
    mother_name TEXT,
    FOREIGN KEY(user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- 4. Applications Table (Visa travel & submission records)
CREATE TABLE IF NOT EXISTS applications (
    application_id INTEGER PRIMARY KEY AUTOINCREMENT,
    applicant_id INTEGER NOT NULL,
    passport_no TEXT NOT NULL,
    requested_entry_type TEXT NOT NULL,
    length_of_stay_days INTEGER NOT NULL,
    port_of_entry TEXT NOT NULL,
    dest_after_ph TEXT,
    age_upon_application INTEGER NOT NULL,
    date_of_application TEXT NOT NULL,
    purpose_type TEXT NOT NULL,
    sponsor_name TEXT,
    spon_contact_no TEXT,
    status TEXT NOT NULL DEFAULT 'PENDING' CHECK(status IN ('PENDING', 'APPROVED', 'DENIED')),
    FOREIGN KEY(applicant_id) REFERENCES applicants(applicant_id) ON DELETE CASCADE,
    FOREIGN KEY(passport_no)  REFERENCES passports(passport_no)
);

-- 5. Children Table (Dependents belonging to applicant)
CREATE TABLE IF NOT EXISTS children (
    child_id INTEGER PRIMARY KEY AUTOINCREMENT,
    applicant_id INTEGER NOT NULL,
    child_name TEXT NOT NULL,
    child_age INTEGER NOT NULL,
    FOREIGN KEY(applicant_id) REFERENCES applicants(applicant_id) ON DELETE CASCADE
);

-- 6. Documents Table (Supporting documents per application)
CREATE TABLE IF NOT EXISTS documents (
    document_id INTEGER PRIMARY KEY AUTOINCREMENT,
    application_id INTEGER NOT NULL,
    document_type TEXT NOT NULL,
    FOREIGN KEY(application_id) REFERENCES applications(application_id) ON DELETE CASCADE
);

-- Indices for performance
CREATE INDEX IF NOT EXISTS idx_applicants_user_id ON applicants(user_id);
CREATE INDEX IF NOT EXISTS idx_applications_applicant_id ON applications(applicant_id);
CREATE INDEX IF NOT EXISTS idx_applications_passport_no ON applications(passport_no);
CREATE INDEX IF NOT EXISTS idx_applications_status ON applications(status);
CREATE INDEX IF NOT EXISTS idx_children_applicant_id ON children(applicant_id);
CREATE INDEX IF NOT EXISTS idx_documents_application_id ON documents(application_id);
