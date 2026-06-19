import sqlite3
conn = sqlite3.connect('visa_app.db')
cur = conn.cursor()
cur.execute("PRAGMA table_info(applications)")
print(cur.fetchall())