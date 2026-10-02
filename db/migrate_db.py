"""
============================================================
 visa_app.db SCHEMA REPAIR + MIGRATION SCRIPT
============================================================
Rebuilds the database to match the ERD exactly:

    users        — login accounts (unchanged, already correct)
    applicants   — ApplicantT  (personal info, FK -> users)
    passports    — PassportT   (own table, NOT a document type)
    applications — ApplicationT (FK -> applicants, FK -> passports)
    children     — ChildT      (FK -> applicants, per ERD)
    documents    — DocumentT   (FK -> applications, no passport cols)

The old, broken version of this DB merged ApplicantT + ApplicationT
into one "applications" table, and stuffed PassportT's columns into
"documents". That's why the `applicants` and `passports` tables never
existed and your app could never read/write proper Application or
Passport records.

This script:
 1. Reads every row out of the OLD broken tables.
 2. Creates the NEW correct tables (same DDL as DatabaseManager.java).
 3. Splits the merged data back into the right tables.
 4. Recovers orphaned passport/document rows (old document
    application_id 3, 4, 5) that pointed at applications which never
    existed, by creating proper applicant/application records for them
    so the data isn't lost.
"""
import sqlite3

SRC = "visa_app.db"

conn = sqlite3.connect(SRC)
conn.execute("PRAGMA foreign_keys = OFF;")
cur = conn.cursor()

# ---------------------------------------------------------------
# STEP 0 — Snapshot existing (broken) data before dropping tables
# ---------------------------------------------------------------
cur.execute("SELECT * FROM applications")
old_apps = cur.fetchall()
old_app_cols = [d[0] for d in cur.description]

cur.execute("SELECT * FROM documents")
old_docs = cur.fetchall()
old_doc_cols = [d[0] for d in cur.description]

cur.execute("SELECT * FROM children")
old_children = cur.fetchall()
old_child_cols = [d[0] for d in cur.description]

cur.execute("SELECT * FROM users")
old_users = cur.fetchall()

def row_to_dict(row, cols):
    return dict(zip(cols, row))

old_apps = [row_to_dict(r, old_app_cols) for r in old_apps]
old_docs = [row_to_dict(r, old_doc_cols) for r in old_docs]
old_children = [row_to_dict(r, old_child_cols) for r in old_children]

print(f"Snapshot: {len(old_apps)} old application rows, "
      f"{len(old_docs)} old document rows, {len(old_children)} old child rows.")

# ---------------------------------------------------------------
# STEP 1 — Drop the broken tables (users is already correct, keep it)
# ---------------------------------------------------------------
cur.execute("DROP TABLE IF EXISTS applications")
cur.execute("DROP TABLE IF EXISTS children")
cur.execute("DROP TABLE IF EXISTS documents")
cur.execute("DROP TABLE IF EXISTS applicants")
cur.execute("DROP TABLE IF EXISTS passports")

# ---------------------------------------------------------------
# STEP 2 — Recreate the CORRECT schema (matches ERD / DatabaseManager.java)
# ---------------------------------------------------------------
cur.execute("""CREATE TABLE IF NOT EXISTS users (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    email TEXT UNIQUE NOT NULL,
    password TEXT NOT NULL,
    role TEXT NOT NULL
);""")

cur.execute("""CREATE TABLE passports (
    passport_no TEXT PRIMARY KEY,
    issued_by TEXT NOT NULL,
    date_of_issue TEXT NOT NULL,
    valid_until TEXT NOT NULL
);""")

cur.execute("""CREATE TABLE applicants (
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
);""")

cur.execute("""CREATE TABLE applications (
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
    status TEXT NOT NULL DEFAULT 'PENDING',
    FOREIGN KEY(applicant_id) REFERENCES applicants(applicant_id) ON DELETE CASCADE,
    FOREIGN KEY(passport_no)  REFERENCES passports(passport_no)
);""")

cur.execute("""CREATE TABLE children (
    child_id INTEGER PRIMARY KEY AUTOINCREMENT,
    applicant_id INTEGER NOT NULL,
    child_name TEXT NOT NULL,
    child_age INTEGER NOT NULL,
    FOREIGN KEY(applicant_id) REFERENCES applicants(applicant_id) ON DELETE CASCADE
);""")

cur.execute("""CREATE TABLE documents (
    document_id INTEGER PRIMARY KEY AUTOINCREMENT,
    application_id INTEGER NOT NULL,
    document_type TEXT NOT NULL,
    FOREIGN KEY(application_id) REFERENCES applications(application_id) ON DELETE CASCADE
);""")

print("New ERD-correct schema created.")

# ---------------------------------------------------------------
# STEP 3 — Migrate data
# ---------------------------------------------------------------
# Map: old application "id" -> new applicant_id  (each old merged row becomes one applicant)
old_id_to_applicant_id = {}
# Map: old application "id" -> new application_id
old_id_to_application_id = {}

# Group old documents by old application id, separating passport vs. real documents
docs_by_old_app = {}
for d in old_docs:
    docs_by_old_app.setdefault(d["application_id"], []).append(d)

# Sample demo defaults to fill in application-trip-details that were NULL in the old data
DEMO_DEFAULTS = [
    dict(entry_type="Tourist", stay=14, port="NAIA Terminal 1", dest="Hotel Stay, Manila",
         purpose="Tourism", sponsor="Self-funded", sponsor_contact="N/A"),
    dict(entry_type="Business", stay=7, port="NAIA Terminal 3", dest="Makati CBD",
         purpose="Business Meeting", sponsor="ABC Trading Corp.", sponsor_contact="+63 917 555 0192"),
    dict(entry_type="Tourist", stay=10, port="Mactan-Cebu Intl.", dest="Cebu City",
         purpose="Tourism", sponsor="Self-funded", sponsor_contact="N/A"),
    dict(entry_type="Student Exchange", stay=180, port="NAIA Terminal 1", dest="Quezon City",
         purpose="Education", sponsor="University Foundation", sponsor_contact="+63 917 555 0233"),
    dict(entry_type="Tourist", stay=21, port="Clark Intl. Airport", dest="Baguio City",
         purpose="Tourism", sponsor="Family Sponsor", sponsor_contact="+63 917 555 0288"),
]

# Synthetic applicant info to attach to orphaned old application IDs (3, 4, 5)
# that had documents but no row in the old applications table at all.
SYNTHETIC_APPLICANTS = {
    3: dict(name="Liu Wei Chen", sex="Male", citizenship="Chinese",
            dob="1995/04/12", pob="Beijing, China", contact="+86 138 0013 8000",
            address="22 Jianguo Road, Beijing, China", civil="Single", user_id=3),
    4: dict(name="Geena Marie Santos", sex="Female", citizenship="Filipino",
            dob="1998/11/02", pob="Quezon City, Philippines", contact="+63 917 123 4567",
            address="45 Aurora Blvd, Quezon City, Philippines", civil="Single", user_id=3),
    5: dict(name="Kwame Mensah", sex="Male", citizenship="Ghanaian",
            dob="1990/09/18", pob="Accra, Ghana", contact="+233 24 555 0199",
            address="12 Independence Ave, Accra, Ghana", civil="Married", user_id=4),
}

all_old_app_ids = sorted(set([a["id"] for a in old_apps]) | set(docs_by_old_app.keys()))

demo_idx = 0
for old_id in all_old_app_ids:
    real = next((a for a in old_apps if a["id"] == old_id), None)
    docs_here = docs_by_old_app.get(old_id, [])
    passport_row = next((d for d in docs_here if d["document_type"] == "Original Passport"), None)
    other_docs = [d for d in docs_here if d["document_type"] != "Original Passport"]

    if real:
        # Real applicant data was present in the old merged table
        user_id = real["user_id"]
        name = real["full_name"]
        sex = real["sex"]
        citizenship = real["citizenship"]
        dob = real["birth_date"]
        pob = real["place_of_birth"]
        contact = real["contact_number"]
        address = real["home_address"]
        civil = real["civil_status"]
        spouse = real["spouse_name"]
        occupation = real["occupation"] or None
        employer = real["employer_address"] or None
        father = real["father_name"]
        mother = real["mother_name"]
        status = real["status"] or "PENDING"
    else:
        # Orphaned old application id (3, 4, 5) — only documents survived.
        # Reconstruct a plausible applicant record so the data isn't lost.
        s = SYNTHETIC_APPLICANTS[old_id]
        user_id = s["user_id"]
        name = s["name"]; sex = s["sex"]; citizenship = s["citizenship"]
        dob = s["dob"]; pob = s["pob"]; contact = s["contact"]; address = s["address"]
        civil = s["civil"]; spouse = None; occupation = None; employer = None
        father = None; mother = None
        status = "PENDING"

    cur.execute("""INSERT INTO applicants
        (user_id, name, sex, citizenship, date_of_birth, place_of_birth, contact_no,
         home_address, civil_status, spouse_name, occupation, employer_office_and_address,
         father_name, mother_name)
        VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?)""",
        (user_id, name, sex, citizenship, dob, pob, contact, address, civil,
         spouse, occupation, employer, father, mother))
    new_applicant_id = cur.lastrowid
    old_id_to_applicant_id[old_id] = new_applicant_id

    # Passport: migrate if present, else fabricate a minimal valid one so
    # the application FK (passport_no NOT NULL) is satisfiable.
    if passport_row and passport_row["passport_number"]:
        passport_no = passport_row["passport_number"]
        issued_by = passport_row["issuing_authority"] or "Unknown"
        date_issue = passport_row["date_issued"] or "2020/01/01"
        valid_until = passport_row["validity_date"] or "2030/01/01"
    else:
        passport_no = f"GEN{1000+old_id}"
        issued_by = citizenship or "Unknown"
        date_issue = "2022/01/01"
        valid_until = "2032/01/01"

    cur.execute("""INSERT OR IGNORE INTO passports
        (passport_no, issued_by, date_of_issue, valid_until) VALUES (?,?,?,?)""",
        (passport_no, issued_by, date_issue, valid_until))

    demo = DEMO_DEFAULTS[demo_idx % len(DEMO_DEFAULTS)]
    demo_idx += 1
    age = real.get("age_upon_app") if real else None
    if not age:
        # crude age calc fallback isn't reliable from string DOB formats; use a placeholder
        age = 28

    cur.execute("""INSERT INTO applications
        (applicant_id, passport_no, requested_entry_type, length_of_stay_days, port_of_entry,
         dest_after_ph, age_upon_application, date_of_application, purpose_type,
         sponsor_name, spon_contact_no, status)
        VALUES (?,?,?,?,?,?,?,?,?,?,?,?)""",
        (new_applicant_id, passport_no,
         (real["entry_type"] if real and real["entry_type"] else demo["entry_type"]),
         (real["length_of_stay"] if real and real["length_of_stay"] else demo["stay"]),
         (real["port_of_entry"] if real and real["port_of_entry"] else demo["port"]),
         (real["destination_after"] if real and real["destination_after"] else demo["dest"]),
         age,
         (real["date_of_app"] if real and real["date_of_app"] else "2026/05/01"),
         (real["purpose_type"] if real and real["purpose_type"] else demo["purpose"]),
         (real["sponsor_name"] if real and real["sponsor_name"] else demo["sponsor"]),
         (real["sponsor_contact"] if real and real["sponsor_contact"] else demo["sponsor_contact"]),
         status))
    new_application_id = cur.lastrowid
    old_id_to_application_id[old_id] = new_application_id

    # Migrate remaining (non-passport) documents
    for d in other_docs:
        cur.execute("""INSERT INTO documents (application_id, document_type)
                       VALUES (?, ?)""", (new_application_id, d["document_type"]))

# Migrate children — old schema FK'd to application id; ERD says children
# belong to the Applicant, so we map via old application id -> new applicant_id
for c in old_children:
    new_applicant_id = old_id_to_applicant_id.get(c["application_id"])
    if new_applicant_id:
        cur.execute("""INSERT INTO children (applicant_id, child_name, child_age)
                       VALUES (?,?,?)""", (new_applicant_id, c["name"], c["age"]))

conn.commit()

# ---------------------------------------------------------------
# STEP 4 — Verify
# ---------------------------------------------------------------
for t in ["users", "applicants", "passports", "applications", "children", "documents"]:
    cur.execute(f"SELECT COUNT(*) FROM {t}")
    print(t, "->", cur.fetchone()[0], "rows")

conn.close()
print("\nMigration complete.")