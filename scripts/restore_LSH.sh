#!/bin/bash

echo "Restoring test_LSH.db..."

# Move to script directory
cd "$(dirname "$0")" || {
    echo "Failed to change directory."
    exit 1
}
# Remove old test database if it exists
rm -f test_LSH.db

# Import schema
if ! sqlite3 test_LSH.db < output/LSH_schema.sql; then
    echo "Warning: There may be invalid index statements (e.g., sqlite_autoindex)."
fi

# Import data
if ! sqlite3 test_LSH.db < output/LSH_data.sql; then
    echo "Error while importing data into test_LSH.db"
    exit 1
fi

# Show available tables
echo -e "\n Tables:"
sqlite3 test_LSH.db ".tables"

# Show sample data from each table
echo -e "\n Sample from 'heroes':"
sqlite3 test_LSH.db "SELECT * FROM heroes LIMIT 5;"

echo -e "\n Sample from 'planets':"
sqlite3 test_LSH.db "SELECT * FROM planets LIMIT 5;"

echo -e "\n Sample from 'powers':"
sqlite3 test_LSH.db "SELECT * FROM powers LIMIT 5;"

echo -e "\n Sample from 'missions':"
sqlite3 test_LSH.db "SELECT * FROM missions LIMIT 5;"

echo -e "\n Done: test_LSH.db is ready."
