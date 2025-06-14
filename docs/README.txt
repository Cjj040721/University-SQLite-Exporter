# SCC201 Coursework - Database Exporter

Author: Cao Junjia
Student ID： 22722003
Date: May 28, 2025

This coursework implements a Java-based SQLite database exporter using JDBC.

##  Milestone Completion status (a-g)

### Platform Compatibility

These scripts and programs were tested on macOS 13 and Ubuntu 22.04 using SQLite 3.42.2 and Java 21.

### Process Stages and Output Files

All the milestones required for the coursework have been achieved, as follows:

| Stage | Description                                                                 | Output File Location                                                |
|-------|-----------------------------------------------------------------------------|----------------------------------------------------------------------|
| a     | Extract table data and generate `INSERT` statements                         | `output/university_backup.sql`, `output/LSH_backup.sql`             |
| b     | Validate and test SQL scripts                                               | `test_university.sql`, `test_LSH.sql`                               |
| c     | Automatically generate `CREATE TABLE` statements (without primary or foreign keys) | `output/university_with_create.sql`, `output/LSH_with_create.sql`   |
| d     | Extract and add primary keys to `CREATE TABLE` statements               | `output/university_with_pk.sql`, `output/LSH_with_pk.sql`  |
| e     | Extract and add foreign keys to `CREATE TABLE` statements               | `output/university_with_constraints.sql`, `output/LSH_with_constraints.sql` |
| f     | Extract and add `CREATE INDEX` statements                                   | `output/university_complete.sql`, `output/LSH_complete.sql`         |
| g     | Apply topological sorting to ensure table creation order respects foreign key constraints | `output/university_complete.sql`, `output/LSH_complete.sql` |

##  Bonus Features Implementation Description

| Feature       | Description                                                                 |
|---------------|-----------------------------------------------------------------------------|
| Bonus 1       | Added `DROP TABLE IF EXISTS` statements at the beginning of schema files    |
| Bonus 2       | Used JDBC metadata to correctly output `NOT NULL` constraints on fields     |
| Bonus 3       | Added comments at the top of schema files including JDBC driver name, version, database version, and export timestamp |
| Bonus 4       | All SQL output is beautified: proper indentation, line breaks, and clear comments |
| Bonus 5       | Separated data and structure output into two files: `*_schema.sql` and `*_data.sql` |


##  Directory and File Structure

SCC201_Coursework/
├── src/                      ← Java source code (`DatabaseExporter.java`)
├── data/                     ← Source databases (e.g., `university.db`, `LSH.db`)
├── output/                   ← All exported `.sql` files
│   ├── university_schema.sql
│   ├── university_data.sql
│   ├── LSH_schema.sql
│   ├── LSH_data.sql
│   └── Other milestone outputs (e.g., `LSH_complete.sql`, `university_with_pk.sql`, etc.)
├── restore_university.sh     ← Shell script to build `test_university.db` (macOS/Linux)
├── restore_LSH.sh            ← Shell script to build `test_LSH.db` (macOS/Linux)
├── test_university.db        ← Generated test database copy
├── test_LSH.db               ← Generated test database copy
├── README.txt                ← This documentation file
└── SCC201_Coursework.iml     ← IntelliJ project file


