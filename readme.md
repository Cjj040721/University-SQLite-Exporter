<div align="center">

# University SQLite Exporter

**Export SQLite schema and rows into separate SQL files with dependency-aware table ordering.**

A small Java utility that writes `CREATE TABLE`, primary key, foreign key, index, and `INSERT` statements while using topological sorting to place referenced tables before dependent tables.

[![Java](https://img.shields.io/badge/Java-17%2B-ED8B00?logo=openjdk&logoColor=white)](https://openjdk.org/)
[![SQLite](https://img.shields.io/badge/SQLite-JDBC-003B57?logo=sqlite&logoColor=white)](https://www.sqlite.org/)
[![Maven](https://img.shields.io/badge/Maven-Build-C71A36?logo=apachemaven&logoColor=white)](https://maven.apache.org/)
[![Verification](https://img.shields.io/badge/Real_Export-Verified-2AA887)](#verification)

[Features](#features) · [Build and Run](#build-and-run) · [Output](#output) · [Verification](#verification) · [Limitations](#limitations)

</div>

---

## 1. Features

- Connects to SQLite through the Xerial JDBC driver.
- Exports schema and data into separate files.
- Orders tables using foreign-key dependencies and depth-first topological sorting.
- Writes primary keys, foreign keys, and explicit indexes.
- Escapes single quotes in text values and writes `NULL` correctly.
- Accepts one or more database paths from the command line.
- Creates the output directory automatically.
- Preserves the original six-database default paths for compatibility.

> The repository name reflects its original university and LSH datasets. The utility exports SQLite databases; it does not implement an LSH indexing algorithm.

## 2. Build and Run

### Requirements

- JDK 17 or newer
- Maven 3.9+

```bash
git clone https://github.com/Cjj040721/University-SQLite-Exporter.git
cd University-SQLite-Exporter
mvn clean package
```

Export one database:

```bash
mvn exec:java -Dexec.args="/absolute/path/to/university.db"
```

Export several databases in one run:

```bash
mvn exec:java -Dexec.args="/path/to/university.db /path/to/LSH.db"
```

If no arguments are supplied, the program looks for the original defaults:

```text
data/university.db
data/LSH.db
data/Chinook.db
data/Northwind.db
data/test1.db
data/test2.db
```

<a id="output"></a>
## 3. Output

For `university.db`, the exporter creates:

```text
output/university_schema.sql
output/university_data.sql
```

The schema file includes table definitions, primary and foreign keys, and indexes. The data file contains one `INSERT` statement per row. Table order follows dependencies where possible; if a dependency cycle is detected, the exporter warns and falls back to the database's original table order.

## 4. Repository Structure

```text
University-SQLite-Exporter/
├── src/DatabaseExporter.java  # JDBC metadata, topological sort, and SQL writers
├── scripts/                  # Original database restoration scripts
├── docs/                     # Original documentation
├── pom.xml                   # Java 17, SQLite JDBC, and exec configuration
└── README.md
```

<a id="verification"></a>
## 5. Verification

The current version was built and run against a real SQLite database containing `department` and `staff` tables with a foreign key, an explicit index, and the text `Ada O'Brien`.

| Check | Result |
|---|---|
| `mvn clean package` | Passed |
| JDBC connection and export | Passed |
| Dependency order | `department` written before `staff` |
| Primary and foreign keys | Present in the exported schema |
| Explicit index | Present in the exported schema |
| Text escaping | `Ada O''Brien` written correctly |
| Output directory | Created automatically |

No automated test suite is present; verification uses a clean Maven build and a real database export.

<a id="limitations"></a>
## 6. Limitations

- The exporter focuses on tables, keys, indexes, and rows; it does not provide complete SQLite database fidelity for views, triggers, or every internal SQLite object.
- Generated SQL disables foreign keys during schema import; restoration scripts should re-enable them where appropriate.
- Cyclic foreign-key dependencies require manual review after the fallback warning.
- This is an educational utility, not a replacement for SQLite's official backup and migration tools.

## 7. Author and Use

**Cao Junjia**, May 2025. Provided for educational use; do not redistribute without permission.

---

<div align="center">

**University SQLite Exporter — Schema first, dependencies in order, rows preserved.**

</div>
