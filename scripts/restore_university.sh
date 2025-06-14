#!/bin/bash

echo " Restoring test_university.db..."

cd "$(dirname "$0")" || exit

# Remove existing test database
rm -f test_university.db

# Create new database from schema and data files
sqlite3 test_university.db < output/university_schema.sql
sqlite3 test_university.db < output/university_data.sql

# Optional: check results
echo " Tables:"
sqlite3 test_university.db ".tables"

echo " Sample from 'staff':"
sqlite3 test_university.db "SELECT * FROM staff LIMIT 5;"

echo " Sample from 'department':"
sqlite3 test_university.db "SELECT * FROM department LIMIT 5;"

echo " Done: test_university.db is ready."
